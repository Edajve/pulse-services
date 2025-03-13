package com.pulse.pulseservices.utils;

import java.util.UUID;

public class Util {
    public static String generateHash() {
        return UUID.randomUUID().toString();
    }
}
