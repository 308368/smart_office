//package com.cqf.common.utils;
//
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//
///**
// * BCrypt 密码加密工具类
// */
//public class BCryptUtil {
//
//    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();
//
//    /**
//     * 加密密码
//     *
//     * @param rawPassword 原始密码
//     * @return 加密后的密码
//     */
//    public static String encode(String rawPassword) {
//        return ENCODER.encode(rawPassword);
//    }
//
//    /**
//     * 验证密码是否匹配
//     *
//     * @param rawPassword     原始密码
//     * @param encodedPassword 加密后的密码
//     * @return 是否匹配
//     */
//    public static boolean matches(String rawPassword, String encodedPassword) {
//        return ENCODER.matches(rawPassword, encodedPassword);
//    }
//}
