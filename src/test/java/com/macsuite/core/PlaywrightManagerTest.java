package com.macsuite.core;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import javax.imageio.ImageIO;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.microsoft.playwright.Page;

public class PlaywrightManagerTest {
    @Test
    public void keepsDesktopViewportWithoutAnInteractiveDesktop() throws Exception {
        String previousHeadless = System.getProperty("HEADLESS");
        String previousSlowMode = System.getProperty("SLOW_MODE");
        Path authState = Files.createTempFile("viewport-auth-state-", ".json");
        try {
            System.setProperty("HEADLESS", "true");
            System.setProperty("SLOW_MODE", "0");
            PlaywrightManager.init("desktop-viewport-test");
            assertDesktopViewport(PlaywrightManager.page());

            PlaywrightManager.saveAuthState(authState.toString());
            PlaywrightManager.reinitContext(authState.toString());
            assertDesktopViewport(PlaywrightManager.page());

            Page popup = PlaywrightManager.page().waitForPopup(
                    () -> PlaywrightManager.page().evaluate("() => window.open('about:blank')"));
            try {
                assertDesktopViewport(popup);
            } finally {
                popup.close();
            }
        } finally {
            PlaywrightManager.close();
            restoreProperty("HEADLESS", previousHeadless);
            restoreProperty("SLOW_MODE", previousSlowMode);
            Files.deleteIfExists(authState);
        }
    }

    private void assertDesktopViewport(Page page) throws Exception {
        Assert.assertEquals(page.viewportSize().width, 1920);
        Assert.assertEquals(page.viewportSize().height, 1080);
        Assert.assertEquals(page.evaluate("() => [innerWidth, innerHeight, screen.width, screen.height]"),
                Arrays.asList(1920, 1080, 1920, 1080));
        Assert.assertEquals(page.evaluate("() => matchMedia('(min-width: 1440px)').matches"), true);
        var screenshot = ImageIO.read(new ByteArrayInputStream(page.screenshot()));
        Assert.assertEquals(screenshot.getWidth(), 1920);
        Assert.assertEquals(screenshot.getHeight(), 1080);
    }

    private void restoreProperty(String name, String previousValue) {
        if (previousValue == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, previousValue);
        }
    }
}