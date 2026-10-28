package com.macsuite.common;

import dev.samstevens.totp.exceptions.CodeGenerationException;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

public class TotpGeneratorTest {
    private static final String RFC_SECRET = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

    @DataProvider
    public Object[][] referenceCodes() {
        return new Object[][] {
            {59L, "287082"},
            {1111111109L, "081804"},
            {1111111111L, "050471"},
            {1234567890L, "005924"},
            {2000000000L, "279037"},
            {20000000000L, "353130"}
        };
    }

    @Test(dataProvider = "referenceCodes")
    public void matchesRfc6238Sha1Vectors(long seconds, String expected) {
        Assert.assertEquals(TotpGenerator.generateCode(RFC_SECRET, Instant.ofEpochSecond(seconds)), expected);
    }

    @Test
    public void acceptsLowercaseSetupKey() {
        Assert.assertEquals(TotpGenerator.generateCode(RFC_SECRET.toLowerCase(java.util.Locale.ROOT),
            Instant.ofEpochSecond(59)), "287082");
    }

    @Test
    public void acceptsPaddedAndUnpaddedBase32() {
        String setupKey = "GEZDGNBVGY3TQOJQGEZDGNBVGY";

        Assert.assertEquals(TotpGenerator.generateCode(setupKey + "======", Instant.ofEpochSecond(59)),
            TotpGenerator.generateCode(setupKey, Instant.ofEpochSecond(59)));
    }

    @DataProvider
    public Object[][] malformedBase32Keys() {
        return new Object[][] {
            {RFC_SECRET + "A"},
            {RFC_SECRET + "AAA"},
            {RFC_SECRET + "AAAAAA"},
            {RFC_SECRET + "="},
            {"GEZDGNBVGY3TQOJQGZ"}
        };
    }

    @Test(dataProvider = "malformedBase32Keys")
    public void rejectsMalformedBase32WithoutDisclosingIt(String secret) {
        IllegalArgumentException failure = Assert.expectThrows(IllegalArgumentException.class,
            () -> TotpGenerator.generateCode(secret, Instant.ofEpochSecond(59)));

        Assert.assertTrue(failure.getMessage().contains("Base32"));
        Assert.assertFalse(failure.getMessage().contains(secret));
        Assert.assertNull(failure.getCause());
    }

    @Test
    public void generatesCodeForConfiguredCiKeyWithoutDisclosingIt() {
        String secret = System.getenv("MFP_UAT_TOTP_SECRET");
        if (secret == null || secret.isBlank()) {
            secret = System.getenv("SALESFORCE_TOTP_SECRET");
        }
        if (secret == null || secret.isBlank()) {
            Assert.assertNotEquals(System.getenv("GITHUB_ACTIONS"), "true",
                "GitHub Actions must supply MFP_UAT_TOTP_SECRET or SALESFORCE_TOTP_SECRET.");
            return;
        }

        String code = TotpGenerator.generateCurrentCode(secret);

        Assert.assertTrue(code.matches("[0-9]{6}"), "Generated authenticator code must be six ASCII digits.");
    }

    @Test
    public void rejectsMissingOrMalformedSecretWithoutDisclosingIt() {
        for (String secret : new String[] {null, "", " ", "invalid-secret!"}) {
            IllegalArgumentException failure = Assert.expectThrows(IllegalArgumentException.class,
                () -> TotpGenerator.generateCode(secret, Instant.ofEpochSecond(59)));
            Assert.assertFalse(failure.getMessage().contains("invalid-secret!"));
        }
    }

    @DataProvider
    public Object[][] generationFailures() {
        return new Object[][] {
            {new InvalidKeyException("sensitive-provider-detail"), "InvalidKeyException"},
            {new NoSuchAlgorithmException("sensitive-provider-detail"), "NoSuchAlgorithmException"},
            {new IllegalArgumentException("sensitive-provider-detail"), "IllegalArgumentException"}
        };
    }

    @Test(dataProvider = "generationFailures")
    public void reportsFailureTypeWithoutDisclosingProviderDetails(Exception providerFailure, String failureType) {
        IllegalStateException failure = Assert.expectThrows(IllegalStateException.class,
            () -> TotpGenerator.generateCode(RFC_SECRET, Instant.ofEpochSecond(59), (secret, counter) -> {
                throw new CodeGenerationException("sensitive-wrapper-detail", providerFailure);
            }));

        Assert.assertTrue(failure.getMessage().contains(failureType));
        Assert.assertFalse(failure.getMessage().contains("sensitive-provider-detail"));
        Assert.assertFalse(failure.getMessage().contains("sensitive-wrapper-detail"));
        Assert.assertFalse(failure.getMessage().contains(RFC_SECRET));
        Assert.assertNull(failure.getCause());
    }
}