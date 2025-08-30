package no.sanderolin.boligbot.web.v1.security;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import no.sanderolin.boligbot.web.v1.security.service.AuthService;
import no.sanderolin.boligbot.web.v1.security.exception.EmailAlreadyExistsException;
import no.sanderolin.boligbot.web.v1.security.request.LoginRequestDTO;
import no.sanderolin.boligbot.web.v1.security.request.SignupRequestDTO;
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
