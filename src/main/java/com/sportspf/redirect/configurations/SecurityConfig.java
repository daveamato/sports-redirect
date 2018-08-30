package com.sportspf.redirect.configurations;

import org.springframework.context.annotation.HttpSecurity;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvcSecurity;
import org.springframework.web.servlet.config.annotation.WebSecurityConfigurerAdapter;
import org.apache.http.client.HttpClient;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

@Configuration
@EnableWebMvcSecurity
@ComponentScan({"com.sportspf.redirect"})
public class SecurityConfig extends WebSecurityConfigurerAdapter {
  
  
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.requiresChannel()
      .requestMatchers(r -> r.getHeader("X-Forwarded-Proto") != null)
      .requiresSecure();
  }
}