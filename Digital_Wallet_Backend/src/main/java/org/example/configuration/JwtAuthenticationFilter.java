package org.example.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.authentication.service.JwtService;
import org.example.user.entity.User;
import org.example.user.repository.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        System.out.println("Authorization header present: " + (authHeader != null && authHeader.startsWith("Bearer ")));
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String email = jwtService.extractClaims(token).getSubject();

            if (email != null
                    && SecurityContextHolder.getContext()
                    .getAuthentication() == null
                    && jwtService.isTokenValid(token, email)) {

                User user = userRepository.findByEmail(email).orElse(null);

                if (user != null) {
                    var authentication =
                            new UsernamePasswordAuthenticationToken(
                                    email,
                                    null,
                                    List.of(new SimpleGrantedAuthority(
                                            "ROLE_" + user.getRole().name()))
                            );

                    SecurityContextHolder.getContext()
                            .setAuthentication(authentication);

                    System.out.println("JWT user found: " + user.getEmail());
                }else {
                    System.out.println("JWT user NOT found in database");
                }
            }
        } catch (io.jsonwebtoken.JwtException
                 | IllegalArgumentException ex) {
            System.out.println("JWT validation failed: "
                    + ex.getClass().getSimpleName());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
