package com.pulse.pulseservices.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

public class Util {
    public static String generateHash() {
        try {
            // Generate a random unique identifier (UUID)
            String randomString = UUID.randomUUID().toString();

            // Create a SHA-256 MessageDigest instance
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Convert the string into bytes and compute the hash
            byte[] hashBytes = digest.digest(randomString.getBytes());

            // Encode the hash bytes to a Base64 string for storage
            return Base64.getEncoder().encodeToString(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating hash", e);
        }
    }
}
