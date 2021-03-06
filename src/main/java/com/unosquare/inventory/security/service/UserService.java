package com.unosquare.inventory.security.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.unosquare.inventory.security.client.UserFeignClient;
import com.unosquare.inventory.security.model.User;

@Service
public class UserService implements UserDetailsService {
	
	private Logger log = LoggerFactory.getLogger(UserService.class);

	private UserFeignClient userClient;

	@Autowired
	public UserService(UserFeignClient userClient) {
		this.userClient = userClient;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userClient.findByUsername(username);
		
		if(user == null) {
			log.error(String.format("The user %s doesn't exists in the system", username));
			throw new UsernameNotFoundException(String.format("The user %s doesn't exists in the system", username));
		}
		
		List<GrantedAuthority> authorities = user.getRoles()
				.stream()
				.map(role -> new SimpleGrantedAuthority(role.getName()))
				.peek(authority -> log.info("Role: " + authority.getAuthority()))
				.collect(Collectors.toList());

		return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(),
				user.getEnabled(), true, true, true, authorities);
	}

}
