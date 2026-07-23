package com.rpm.remotepatientmonitoring;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class Login03Test {
    @LocalServerPort
    private int port;
    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    @AfterAll
    static void closeBrowser() {
        browser.close();
        playwright.close();
    }

    @BeforeEach
    void setUp() {
        context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1920, 1080));
        page = context.newPage();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void testLoginFailsWithInvalidEmailFormat() {
        page.navigate("http://localhost:" + port + "/auth/login");
        page.fill("#username", "invalid-email-format");
        page.fill("#password", "Password123");
        page.click("button[type='submit']");
        
        // HTML5 validation should trigger
        boolean isInvalid = (Boolean) page.evaluate("document.querySelector('#username').validity.typeMismatch");
        assertTrue(isInvalid, "The Email field should display a type validation message");
        assertTrue(page.url().contains("/auth/login"), "User should remain on the login page");
    }
}
