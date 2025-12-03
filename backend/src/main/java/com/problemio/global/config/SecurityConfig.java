package com.problemio.global.config;

import com.problemio.global.jwt.JwtAuthenticationFilter;
import com.problemio.global.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtTokenProvider jwtTokenProvider,
            UserDetailsService userDetailsService
    ) throws Exception {

        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Auth endpoints
                        .requestMatchers("/api/auth/signup", "/api/auth/login", "/api/auth/reissue").permitAll()

                        // Static/file access
                        .requestMatchers("/api/files/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()

                        // Comments: 조회/작성/수정/삭제는 게스트 허용, 좋아요는 로그인 필요
                        .requestMatchers(HttpMethod.GET, "/api/quizzes/*/comments").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/quizzes/*/comments").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/comments/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/comments/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/comments/*/likes").authenticated()

                        // Public quiz and submission endpoints
                        .requestMatchers(HttpMethod.GET, "/api/quizzes/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/quizzes/*/submissions").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/quizzes/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/quizzes/**").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/api/quizzes/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/quizzes/**").authenticated()
                        .requestMatchers("/api/submissions/**").permitAll()

                        // User lookups available to guests
                        .requestMatchers("/api/users/checkNickname").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/me", "/api/users/me/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/users/*/quizzes").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users/*").permitAll()

                        // Email verification
                        .requestMatchers("/api/auth/email/**").permitAll()

                        // Everything else requires auth
                        .requestMatchers("/api/users/**").authenticated()
                        .requestMatchers("/api/follows/**").authenticated()

                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
