package com.company.vzvod.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncodingConfiguration {

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        // Supports {bcrypt} (default) and legacy prefixes like {noop} used in DB seeds.
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}

