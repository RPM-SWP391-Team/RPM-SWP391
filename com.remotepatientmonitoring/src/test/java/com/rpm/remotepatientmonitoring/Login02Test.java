package com.rpm.remotepatientmonitoring;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class Login02Test {

    @LocalServerPort
    private int port;

    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    private static final String TEST_PASSWORD = "123456";

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        try {
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false)
                    .setSlowMo(500)
                    .setChannel("chrome"));
        } catch (Exception e) {
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(false)
                    .setSlowMo(500)
                    .setChannel("msedge"));
        }
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void setUp() {
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    void testLoginFailsWhenMandatoryFieldsAreMissing() {
        // Step 1 & 2: Open browser and navigate to Login page
        page.navigate("http://localhost:" + port + "/auth/login");

        // Step 3 & 4: Leave Email blank, enter valid Password
        page.fill("#username", "");
        page.fill("#password", TEST_PASSWORD);

        // Step 5: Click Login button
        page.click("button[type='submit']");

        // Step 6: Observe validation messages (HTML5 validation should prevent submission)
        // Check if the username field has the HTML5 'required' pseudo-class or validation message
        boolean isRequired = (Boolean) page.evaluate("document.querySelector('#username').validity.valueMissing");
        assertTrue(isRequired, "The Email field should display a required validation message");

        // Step 7 & 8: Verify user remains on the Login page
        assertTrue(page.url().contains("/auth/login"), "User should remain on the login page");
    }
}
