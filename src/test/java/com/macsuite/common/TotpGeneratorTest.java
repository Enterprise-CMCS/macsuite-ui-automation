package com.macsuite.common;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

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
    public void rejectsMissingOrMalformedSecretWithoutDisclosingIt() {
        for (String secret : new String[] {null, "", " ", "invalid-secret!"}) {
            IllegalArgumentException failure = Assert.expectThrows(IllegalArgumentException.class,
                () -> TotpGenerator.generateCode(secret, Instant.ofEpochSecond(59)));
            Assert.assertFalse(failure.getMessage().contains("invalid-secret!"));
        }
    }
}