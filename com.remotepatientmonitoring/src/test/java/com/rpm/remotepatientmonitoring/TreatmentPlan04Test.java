package com.rpm.remotepatientmonitoring;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TreatmentPlan04Test {

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
    void testDoctorCanViewExistingTreatmentPlan() {
        // Step 1: Open browser and navigate to login
        page.navigate("http://localhost:" + port + "/auth/login");
        
        // Step 2 & 3: Login as a doctor (Assume admin@bvdktrunguong-mau.vn has patients)
        page.fill("#username", "admin@bvdktrunguong-mau.vn");
        page.fill("#password", "123456");
        page.click("button[type='submit']");
        
        // Wait for dashboard
        page.waitForURL("**/doctor/**");
        
        // Step 4: Go to patient list
        page.navigate("http://localhost:" + port + "/doctor/patients");
        
        // Step 5: Click on a patient (assuming there is at least one patient link in the table)
        // Wait for table to load
        page.waitForSelector("table tbody tr");
        
        // Click the first patient's details link
        page.locator("table tbody tr:first-child a.btn-info").click(); // Adjust selector based on UI
        
        // Step 6: Verify patient detail page is open
        assertTrue(page.url().contains("/doctor/patients/"), "Should navigate to patient details");
        
        // Step 7: Verify Treatment plan section is available and shows data
        // Assume fields are populated if they exist
        boolean isPlanVisible = page.locator("input[name='targetWeightKg']").isVisible();
        assertTrue(isPlanVisible, "Treatment plan fields should be visible on patient detail page");
    }
}
