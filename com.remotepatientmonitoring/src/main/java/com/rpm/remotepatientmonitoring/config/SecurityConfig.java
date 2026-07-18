package com.rpm.remotepatientmonitoring.config;

import com.rpm.remotepatientmonitoring.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder builder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        builder.userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder());
        return builder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(exceptions -> exceptions
                .accessDeniedHandler(new CustomAccessDeniedHandler())
            )
            .authorizeHttpRequests(auth -> auth
                // Public: auth pages, static files, setup tool (xóa /setup/** sau khi setup xong)
                .requestMatchers("/auth/**", "/css/**", "/js/**", "/images/**", "/setup/**", "/uploads/**").permitAll()

                // Trang Thymeleaf theo role
                .requestMatchers("/hospital/**").hasRole("HOSPITAL_ADMIN")
                .requestMatchers("/doctor/**").hasRole("DOCTOR")
                .requestMatchers("/patient/**").hasRole("PATIENT")

                // REST API theo role
                .requestMatchers("/api/doctor/**").hasRole("DOCTOR")
                .requestMatchers("/api/hospital/**").hasRole("HOSPITAL_ADMIN")
                .requestMatchers("/api/patient/**").hasRole("PATIENT")

                // Dashboard điều hướng — ai đăng nhập đều vào được
                .requestMatchers("/dashboard").authenticated()

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .usernameParameter("username")  // khớp name="username" trong HTML form
                .passwordParameter("password")  // khớp name="password" trong HTML form
                .defaultSuccessUrl("/dashboard", true)
                .failureHandler((request, response, exception) -> {
                    String username = request.getParameter("username");
                    String encodedUsername = java.net.URLEncoder.encode(
                            username != null ? username : "", java.nio.charset.StandardCharsets.UTF_8);
                    if (exception instanceof org.springframework.security.authentication.DisabledException) {
                        response.sendRedirect("/auth/login?disabled=true&username=" + encodedUsername);
                    } else {
                        response.sendRedirect("/auth/login?error=true&username=" + encodedUsername);
                    }
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );
        return http.build();
    }
}