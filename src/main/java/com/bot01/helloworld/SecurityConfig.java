package com.bot01.helloworld;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/webhook")
            )

            .authorizeHttpRequests(auth -> auth

                // WhatsApp webhook must remain public
                .requestMatchers("/webhook").permitAll()

                // Login page must remain public
                .requestMatchers("/admin/login").permitAll()

                // Everything under admin is protected
                .requestMatchers("/admin/**").authenticated()

                // Home page
                .requestMatchers("/").permitAll()

                // Everything else
                .anyRequest().permitAll()
            )

            .formLogin(form -> form
                .loginPage("/admin/login")
                .loginProcessingUrl("/admin/login")
                .defaultSuccessUrl("/admin", true)
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/admin/logout")
                .logoutSuccessUrl("/admin/login")
                .permitAll()
            );

        return http.build();
    }
}
