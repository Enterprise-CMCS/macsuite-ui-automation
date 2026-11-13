package com.macsuite.config;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class CredentialProviderTest {
    @DataProvider
    public Object[][] roles() {
        return new Object[][] {
                {"BCT", "BCT"},
                {"BCT-DSG-CRU", "BCT_DSG_CRU"},
                {"bct-dsg-osg-read", "BCT_DSG_OSG_READ"},
                {"OTHER-ROLE", "OTHER-ROLE"}
        };
    }

    @Test(dataProvider = "roles")
    public void resolvesCredentialsWithExpectedRoleKey(String role, String credentialRole) {
        EnvLoader.load();
        String usernameKey = credentialRole + "_CREDENTIAL_TEST_USERNAME";
        String passwordKey = credentialRole + "_CREDENTIAL_TEST_PASSWORD";
        String totpKey = role.toUpperCase() + "_CREDENTIAL_TEST_TOTP_SECRET";
        System.setProperty(usernameKey, "test-user");
        System.setProperty(passwordKey, "test-password");
        System.setProperty(totpKey, "test-totp");
        try {
            CredentialProvider.Credentials credentials = CredentialProvider.getCredentials(role, "credential_test");
            Assert.assertEquals(credentials.getUsername(), "test-user");
            Assert.assertEquals(credentials.getPassword(), "test-password");
            Assert.assertEquals(credentials.getTotpSecret(), "test-totp");
        } finally {
            System.clearProperty(usernameKey);
            System.clearProperty(passwordKey);
            System.clearProperty(totpKey);
        }
    }
}
