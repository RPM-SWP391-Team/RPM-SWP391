package com.rpm.remotepatientmonitoring;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PlaywrightHealthLogTest {

    @LocalServerPort
    private int port;

    private static Playwright playwright;
    private static Browser browser;
    private BrowserContext context;
    private Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true).setSlowMo(50));
    }

    @AfterAll
    static void closeBrowser() {
        playwright.close();
    }

    @BeforeEach
    void createContextAndPage() {
        context = browser.newContext(new Browser.NewContextOptions().setBaseURL("http://localhost:" + port).setViewportSize(1920, 1080));
        page = context.newPage();
    }

    @AfterEach
    void closeContext() {
        context.close();
    }

    private void login(String email, String password) {
        page.navigate("/auth/login");
        page.fill("#username", email);
        page.fill("#password", password);
        page.click("button[type='submit']");
        page.waitForURL(url -> url.contains("/patient/dashboard") || url.contains("/patient/appointments"));
    }

    // ==========================================
    // HYPERTENSION FLOW (domanht10@gmail.com)
    // ==========================================
    @Test
    @DisplayName("[TC-LOG-BP-01] Log Blood Pressure successfully with Morning")
    void testBpMorning() {
        login("domanht10@gmail.com", "12345678");
        page.navigate("/patient/log");

        page.click("#panelBpMilestone .ms-card:has-text('Buổi sáng')");
        page.fill("#systolicBp", "80");
        page.fill("#diastolicBp", "150");
        page.fill("#heartRate", "75");
        page.fill("#notesBp", "Khỏe");
        page.click("button:has-text('Lưu Huyết áp')");

        page.waitForURL(url -> url.contains("/patient/log?saveSuccess=true"));
        Locator alert = page.locator("#successAlert");
        assertThat(alert.isVisible()).isTrue();
        assertThat(alert.textContent()).contains("Thành công! Chỉ số sức khỏe của bạn đã được ghi nhận vào nhật ký.");

        // Verify in Appointments History
        page.navigate("/patient/appointments");
        page.click("#bp-history-tab");
        Locator firstRow = page.locator("#bp-history-pane table tbody tr").first();
        assertThat(firstRow.textContent()).contains("Buổi sáng");
        assertThat(firstRow.textContent()).contains("80 mmHg");
        assertThat(firstRow.textContent()).contains("150 mmHg");
        assertThat(firstRow.textContent()).contains("75 bpm");
        assertThat(firstRow.textContent()).contains("Khỏe");
        assertThat(firstRow.textContent()).contains("MANUAL");
    }

    @Test
    @DisplayName("[TC-LOG-BP-02] Log Blood Pressure successfully with Evening")
    void testBpEvening() {
        login("domanht10@gmail.com", "12345678");
        page.navigate("/patient/log");

        page.click("#panelBpMilestone .ms-card:has-text('Buổi tối')");
        page.fill("#systolicBp", "120");
        page.fill("#diastolicBp", "80");
        page.fill("#heartRate", "75");
        page.fill("#notesBp", "Khỏe");
        page.click("button:has-text('Lưu Huyết áp')");

        page.waitForURL(url -> url.contains("/patient/log?saveSuccess=true"));
        Locator alert = page.locator("#successAlert");
        assertThat(alert.isVisible()).isTrue();

        page.navigate("/patient/appointments");
        page.click("#bp-history-tab");
        Locator firstRow = page.locator("#bp-history-pane table tbody tr").first();
        assertThat(firstRow.textContent()).contains("Buổi tối");
        assertThat(firstRow.textContent()).contains("120 mmHg");
        assertThat(firstRow.textContent()).contains("80 mmHg");
        assertThat(firstRow.textContent()).contains("75 bpm");
    }

    @Test
    @DisplayName("[TC-LOG-BP-03] Log Blood Pressure successfully with Custom Date Time")
    void testBpCustom() {
        login("domanht10@gmail.com", "12345678");
        page.navigate("/patient/log");

        page.click("#panelBpMilestone .ms-card:has-text('Nhập tự do')");
        page.fill("#systolicBp", "120");
        page.fill("#diastolicBp", "80");
        page.fill("#heartRate", "75");
        page.fill("#notesBp", "Khỏe");
        page.click("button:has-text('Lưu Huyết áp')");

        page.waitForURL(url -> url.contains("/patient/log?saveSuccess=true"));
        Locator alert = page.locator("#successAlert");
        assertThat(alert.isVisible()).isTrue();

        page.navigate("/patient/appointments");
        page.click("#bp-history-tab");
        Locator firstRow = page.locator("#bp-history-pane table tbody tr").first();
        assertThat(firstRow.textContent()).contains("Nhập tự do");
        assertThat(firstRow.textContent()).contains("120 mmHg");
        assertThat(firstRow.textContent()).contains("80 mmHg");
        assertThat(firstRow.textContent()).contains("75 bpm");
    }

    // ==========================================
    // DIABETES FLOW (domanht13@gmail.com)
    // ==========================================
    @Test
    @DisplayName("[TC-LOG-GL-01] Log Blood Glucose successfully before breakfast")
    void testGlMorning() {
        login("domanht13@gmail.com", "12345678");
        page.navigate("/patient/log");

        page.click("#panelGlMilestone .ms-card:has-text('Trước ăn sáng')");
        page.fill("#glucoseLevel", "5.8");
        page.fill("#notesGl", "Đo lúc 6h30");
        page.click("button:has-text('Lưu Đường huyết')");

        page.waitForURL(url -> url.contains("/patient/log?saveSuccess=true"));
        Locator alert = page.locator("#successAlert");
        assertThat(alert.isVisible()).isTrue();
        assertThat(alert.textContent()).contains("Thành công! Chỉ số sức khỏe của bạn đã được ghi nhận vào nhật ký.");

        page.navigate("/patient/appointments");
        page.click("#glucose-history-tab");
        Locator firstRow = page.locator("#glucose-history-pane table tbody tr").first();
        assertThat(firstRow.textContent()).contains("Trước ăn sáng");
        assertThat(firstRow.textContent()).contains("5.80 mmol/L");
        assertThat(firstRow.textContent()).contains("Đo lúc 6h30");
        assertThat(firstRow.textContent()).contains("MANUAL");
    }

    @Test
    @DisplayName("[TC-LOG-GL-02] Log Blood Glucose successfully 2 hours after dinner")
    void testGlEvening() {
        login("domanht13@gmail.com", "12345678");
        page.navigate("/patient/log");

        page.click("#panelGlMilestone .ms-card:has-text('Sau ăn tối 2h')");
        page.fill("#glucoseLevel", "7.2");
        page.fill("#notesGl", "Đo lúc 20h");
        page.click("button:has-text('Lưu Đường huyết')");

        page.waitForURL(url -> url.contains("/patient/log?saveSuccess=true"));
        Locator alert = page.locator("#successAlert");
        assertThat(alert.isVisible()).isTrue();

        page.navigate("/patient/appointments");
        page.click("#glucose-history-tab");
        Locator firstRow = page.locator("#glucose-history-pane table tbody tr").first();
        assertThat(firstRow.textContent()).contains("Sau ăn tối 2h");
        assertThat(firstRow.textContent()).contains("7.20 mmol/L");
        assertThat(firstRow.textContent()).contains("Đo lúc 20h");
    }

    @Test
    @DisplayName("[TC-LOG-GL-03] Log Blood Glucose successfully with Custom Date Time")
    void testGlCustom() {
        login("domanht13@gmail.com", "12345678");
        page.navigate("/patient/log");

        page.click("#panelGlMilestone .ms-card:has-text('Nhập tự do')");
        page.fill("#glucoseLevel", "6.6");
        page.fill("#notesGl", "02/07/2026 21:30");
        page.click("button:has-text('Lưu Đường huyết')");

        page.waitForURL(url -> url.contains("/patient/log?saveSuccess=true"));
        Locator alert = page.locator("#successAlert");
        assertThat(alert.isVisible()).isTrue();

        page.navigate("/patient/appointments");
        page.click("#glucose-history-tab");
        Locator firstRow = page.locator("#glucose-history-pane table tbody tr").first();
        assertThat(firstRow.textContent()).contains("Nhập tự do");
        assertThat(firstRow.textContent()).contains("6.60 mmol/L");
        assertThat(firstRow.textContent()).contains("02/07/2026 21:30");
    }

    // ==========================================
    // COMORBID FLOW (domanht7@gmail.com)
    // ==========================================
    @Test
    @DisplayName("[TC-LOG-COM-01] Log Blood Pressure successfully with Morning for Comorbid")
    void testComBpMorning() {
        login("domanht7@gmail.com", "12345678");
        page.navigate("/patient/log");

        page.click(".cat-card:has-text('Huyết áp')");
        page.click("#panelBpMilestone .ms-card:has-text('Buổi sáng')");

        page.fill("#systolicBp", "120");
        page.fill("#diastolicBp", "80");
        page.fill("#heartRate", "75");
        page.fill("#notesBp", "Khỏe");
        page.click("button:has-text('Lưu Huyết áp')");

        page.waitForURL(url -> url.contains("/patient/log?saveSuccess=true"));
        Locator alert = page.locator("#successAlert");
        assertThat(alert.isVisible()).isTrue();
    }

    @Test
    @DisplayName("[TC-LOG-COM-02] Log Blood Pressure successfully with Evening for Comorbid")
    void testComBpEvening() {
        login("domanht7@gmail.com", "12345678");
        page.navigate("/patient/log");

        page.click(".cat-card:has-text('Huyết áp')");
        page.click("#panelBpMilestone .ms-card:has-text('Buổi tối')");

        page.fill("#systolicBp", "120");
        page.fill("#diastolicBp", "80");
        page.fill("#heartRate", "75");
        page.fill("#notesBp", "Khỏe");
        page.click("button:has-text('Lưu Huyết áp')");

        page.waitForURL(url -> url.contains("/patient/log?saveSuccess=true"));
        Locator alert = page.locator("#successAlert");
        assertThat(alert.isVisible()).isTrue();
    }

    @Test
    @DisplayName("[TC-LOG-COM-03] Log Blood Pressure successfully with Custom Date Time for Comorbid")
    void testComBpCustom() {
        login("domanht7@gmail.com", "12345678");
        page.navigate("/patient/log");

        page.click(".cat-card:has-text('Huyết áp')");
        page.click("#panelBpMilestone .ms-card:has-text('Nhập tự do')");

        page.fill("#systolicBp", "120");
        page.fill("#diastolicBp", "80");
        page.fill("#heartRate", "75");
        page.fill("#notesBp", "Khỏe");
        page.click("button:has-text('Lưu Huyết áp')");

        page.waitForURL(url -> url.contains("/patient/log?saveSuccess=true"));
        Locator alert = page.locator("#successAlert");
        assertThat(alert.isVisible()).isTrue();
    }

    @Test
    @DisplayName("[TC-LOG-COM-04] Log Blood Glucose successfully before breakfast for Comorbid")
    void testComGlMorning() {
        login("domanht7@gmail.com", "12345678");
        page.navigate("/patient/log");

        page.click(".cat-card:has-text('Đường huyết')");
        page.click("#panelGlMilestone .ms-card:has-text('Trước ăn sáng')");

        page.fill("#glucoseLevel", "5.8");
        page.fill("#notesGl", "Đo lúc 6h30");
        page.click("button:has-text('Lưu Đường huyết')");

        page.waitForURL(url -> url.contains("/patient/log?saveSuccess=true"));
        Locator alert = page.locator("#successAlert");
        assertThat(alert.isVisible()).isTrue();
    }

    @Test
    @DisplayName("[TC-LOG-COM-05] Log Blood Glucose successfully 2 hours after dinner for Comorbid")
    void testComGlEvening() {
        login("domanht7@gmail.com", "12345678");
        page.navigate("/patient/log");

        page.click(".cat-card:has-text('Đường huyết')");
        page.click("#panelGlMilestone .ms-card:has-text('Sau ăn tối 2h')");

        page.fill("#glucoseLevel", "7.2");
        page.fill("#notesGl", "Đo lúc 20h");
        page.click("button:has-text('Lưu Đường huyết')");

        page.waitForURL(url -> url.contains("/patient/log?saveSuccess=true"));
        Locator alert = page.locator("#successAlert");
        assertThat(alert.isVisible()).isTrue();
    }

    @Test
    @DisplayName("[TC-LOG-COM-06] Log Blood Glucose successfully with Custom Date Time for Comorbid")
    void testComGlCustom() {
        login("domanht7@gmail.com", "12345678");
        page.navigate("/patient/log");

        page.click(".cat-card:has-text('Đường huyết')");
        page.click("#panelGlMilestone .ms-card:has-text('Nhập tự do')");

        page.fill("#glucoseLevel", "6.6");
        page.fill("#notesGl", "02/07/2026 21:30");
        page.click("button:has-text('Lưu Đường huyết')");

        page.waitForURL(url -> url.contains("/patient/log?saveSuccess=true"));
        Locator alert = page.locator("#successAlert");
        assertThat(alert.isVisible()).isTrue();
    }

    // ==========================================
    // VALIDATION FLOW
    // ==========================================
    @Test
    @DisplayName("[TC-LOG-VAL-01] Log BP fails when Systolic is empty")
    void testValBpEmptySystolic() {
        login("domanht10@gmail.com", "12345678");
        page.navigate("/patient/log");
        page.click("#panelBpMilestone .ms-card:has-text('Buổi sáng')");

        page.fill("#diastolicBp", "80");
        page.fill("#heartRate", "75");
        page.fill("#notesBp", "Đo tại nhà");

        page.onceDialog(dialog -> {
            assertThat(dialog.message()).isEqualTo("Vui lòng nhập chỉ số Huyết áp (Tâm thu và Tâm trương).");
            try { Thread.sleep(3000); } catch (InterruptedException e) {}
            dialog.dismiss();
        });

        page.click("button:has-text('Lưu Huyết áp')");
        assertThat(page.locator("#panelBpInput").isVisible()).isTrue();
    }

    @Test
    @DisplayName("[TC-LOG-VAL-02] Log BP fails when Systolic is out of range")
    void testValBpSystolicOutOfRange() {
        login("domanht10@gmail.com", "12345678");
        page.navigate("/patient/log");
        page.click("#panelBpMilestone .ms-card:has-text('Buổi sáng')");

        page.fill("#systolicBp", "45");
        page.fill("#diastolicBp", "80");
        page.fill("#heartRate", "75");
        page.fill("#notesBp", "Đo tại nhà");

        page.onceDialog(dialog -> {
            assertThat(dialog.message()).isEqualTo("Chỉ số tâm thu phải từ 50 đến 300 mmHg.");
            try { Thread.sleep(3000); } catch (InterruptedException e) {}
            dialog.dismiss();
        });

        page.click("button:has-text('Lưu Huyết áp')");
        assertThat(page.locator("#panelBpInput").isVisible()).isTrue();
    }

    @Test
    @DisplayName("[TC-LOG-VAL-03] Log BP fails when Diastolic is out of range")
    void testValBpDiastolicOutOfRange() {
        login("domanht10@gmail.com", "12345678");
        page.navigate("/patient/log");
        page.click("#panelBpMilestone .ms-card:has-text('Buổi sáng')");

        page.fill("#systolicBp", "120");
        page.fill("#diastolicBp", "25");
        page.fill("#heartRate", "75");
        page.fill("#notesBp", "Đo tại nhà");

        page.onceDialog(dialog -> {
            assertThat(dialog.message()).isEqualTo("Chỉ số tâm trương phải từ 30 đến 200 mmHg.");
            try { Thread.sleep(3000); } catch (InterruptedException e) {}
            dialog.dismiss();
        });

        page.click("button:has-text('Lưu Huyết áp')");
        assertThat(page.locator("#panelBpInput").isVisible()).isTrue();
    }

    @Test
    @DisplayName("[TC-LOG-VAL-04] Log BP fails when Heart Rate is out of range")
    void testValBpHeartRateOutOfRange() {
        login("domanht10@gmail.com", "12345678");
        page.navigate("/patient/log");
        page.click("#panelBpMilestone .ms-card:has-text('Buổi sáng')");

        page.fill("#systolicBp", "120");
        page.fill("#diastolicBp", "80");
        page.fill("#heartRate", "15");
        page.fill("#notesBp", "Đo tại nhà");

        page.onceDialog(dialog -> {
            assertThat(dialog.message()).isEqualTo("Nhịp tim phải từ 20 đến 300 lần/phút.");
            try { Thread.sleep(3000); } catch (InterruptedException e) {}
            dialog.dismiss();
        });

        page.click("button:has-text('Lưu Huyết áp')");
        assertThat(page.locator("#panelBpInput").isVisible()).isTrue();
    }

    @Test
    @DisplayName("[TC-LOG-VAL-05] Log Glucose fails when Glucose is empty")
    void testValGlEmpty() {
        login("domanht13@gmail.com", "12345678");
        page.navigate("/patient/log");
        page.click("#panelGlMilestone .ms-card:has-text('Trước ăn sáng')");

        page.fill("#notesGl", "Đo lúc đói");

        page.onceDialog(dialog -> {
            assertThat(dialog.message()).isEqualTo("Vui lòng nhập chỉ số Đường huyết.");
            try { Thread.sleep(3000); } catch (InterruptedException e) {}
            dialog.dismiss();
        });

        page.click("button:has-text('Lưu Đường huyết')");
        assertThat(page.locator("#panelGlInput").isVisible()).isTrue();
    }

    @Test
    @DisplayName("[TC-LOG-VAL-06] Log Glucose fails when Glucose is out of range")
    void testValGlOutOfRange() {
        login("domanht13@gmail.com", "12345678");
        page.navigate("/patient/log");
        page.click("#panelGlMilestone .ms-card:has-text('Trước ăn sáng')");

        page.fill("#glucoseLevel", "0.5");
        page.fill("#notesGl", "Đo lúc đói");

        page.onceDialog(dialog -> {
            assertThat(dialog.message()).isEqualTo("Chỉ số Đường huyết phải từ 1.0 đến 33.3 mmol/L.");
            try { Thread.sleep(3000); } catch (InterruptedException e) {}
            dialog.dismiss();
        });

        page.click("button:has-text('Lưu Đường huyết')");
        assertThat(page.locator("#panelGlInput").isVisible()).isTrue();
    }
}
