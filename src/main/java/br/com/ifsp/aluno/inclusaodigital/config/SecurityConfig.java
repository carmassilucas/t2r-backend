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
    private final SecurityFilterInterConfig securityFilterInterConfig;
    private final CorsConfig corsConfig;

    public SecurityConfig(SecurityFilterInterConfig securityFilterInterConfig, CorsConfig corsConfig) {
        this.securityFilterInterConfig = securityFilterInterConfig;
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
                            .anyRequest().authenticated())
                .addFilterBefore(securityFilterInterConfig, AuthorizationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
