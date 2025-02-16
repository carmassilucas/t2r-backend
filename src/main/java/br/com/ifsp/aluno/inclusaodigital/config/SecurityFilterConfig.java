package br.com.ifsp.aluno.inclusaodigital.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilterConfig extends OncePerRequestFilter {
    @Value("authentication.jwt.issuer")
    private String authenticateJwtIssuer;

    @Value("authentication.algorithm.secret")
    private String authenticateAlgorithmSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null) {
            filterChain.doFilter(request, response);
            return;
        }
        var verifyer = JWT.require(Algorithm.HMAC256(authenticateAlgorithmSecret))
                .withIssuer(authenticateJwtIssuer)
                .build();
        var token = verifyer.verify(header.replace("Bearer ", ""));
        if (token == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        request.setAttribute("interlocutor_id", token.getSubject());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(token.getSubject(), null, null)
        );
        filterChain.doFilter(request, response);
    }
}
