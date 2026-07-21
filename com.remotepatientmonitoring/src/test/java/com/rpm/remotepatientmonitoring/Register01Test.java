package com.rpm.remotepatientmonitoring;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class Register01Test {

    @LocalServerPort
    private int port;

    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

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
    void testRegisterPageUI() {
        // Procedure:
        // 1. Open browser.
        // 2. Navigate to Register page.
        page.navigate("http://localhost:" + port + "/auth/register");

        // Expected Output matching user specification exactly:
        // Registter page is displayed with following fields:
        // -Textbox: email
        // -Textbox: password
        // -Textbox: confirm password
        // - Textbox: fullname
        // - Textbox: phone
        // - DateTime box: dob
        // - Selection box: gender
        // - Button "Đăng ký tài khoản"

        // Asserting all required UI elements are visible on the Register page
        assertTrue(page.locator("#email").isVisible(), "Textbox: email is not displayed");
        assertTrue(page.locator("#password").isVisible(), "Textbox: password is not displayed");
        assertTrue(page.locator("#confirmPassword").isVisible(), "Textbox: confirm password is not displayed");
        assertTrue(page.locator("#fullName").isVisible(), "Textbox: fullname is not displayed");
        assertTrue(page.locator("#phone").isVisible(), "Textbox: phone is not displayed");
        assertTrue(page.locator("#dateOfBirth").isVisible(), "DateTime box: dob is not displayed");
        assertTrue(page.locator("#gender").isVisible(), "Selection box: gender is not displayed");
        
        // Asserting the submit button with specific text
        Locator registerBtn = page.locator("button:has-text('Đăng ký tài khoản')");
        assertTrue(registerBtn.isVisible(), "Button 'Đăng ký tài khoản' is not displayed");
        
        System.out.println("Expected Output verified: Registter page is displayed with following fields:");
        System.out.println("-Textbox: email");
        System.out.println("-Textbox: password");
        System.out.println("-Textbox: confirm password");
        System.out.println("- Textbox: fullname");
        System.out.println("- Textbox: phone");
        System.out.println("- DateTime box: dob");
        System.out.println("- Selection box: gender");
        System.out.println("- Button \"Đăng ký tài khoản\"");
    }
}
