package com.example.demo.Service.Impl;

import com.example.demo.Config.CustomUser;
import com.example.demo.Entity.User;
import com.example.demo.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {



    @Autowired
     UserRepository userRepository;



    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("username "+ username);
        User user = userRepository.findByEmail(username);
        if(user == null) {
            throw new UsernameNotFoundException("User NOT Found for :"+username);
        }
        return new CustomUser(user);
    }

}