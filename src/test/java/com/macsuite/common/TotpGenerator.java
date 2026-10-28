package com.macsuite.common;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.exceptions.CodeGenerationException;
import org.apache.commons.codec.binary.Base32;

import java.time.Instant;
import java.util.Locale;

public final class TotpGenerator {
    private static final String INVALID_SETUP_KEY_MESSAGE = "Salesforce TOTP secret is not valid Base32. "
        + "Replace the selected role/environment TOTP_SECRET or SALESFORCE_TOTP_SECRET with the exact "
        + "authenticator setup key registered for this user, not a verification code. "
        + "Do not edit padding or guess missing characters.";

    private TotpGenerator() {
    }

    public static String generateCurrentCode(String secret) {
        return generateCode(secret, Instant.now());
    }

    static String generateCode(String secret, Instant instant) {
        return generateCode(secret, instant, new DefaultCodeGenerator());
    }

    static String generateCode(String secret, Instant instant, CodeGenerator generator) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException("Salesforce TOTP secret is missing.");
        }
        String normalized = secret.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z2-7]{16,}={0,6}")) {
            throw new IllegalArgumentException(INVALID_SETUP_KEY_MESSAGE);
        }
        Base32 base32 = new Base32();
        String canonical;
        try {
            canonical = base32.encodeToString(base32.decode(normalized));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(INVALID_SETUP_KEY_MESSAGE);
        }
        if (!normalized.equals(canonical) && !normalized.equals(canonical.replace("=", ""))) {
            throw new IllegalArgumentException(INVALID_SETUP_KEY_MESSAGE);
        }
        try {
            return generator.generate(normalized, instant.getEpochSecond() / 30);
        } catch (CodeGenerationException exception) {
            Throwable failure = exception.getCause() == null ? exception : exception.getCause();
            throw new IllegalStateException("Unable to generate the Salesforce verification code ("
                + failure.getClass().getSimpleName() + "). Confirm the selected secret is the Base32 setup key "
                + "registered for this user. If the TOTP reference tests also fail, check the runner JDK's "
                + "HmacSHA1 provider and cryptographic policy. No secret values or underlying messages are logged.");
        }
    }
}