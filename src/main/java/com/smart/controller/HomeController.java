package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.web.IWebSession;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomeController {

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder ;
	
	
	@GetMapping("/")
	public String home(Model m) {
		m.addAttribute("title", "Home - Smart Contact Manager");
		return "home";
	}
	
	@GetMapping("/about")
	public String about(Model m) {
		m.addAttribute("title", "About - Smart Contact Manager");
		return "about";
	}
	
	@GetMapping("/signup")
	public String signup(Model m) {
		m.addAttribute("title", "Register - Smart Contact Manager");
		m.addAttribute("user", new User());
		return "signup";
	}
	
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User u,BindingResult result1,@RequestParam(value = "agreement",defaultValue = "false") boolean a,Model m, HttpSession session) {
		try {
		if(!a) {
			throw new Exception("You have not agreed terms and conditions");
		}
		if(result1.hasErrors()) {
			m.addAttribute("user", u);
			return "signup";
		}
		u.setRole("ROLE_USER");
		u.setEnabled(true);
		u.setImageUrl("default.png");
		u.setPassword(passwordEncoder.encode(u.getPassword()));
		User result= this.userRepository.save(u);
		m.addAttribute("user", new User());	
		session.setAttribute("message", new Message("Successfully Registered !!", "alert-success"));
		return "signup";
		} catch (Exception e) {
			e.printStackTrace();
			m.addAttribute("user", u);
			session.setAttribute("message", new Message("Something went wrong!!"+e.getMessage(), "alert-danger"));
			return "signup";
		}
		
	}

    // handler for custom login
	@GetMapping("/signin")
	public String customLogin(Model m) {
		m.addAttribute("title", "Login");
		return "login";
	}
}
