package no.sanderolin.boligbot.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.sanderolin.boligbot.web.dto.LoginRequestDTO;
import no.sanderolin.boligbot.web.dto.SignupRequestDTO;
import no.sanderolin.boligbot.web.exception.EmailAlreadyExistsException;
import no.sanderolin.boligbot.web.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequestDTO signupRequestDTO) {
        try {
            authService.signup(signupRequestDTO);
            return ResponseEntity.ok("User registered successfully!");
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        try {
            String jwt = authService.login(loginRequestDTO);
            return ResponseEntity.ok(jwt);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid credentials");
        }
    }
}
