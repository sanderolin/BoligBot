package no.sanderolin.boligbot.web.security;

import lombok.RequiredArgsConstructor;
import no.sanderolin.boligbot.dao.model.UserModel;
import no.sanderolin.boligbot.dao.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserModel userModel = userRepository
                .findByEmail(email)
                .orElseThrow(
                        () -> new UsernameNotFoundException("User Not Found with email: " + email)
                );
        return new org.springframework.security.core.userdetails.User(
                userModel.getEmail(),
                userModel.getPasswordHash(),
                Collections.emptyList()
        );
    }
}
