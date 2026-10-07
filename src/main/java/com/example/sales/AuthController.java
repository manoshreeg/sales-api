package com.example.sales;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    record Credentials(@NotBlank String username, @NotBlank @Size(min = 6) String password) {}

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtUtil jwt) {
        this.users = users; this.encoder = encoder; this.jwt = jwt;
    }

    @PostMapping("/register")
    public Map<String, String> register(@Valid @RequestBody Credentials c) {
        if (users.findByUsername(c.username()).isPresent())
            throw new IllegalArgumentException("Username already taken");
        AppUser u = new AppUser();
        u.username = c.username();
        u.password = encoder.encode(c.password());
        u.role = "ANALYST";
        users.save(u);
        return Map.of("message", "Registered with role ANALYST");
    }

    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody Credentials c) {
        AppUser u = users.findByUsername(c.username())
                .filter(x -> encoder.matches(c.password(), x.password))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        return Map.of("token", jwt.generate(u.username, u.role), "role", u.role);
    }
}
