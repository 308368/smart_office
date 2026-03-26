package com.cqf.test;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestBCrypt {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "123456";
        String encoded = encoder.encode(rawPassword);
        System.out.println("加密后: " + encoded);
        System.out.println("验证结果: " + encoder.matches(rawPassword, encoded));
    }
}
