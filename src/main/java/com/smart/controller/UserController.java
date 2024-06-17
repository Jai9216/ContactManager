package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	// common method run for all
	@ModelAttribute
	public void addCommonData(Model m,Principal p) {
		String userName = p.getName();
		User u = userRepository.getUserByUserName(userName);
		m.addAttribute("user", u);
	}

	@RequestMapping("/")
	public String dashboard(Model m,Principal principal) {
//		String userName = principal.getName();
//		User u = userRepository.getUserByUserName(userName);
//		m.addAttribute("user", u);
		m.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}
	
	// add form
	@RequestMapping("/add-contact")
	public String openAddContactForm(Model m) {
		m.addAttribute("title", "Add Contact");
		m.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	// process add contact
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file ,Principal p,HttpSession session) {
		try {
		System.out.println(contact);
		String name = p.getName();
		User u = userRepository.getUserByUserName(name);
		u.getContacts().add(contact);
		contact.setUser(u);
		// image file saving
		
		if(file.isEmpty()) {
			System.out.println("File is empty");
			contact.setImage("contact.png");
		}else {
			contact.setImage(file.getOriginalFilename());
			File saveFile = new ClassPathResource("static/img").getFile();
			Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(),path,StandardCopyOption.REPLACE_EXISTING);
		}
		
		//saving user
		this.userRepository.save(u);
		session.setAttribute("message", new Message("Contact Added successfully!! Add More..", "alert-success"));
		}catch (Exception e) {
			System.out.println("Error "+e.getMessage());
			e.printStackTrace();
			session.setAttribute("message", new Message("Something Went Wrong!! Try Again..", "alert-danger"));
		}
		
		return "normal/add_contact_form";
	}
	
	
	// show contacts
	// per page 5[n]
	//current page =0[page]
	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model m,Principal p) {
		m.addAttribute("title", "Show user contacts");
        // pageable obj for pagination
		Pageable pageable= PageRequest.of(page, 5);
		String userName = p.getName();
		User u = this.userRepository.getUserByUserName(userName);
        Page<Contact> contacts = this.contactRepository.findContactsByUser(u.getId(),pageable);
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages",contacts.getTotalPages());
//		alternate way
//		List<Contact> contacts = u.getContacts();
		
		
		return "normal/show_contacts";
	}
	
	// showing particular contact details
	@GetMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") int cId,Model m,Principal p) {
		Optional<Contact> op= this.contactRepository.findById(cId);
		Contact c = op.get();
		// checking contact should be belong to same user
		String userName = p.getName();
		User u = this.userRepository.getUserByUserName(userName);
		if(u.getId() == c.getUser().getId()) {
			m.addAttribute("contact", c);
			m.addAttribute("title", c.getName());
		}
		return "normal/contact_detail";
	}
	
	//deleting contact
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId,Model m,Principal p,HttpSession session) {
		Optional<Contact> op=this.contactRepository.findById(cId);
		Contact c = op.get();
		//check
		String userName = p.getName();
		User u = this.userRepository.getUserByUserName(userName);
		if(u.getId()==c.getUser().getId()) {
			// because of all cascade type
			c.setUser(null);
			this.contactRepository.delete(c);
			session.setAttribute("message", new Message("Contact Deleted Successfully...","alert-success"));
		}
		return "redirect:/user/show-contacts/0";
	}
	
	//update form for contact
	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cId, Model m) {
		m.addAttribute("title", "Update Contact form");
		Contact c = this.contactRepository.findById(cId).get();
		m.addAttribute("contact", c);
		return "normal/update_form";
	}
	
	//update contact 
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,Model m,HttpSession session,Principal p) {
		try {
			// old contact detail
			Contact oc = this.contactRepository.findById(contact.getcId()).get();
			
			if(!file.isEmpty()) {
				// delete old photo
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile,oc.getImage());
				file1.delete();
				// update new photo
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path= Paths.get(saveFile.getAbsolutePath()+File.separator+ file.getOriginalFilename());
				Files.copy(file.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			}else {
				// saving old file
				contact.setImage(oc.getImage());
			}
			User u = this.userRepository.getUserByUserName(p.getName());
			contact.setUser(u);
		    this.contactRepository.save(contact);
		    session.setAttribute("message", new Message("Your Contact is updated..", "alert-success"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
	// profile handler
	@GetMapping("/profile")
	public String yourProfile(Model m) {
		m.addAttribute("title", "Profile page");
		return "normal/profile";
	}
	
	// open settings handler
	@GetMapping("/settings")
	public String openSettings() {
		
		return "normal/settings";
	}
	
	// process change password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String opass, @RequestParam("newPassword") String npass,Principal p,HttpSession session) {
	    System.out.println(opass);
	    System.out.println(npass);
		User cu = this.userRepository.getUserByUserName(p.getName());
	    if(this.bCryptPasswordEncoder.matches(opass,cu.getPassword())) {
	    	//change password
	    	cu.setPassword(this.bCryptPasswordEncoder.encode(npass));
	    	this.userRepository.save(cu);
	    	session.setAttribute("message", new Message("Password Successfully changed...","alert-success"));
	    	return "redirect:/user/";
	    }else {
	    	session.setAttribute("message", new Message("Please enter correct old password...","alert-danger"));
	    	return "redirect:/user/settings";
	    }
		
	}
	
	
	
	
	
	
	
	
}
