package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgetController {
	
	@Autowired
	private EmailService emailService;
	@Autowired
	private UserRepository repository;
	@Autowired
	private BCryptPasswordEncoder encoder;
	
	Random random = new Random(1000);
	
	// eamil id form open
	@GetMapping("/forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}
	
	// for sending otp to email
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email,HttpSession session) {
		System.out.println(email);
		// generating otp
		int otp = random.nextInt(999999);
		// write code to send otp to email
		String subject = "OTP from SCM";
		String message = 
				"<div style='border: 1px solid #e2e2e2; padding:20px'>"
				+ "<h1>OTP = "+otp+"</h1>"
				+ "</div>";
		String to = email;
		boolean flag = this.emailService.sendEmail(subject, message, to);
		System.out.println(otp);
		if(flag) { 
			session.setAttribute("ssotp", otp);
			session.setAttribute("email", email);
			return "verify_otp";
		}else {
			session.setAttribute("message", "Check Your email id!!");
			return "forgot_email_form";
		}
	}
	
	// for verifying otp
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") Integer otp,HttpSession session) {
		System.out.println(otp);
		Integer ssotp = (Integer)session.getAttribute("ssotp");
		System.out.println(ssotp);
		String email = (String)session.getAttribute("email");
		if(ssotp.equals(otp)) {
			// checking user exist or not 
			User u = this.repository.getUserByUserName(email);
//			System.out.println(u);
			if(u==null) {
				// send error message
				session.setAttribute("message", "User doesn't exits with this email");
				return "forgot_email_form";
			}else {
				//send password change form
				return "password_change_form";
			}
//			return "password_change_form";
		}else {
			session.setAttribute("message","You have entered wrong otp !!");
			return "verify_otp";
		}
	}
	
	// change password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("npass") String npass,HttpSession session) {
		String email = (String) session.getAttribute("email");
		User u = this.repository.getUserByUserName(email);
		u.setPassword(this.encoder.encode(npass));
		this.repository.save(u);
		return "redirect:/signin?change=password changed successfully..";
	}
}
