package com.rpm.remotepatientmonitoring;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class Login01Test {

    @LocalServerPort
    private int port;

    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    // Use an existing test user or create one. Assuming 'admin@bvdktrunguong-mau.vn' exists based on mock data.
    private static final String TEST_EMAIL = "admin@rpm.com";
    private static final String TEST_PASSWORD = "123456"; // Assuming this is the password for the mocked data

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
    void testUserCanLoginSuccessfully() {
        // Step 1 & 2: Open browser and navigate to Login page
        page.navigate("http://localhost:" + port + "/auth/login");

        // Step 3 & 4: Enter valid Email and Password
        page.fill("#username", TEST_EMAIL);
        page.fill("#password", TEST_PASSWORD);

        // Step 5: Click Login button
        page.click("button[type='submit']");

        // Step 6 & 7 & 8: Wait for system authentication and verify Dashboard is displayed
        page.waitForURL("**/dashboard**");
        
        // Assert that we are on the dashboard
        assertTrue(page.url().contains("/dashboard"), "User should be redirected to dashboard");
    }
}
