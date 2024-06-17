package com.smart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class MyConfig{

    @Bean
    UserDetailsService getUserDetailsService() {
		return new UserDetailsServiceImpl();
	}
	
    @Bean 
    BCryptPasswordEncoder passwordEncoder() {
    	return new BCryptPasswordEncoder();
    }
    
    @Bean 
    DaoAuthenticationProvider authenticationProvider() {
    	DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
    	daoAuthenticationProvider.setUserDetailsService(this.getUserDetailsService());
    	daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
    	return daoAuthenticationProvider;
    }
    
    
    @Bean
    AuthenticationManager authenticationManager() throws Exception{
    	return new ProviderManager(authenticationProvider());
    }
    
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    	 http.csrf(AbstractHttpConfigurer::disable)
         .authorizeHttpRequests((authorize) -> authorize
                         .requestMatchers("/admin/**").hasRole("ADMIN")
                         .requestMatchers("/user/**").hasRole("USER")
                         .requestMatchers("/**").permitAll())
                         .httpBasic(Customizer.withDefaults())
                     	 .formLogin(form -> form
                     			.loginPage("/signin")
                     			.loginProcessingUrl("/dologin")
                     			.defaultSuccessUrl("/user/"));
    	return http.build();
    }
    
    
	
}
