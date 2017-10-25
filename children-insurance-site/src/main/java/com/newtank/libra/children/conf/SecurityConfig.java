package com.newtank.libra.children.conf;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

/**
 * Created by huanglei on 2016/12/26.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    //In op-site all requests need to be authenticated.
    http.authorizeRequests().antMatchers(HttpMethod.OPTIONS, "/**").permitAll();

    // Disable csrf filter. Because our operation requires customized headers.
    http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    http.csrf().disable();
  }

//  @Autowired
//  public void registerGlobal(AuthenticationManagerBuilder auth) throws Exception {
//    auth.authenticationProvider(authenticator);
//  }

}
