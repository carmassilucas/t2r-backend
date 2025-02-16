package br.com.ifsp.aluno.inclusaodigital.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    private final SecurityFilterConfig securityFilterConfig;
    private final CorsConfig corsConfig;

    public SecurityConfig(SecurityFilterConfig securityFilterConfig, CorsConfig corsConfig) {
        this.securityFilterConfig = securityFilterConfig;
        this.corsConfig = corsConfig;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(httpSecurityCorsConfigurer -> corsConfig.corsConfigurationSource())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorization) ->
                    authorization.requestMatchers(HttpMethod.POST, "/interlocutor").permitAll()
                            .requestMatchers(HttpMethod.POST, "/interlocutor/auth").permitAll()
                            .requestMatchers("/t2r-websocket/**").permitAll()
                            .anyRequest().authenticated())
                .addFilterBefore(securityFilterConfig, AuthorizationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
