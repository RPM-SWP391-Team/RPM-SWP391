package com.rpm.remotepatientmonitoring;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TreatmentPlan01Test {

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
    void testDoctorCanCreateNewTreatmentPlan() {
        page.navigate("http://localhost:" + port + "/auth/login");
        
        page.fill("#username", "admin@bvdktrunguong-mau.vn");
        page.fill("#password", "123456");
        page.click("button[type='submit']");
        page.waitForURL("**/doctor/**");
        
        page.navigate("http://localhost:" + port + "/doctor/patients");
        page.waitForSelector("table tbody tr");
        page.locator("table tbody tr:first-child a.btn-info").click();
        assertTrue(page.url().contains("/doctor/patients/"));
        
        // Fill form fields
        page.fill("input[name='targetWeightKg']", "65");
        page.fill("input[name='exerciseGoal']", "Chạy bộ 30p");
        page.fill("input[name='treatmentNotes']", "Theo dõi huyết áp");
        
        // Submit form (Assuming there is a save button in that form)
        page.click("form[action*='/treatment'] button[type='submit']");
        
        // Verify success
        boolean hasAlert = page.locator(".alert-success").isVisible() || page.url().contains("success");
        assertTrue(hasAlert, "Success message should be displayed");
    }
}
