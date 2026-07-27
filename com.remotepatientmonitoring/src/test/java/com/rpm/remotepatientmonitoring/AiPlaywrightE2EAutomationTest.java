package com.rpm.remotepatientmonitoring;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("[TC-04-AIPlaywright] Playwright E2E: Interactive Search & Patient Detail Navigation")
public class AiPlaywrightE2EAutomationTest {

    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(1000)
        );
    }

    @AfterAll
    static void closeBrowser() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void closeContext() {
        if (context != null) context.close();
    }

    @Test
    @DisplayName("[TC-04-AIPlaywright] E2E: Login -> Patient List Keyword Search -> View Patient Record")
    void testAiChatQueryInteraction() {
        // Step 1: Open Login Page
        page.navigate("http://localhost:8080/auth/login");
        assertTrue(page.isVisible("#username"));

        // Step 2: Login as Doctor
        page.fill("#username", "doctor1@hospital.vn");
        page.fill("#password", "123456");
        page.click("button[type='submit']");

        // Step 3: Wait for Dashboard
        page.waitForURL("**/doctor/dashboard**");
        assertTrue(page.url().contains("/doctor/dashboard"));

        // Step 4: Perform Patient List Keyword Search
        if (page.isVisible("input[name='keyword']")) {
            page.fill("input[name='keyword']", "Lê");
            page.click("button:has-text('Tìm')");
            page.waitForTimeout(2000);
        }

        // Step 5: Click 'Xem hồ sơ' of first matching patient
        if (page.isVisible("a.btn-outline-primary")) {
            page.click("a.btn-outline-primary");
            page.waitForTimeout(3000);
        }

        // Wait 5 seconds for visual verification
        page.waitForTimeout(5000);
    }
}
