package com.projectflow;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordHashingTest {

    @Test
    void bcryptDoesNotStorePlaintextPasswords() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hash = encoder.encode("Admin123!");
        assertThat(hash).isNotEqualTo("Admin123!");
        assertThat(hash).startsWith("$2");
        assertThat(encoder.matches("Admin123!", hash)).isTrue();
        assertThat(encoder.matches("wrong", hash)).isFalse();
    }
}
