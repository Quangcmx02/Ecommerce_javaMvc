package com.example.demo.Service.Impl;

import com.example.demo.Entity.User;
import com.example.demo.Repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService{
	@Autowired
	private UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(email);
		if (user == null) {
			throw new UsernameNotFoundException("User not found with email: " + email);
		}
		return org.springframework.security.core.userdetails.User
				.withUsername(user.getEmail())
				.password(user.getPassword())
				.roles(user.getRole())
				.disabled(!user.getIsEnable())
				.accountLocked(!user.getAccountStatusNonLocked())
				.build();
	}
	// Thêm phương thức để lấy User entity trực tiếp nếu cần
	public User getUserEntity(String email) {
		return userRepository.findByEmail(email);}


}
