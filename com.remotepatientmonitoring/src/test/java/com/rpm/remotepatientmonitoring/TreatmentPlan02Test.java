package com.rpm.remotepatientmonitoring;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TreatmentPlan02Test {

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
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @BeforeEach
    void setUp() {
        context = browser.newContext();
        page = context.newPage();
    }

    @AfterEach
    void tearDown() {
        if (context != null) context.close();
    }

    @Test
    void testDoctorCannotCreateTreatmentPlanWithNegativeWeight() {
        page.navigate("http://localhost:" + port + "/auth/login");
        
        page.fill("#username", "admin@bvdktrunguong-mau.vn");
        page.fill("#password", "123456");
        page.click("button[type='submit']");
        page.waitForURL("**/doctor/**");
        
        page.navigate("http://localhost:" + port + "/doctor/patients");
        page.waitForSelector("table tbody tr");
        page.locator("table tbody tr:first-child a.btn-info").click();
        
        // Fill form field with negative weight
        page.fill("input[name='targetWeightKg']", "-10");
        page.click("form[action*='/treatment'] button[type='submit']");
        
        // HTML5 Validation or server error
        boolean isInvalid = (Boolean) page.evaluate("document.querySelector(\"input[name='targetWeightKg']\").validity.rangeUnderflow")
                || page.locator(".alert-danger").isVisible();
        
        assertTrue(isInvalid, "Validation error should be triggered for negative weight");
    }
}
