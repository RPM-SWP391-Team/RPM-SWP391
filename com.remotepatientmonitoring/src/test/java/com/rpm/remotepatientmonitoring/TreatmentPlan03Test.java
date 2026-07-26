package com.rpm.remotepatientmonitoring;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TreatmentPlan03Test {

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
        context = browser.newContext(new Browser.NewContextOptions().setViewportSize(1920, 1080));
        page = context.newPage();
    }

    @AfterEach
    void tearDown() {
        if (context != null) context.close();
    }

    @Test
    void testDoctorCannotCreateTreatmentPlanExceedingMaxLength() {
        page.navigate("http://localhost:" + port + "/auth/login");
        
        page.fill("#username", "admin@bvdktrunguong-mau.vn");
        page.fill("#password", "123456");
        page.click("button[type='submit']");
        page.waitForURL("**/doctor/**");
        
        page.navigate("http://localhost:" + port + "/doctor/patients");
        page.waitForSelector("table tbody tr");
        page.locator("table tbody tr:first-child a.btn-info").click();
        
        // Generate 501 chars string
        String longText = "A".repeat(501);
        
        page.fill("input[name='targetWeightKg']", "60");
        page.fill("input[name='treatmentNotes']", longText);
        page.click("form[action*='/treatment'] button[type='submit']");
        
        // Maxlength attribute validation or server error
        boolean isInvalid = (Boolean) page.evaluate("document.querySelector(\"input[name='treatmentNotes']\").validity.tooLong")
                || page.locator(".alert-danger").isVisible() 
                || (Boolean) page.evaluate("document.querySelector(\"input[name='treatmentNotes']\").value.length === 500"); // Auto truncated by browser
        
        assertTrue(isInvalid, "Validation error or input truncation should be triggered for too long text");
    }
}
