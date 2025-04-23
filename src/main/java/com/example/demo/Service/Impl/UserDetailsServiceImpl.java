package com.example.demo.Service.Impl;

import com.example.demo.Entity.User;
import com.example.demo.Repository.UserRepository;

import com.example.demo.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService{
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private UserService userService;
	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(email);
		if (user == null) {
			throw new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email);
		}
		if (!user.getIsActive()) {
			throw new UsernameNotFoundException("Tài khoản chưa được kích hoạt: " + email);
		}
		if (!user.getIsEnable()) {
			throw new UsernameNotFoundException("Tài khoản bị vô hiệu hóa: " + email);
		}
		if (!user.getAccountStatusNonLocked()) {
			if (userService.isUnlockAccountTimeExpired(user)) {

				return org.springframework.security.core.userdetails.User
						.withUsername(user.getEmail())
						.password(user.getPassword())
						.roles(user.getRole())
						.disabled(!user.getIsEnable())
						.accountLocked(false)
						.build();
			}
			throw new UsernameNotFoundException("Tài khoản của bạn đang bị khóa. Vui lòng thử lại sau 5 phút.");
		}
		return org.springframework.security.core.userdetails.User
				.withUsername(user.getEmail())
				.password(user.getPassword())
				.roles(user.getRole())
				.disabled(!user.getIsEnable())
				.accountLocked(!user.getAccountStatusNonLocked())
				.build();
	}

	public User getUserEntity(String email) {
		return userRepository.findByEmail(email);}


}
