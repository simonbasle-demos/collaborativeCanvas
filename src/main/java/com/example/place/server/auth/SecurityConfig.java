package com.example.place.server.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * @author Simon Baslé
 */
@EnableWebFluxSecurity
public class SecurityConfig {

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		return http
				.csrf().disable()
				.formLogin()
				.and()
				.authorizeExchange()
				.pathMatchers("/admin/**").hasRole("ADMIN")
				.pathMatchers("/paint/").authenticated()
				.anyExchange().permitAll()
				.and()
				.build();
	}

	@Bean
	public ReactiveAuthenticationManager manager(@Autowired ReactiveUserDetailsService userDetailsService) {
		return new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
	}
}
