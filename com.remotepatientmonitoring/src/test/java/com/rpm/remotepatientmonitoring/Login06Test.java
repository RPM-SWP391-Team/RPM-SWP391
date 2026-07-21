package com.rpm.remotepatientmonitoring;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class Login06Test {
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
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void testLoginFailsWithLockedAccount() {
        page.navigate("http://localhost:" + port + "/auth/login");
        page.fill("#username", "lockeduser@example.com"); // Mock locked account email
        page.fill("#password", "Password123");
        page.click("button[type='submit']");
        
        // Expect an error message from the server indicating locked account
        page.waitForSelector(".alert-danger, .error-message");
        boolean hasError = page.locator(".alert-danger, .error-message").isVisible() || page.url().contains("error");
        assertTrue(hasError, "Should display an error message about locked account");
        assertTrue(page.url().contains("/auth/login"), "User should remain on the login page");
    }
}
