package com.rpm.remotepatientmonitoring;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("[TC-01-AIChatbot] Playwright E2E: General Doctor AI Chatbot Assistant on Dashboard")
public class AiChatbotServiceAutomationTest {

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
    @DisplayName("[TC-01-AIChatbot] E2E: Login -> Doctor Dashboard -> Direct AI Chatbot Consulting (10min Delay)")
    void testDoctorChatbotResponseSuccess() {
        // Step 1: Open Login Page
        page.navigate("http://localhost:8080/auth/login");
        assertTrue(page.isVisible("#username"), "Username field must be visible");

        // Step 2: Fill credentials & Submit Login
        page.fill("#username", "doctor1@hospital.vn");
        page.fill("#password", "123456");
        page.click("button[type='submit']");

        // Step 3: Wait until Dashboard is loaded
        page.waitForURL("**/doctor/dashboard**");
        assertTrue(page.url().contains("/doctor/dashboard"), "Must redirect to Doctor Dashboard");

        // Step 4: Click Floating AI Chatbot Brain Button (#aiChatBtn) directly on Dashboard
        page.locator("#aiChatBtn").scrollIntoViewIfNeeded();
        page.click("#aiChatBtn");

        // Step 5: Verify AI Chatbot Window opens
        page.waitForSelector("#aiChatWindow");
        assertTrue(page.isVisible("#aiChatWindow"), "AI Chat Window must open");

        // Step 6: Enter Medical Question into AI Chat Input (#aiChatInput)
        page.fill("#aiChatInput", "Bệnh nhân đường huyết cao nên xử lý sao?");

        // Step 7: Click Send Button (#aiChatSend)
        page.click("#aiChatSend");

        // Step 8: Đợi 10 phút (600.000 ms) cho Bác sĩ quan sát câu trả lời phác đồ y khoa tổng quát của AI
        page.waitForTimeout(600000);
    }
}
