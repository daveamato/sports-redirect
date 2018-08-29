package com.sportspf.redirect.configurations;

 
import org.springframework.context.annotation.Configuration; 
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder; 
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity; 
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration; 
import org.springframework.security.config.annotation.web.builders.HttpSecurity; 
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter; 
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity; 
 
@Configuration 
@EnableWebMvcSecurity 
public class SecurityConfig extends WebSecurityConfigurerAdapter { 
 
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.requiresChannel()
      .requestMatchers(r -> r.getHeader("X-Forwarded-Proto") != null)
      .requiresSecure();
  }
 
    @EnableGlobalMethodSecurity(prePostEnabled = true, jsr250Enabled = true) 
    private static class GlobalSecurityConfiguration extends GlobalMethodSecurityConfiguration { 
    } 
 
}