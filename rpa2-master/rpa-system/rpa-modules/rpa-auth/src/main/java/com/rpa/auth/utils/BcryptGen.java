package com.rpa.auth.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BcryptGen {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String raw = args.length > 0 ? args[0] : "admin123";
        String hash = encoder.encode(raw);
        System.out.println("RAW:    " + raw);
        System.out.println("HASH:   " + hash);
        System.out.println("MATCH:  " + encoder.matches(raw, hash));
        System.out.println("SQL:    UPDATE sys_user SET password='" + hash + "' WHERE username='admin';");
    }
}
