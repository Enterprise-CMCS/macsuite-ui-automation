package com.macsuite.common;

import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.exceptions.CodeGenerationException;

import java.time.Instant;
import java.util.Locale;

public final class TotpGenerator {
    private TotpGenerator() {
    }

    public static String generateCurrentCode(String secret) {
        return generateCode(secret, Instant.now());
    }

    static String generateCode(String secret, Instant instant) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("Salesforce TOTP secret is missing.");
        }
        String normalized = secret.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z2-7]{16,}={0,6}")) {
            throw new IllegalArgumentException("Salesforce TOTP secret must be the Base32 authenticator setup key.");
        }
        try {
            return new DefaultCodeGenerator().generate(normalized, instant.getEpochSecond() / 30);
        } catch (CodeGenerationException exception) {
            throw new IllegalStateException("Unable to generate the Salesforce verification code.");
        }
    }
}