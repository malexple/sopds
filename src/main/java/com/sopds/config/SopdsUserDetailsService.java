package com.sopds.config;

import com.sopds.domain.AuthUser;
import com.sopds.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SopdsUserDetailsService implements UserDetailsService {

    private final AuthUserRepository authUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AuthUser authUser = authUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (Boolean.TRUE.equals(authUser.getIsSuperuser())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        if (Boolean.TRUE.equals(authUser.getIsStaff())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_STAFF"));
        }

        return User.builder()
                .username(authUser.getUsername())
                .password(authUser.getPassword())
                .disabled(!Boolean.TRUE.equals(authUser.getIsActive()))
                .authorities(authorities)
                .build();
    }
}