package com.rpm.remotepatientmonitoring;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.SelectOption;
import org.junit.jupiter.api.*;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class DoctorManagementIT {

    static Playwright playwright;
    static Browser browser;

    BrowserContext context;
    Page page;

    // ============================================================
    // CẤU HÌNH CHUNG
    // ============================================================

    private static final String BASE_URL = "http://localhost:8080";
    private static final String LOGIN_URL = BASE_URL + "/auth/login";
    private static final String DOCTORS_URL = BASE_URL + "/hospital/doctors";

    private static final String ADMIN_EMAIL = "admin@bvdktrunguong-mau.vn";
    private static final String ADMIN_PASSWORD = "123456";

    // ============================================================
    // SELECTOR HELPER METHODS
    // ============================================================

    private Locator addDoctorButton() {
        return page.locator("button")
                .filter(new Locator.FilterOptions()
                        .setHasText("Thêm Bác Sĩ Mới"))
                .first();
    }

    private Locator doctorTableBody() {
        return page.locator("table tbody")
                .filter(new Locator.FilterOptions()
                        .setHasNot(page.locator("#detailPatientsTableBody")))
                .first();
    }

    private Locator createDoctorModal() {
        return page.locator("form")
                .filter(new Locator.FilterOptions()
                        .setHas(page.locator("#fullName")))
                .first();
    }

    private Locator createDoctorNameInput() {
        return createDoctorModal().locator("#fullName");
    }

    private Locator createDoctorPhoneInput() {
        return createDoctorModal().locator("#phone");
    }

    private Locator createDoctorEmailInput() {
        return createDoctorModal().locator("#email");
    }

    private Locator createDoctorCapacityInput() {
        return createDoctorModal().locator("input[type='number']").first();
    }

    private Locator createDoctorGenderSelect() {
        return createDoctorModal().locator("select").nth(0);
    }

    private Locator createDoctorSpecialtySelect() {
        return createDoctorModal().locator("select").nth(1);
    }

    private Locator createDoctorSubmitButton() {
        return createDoctorModal()
                .locator("button")
                .filter(new Locator.FilterOptions()
                        .setHasText("Lưu và Tạo Tài Khoản"))
                .first();
    }

    private String uniquePhone() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        return "09" + timestamp.substring(timestamp.length() - 8);
    }

    private String uniqueEmail(String prefix) {
        return prefix + System.currentTimeMillis() + "@benhvien.com";
    }

    // ============================================================
    // KHỞI TẠO VÀ DỌN DẸP
    // ============================================================

    @BeforeAll
    static void setupAll() {
        playwright = Playwright.create();

        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(false)
                        .setSlowMo(1000)
        );
    }

    @BeforeEach
    void setup() {
        context = browser.newContext(
                new Browser.NewContextOptions()
                        .setViewportSize(1600, 900)
        );

        page = context.newPage();
        page.setDefaultTimeout(15_000);

        page.navigate(LOGIN_URL);
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);

        page.getByPlaceholder("Nhập email đăng nhập").fill(ADMIN_EMAIL);
        page.getByPlaceholder("Nhập mật khẩu").fill(ADMIN_PASSWORD);

        page.locator("button[type='submit']").click();

        page.waitForURL(
                Pattern.compile(".*(/hospital/dashboard|/hospital/doctors).*"),
                new Page.WaitForURLOptions().setTimeout(15_000)
        );

        page.navigate(DOCTORS_URL);
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        doctorTableBody().waitFor();
    }

    // ============================================================
    // INTEGRATION TEST CASES
    // ============================================================

    @Test
    @DisplayName("[IT-DOC-01] Create New Doctor Successfully")
    void testCreateDoctorSuccessfully() {

        // 1. Mở form thêm bác sĩ
        addDoctorButton().click();
        createDoctorModal().waitFor();

        // 2. Chuẩn bị dữ liệu test
        String doctorName = "Nguyễn Văn Anh";
        String phone = uniquePhone();
        String email = uniqueEmail("doctor");

        // 3. Nhập thông tin bác sĩ
        createDoctorNameInput().fill(doctorName);
        createDoctorPhoneInput().fill(phone);
        createDoctorEmailInput().fill(email);
        createDoctorCapacityInput().fill("40");

        createDoctorGenderSelect().selectOption(
                new SelectOption().setLabel("Nam")
        );

        createDoctorSpecialtySelect().selectOption(
                new SelectOption().setLabel("Tiểu đường")
        );

        // 4. Lưu và Tạo Tài Khoản
        createDoctorSubmitButton().click();

        // 5. Chờ hệ thống xử lý và cập nhật UI
        page.waitForTimeout(1_000);
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        doctorTableBody().waitFor();

        // ===== Verify Integration =====

        // Verify 1: Thông báo thành công xuất hiện trên giao diện
        String bodyText = page.locator("body").innerText();
        assertTrue(
                bodyText.contains("Thành công") || bodyText.contains("thành công"),
                "Không hiển thị thông báo tạo bác sĩ thành công"
        );

        // Verify 2: Bác sĩ mới xuất hiện trong danh sách
        String tableText = doctorTableBody().innerText();
        assertTrue(
                tableText.contains(doctorName),
                "Không tìm thấy bác sĩ mới trong danh sách"
        );

        // Verify 3: Email hiển thị chính xác trong bảng
        assertTrue(
                tableText.contains(email),
                "Email bác sĩ không đúng trong bảng dữ liệu"
        );

        // Verify 4: Trạng thái mặc định là "Đang hoạt động"
        assertTrue(
                tableText.contains("Đang hoạt"),
                "Bác sĩ mới chưa ở trạng thái Đang hoạt động"
        );
    }

    @AfterEach
    void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @AfterAll
    static void tearDownAll() {
        if (browser != null) {
            browser.close();
        }

        if (playwright != null) {
            playwright.close();
        }
    }
}