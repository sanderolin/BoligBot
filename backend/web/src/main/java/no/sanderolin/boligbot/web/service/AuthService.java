package no.sanderolin.boligbot.web.service;

import lombok.RequiredArgsConstructor;
import no.sanderolin.boligbot.dao.model.UserModel;
import no.sanderolin.boligbot.dao.repository.UserRepository;
import no.sanderolin.boligbot.web.dto.LoginRequestDTO;
import no.sanderolin.boligbot.web.dto.SignupRequestDTO;
import no.sanderolin.boligbot.web.exception.EmailAlreadyExistsException;
import no.sanderolin.boligbot.web.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public void signup(SignupRequestDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException("Email address already in use");
        }
        Instant now = Instant.now();
        UserModel user = new UserModel();
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setCreatedAt(now);
        user.setLastModifiedAt(now);
        userRepository.save(user);
    }

    public String login(LoginRequestDTO dto) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );
        return jwtUtil.generateAccessToken(auth.getName());
    }
}
