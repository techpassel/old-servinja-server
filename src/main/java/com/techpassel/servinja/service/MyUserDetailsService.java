package com.techpassel.servinja.service;

import com.techpassel.servinja.model.AuthUserDetails;
import com.techpassel.servinja.model.User;
import com.techpassel.servinja.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String searchKey) throws UsernameNotFoundException {
        Optional<User> user = userRepo.findByEmail(searchKey);
        if(user.isEmpty()){
            user = userRepo.findByPhone(searchKey);
        }
        user.orElseThrow(() -> new UsernameNotFoundException("Not found: " + searchKey));

        return user.map(AuthUserDetails::new).get(); //To convert user data as default UserDetails structure
    }
}
