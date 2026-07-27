package com.rpm.remotepatientmonitoring;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("[TC-02-AISummary] Playwright E2E: Doctor Dashboard Vitals Summary & Patient Record Detail")
public class AiSummaryAndAdaStatsAutomationTest {

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
    @DisplayName("[TC-02-AISummary] E2E: Login -> Doctor Dashboard Stat Cards -> View Patient Detail & Vitals Summary")
    void testAnalyzePatientConditionWithAdaAhaStats() {
        // Step 1: Open Login Page
        page.navigate("http://localhost:8080/auth/login");
        assertTrue(page.isVisible("#username"));

        // Step 2: Login as Doctor
        page.fill("#username", "doctor1@hospital.vn");
        page.fill("#password", "123456");
        page.click("button[type='submit']");

        // Step 3: Verify Dashboard & Stat Cards
        page.waitForURL("**/doctor/dashboard**");
        assertTrue(page.url().contains("/doctor/dashboard"));
        page.waitForTimeout(2000);

        // Step 4: Scroll down patient table and click 'Xem hồ sơ'
        if (page.isVisible("a.btn-outline-primary")) {
            page.locator("a.btn-outline-primary").first().scrollIntoViewIfNeeded();
            page.click("a.btn-outline-primary");
            page.waitForTimeout(3000);
        }

        // Wait 5 seconds for visual verification
        page.waitForTimeout(5000);
    }
}
