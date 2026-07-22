package com.rpm.remotepatientmonitoring;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.SelectOption;
import org.junit.jupiter.api.*;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Test suite cho tính năng Quản lý Bác sĩ (Doctor Management).
 * Module: Hospital Admin
 * Chức năng: Thêm mới Bác sĩ
 */
public class DoctorManagementIT {

    static Playwright playwright;
    static Browser browser;

    BrowserContext context;
    Page page;

    // ============================================================
    // CẤU HÌNH HỆ THỐNG & TÀI KHOẢN
    // ============================================================
    private static final String BASE_URL = "http://localhost:8080";
    private static final String LOGIN_URL = BASE_URL + "/auth/login";
    private static final String DOCTORS_URL = BASE_URL + "/hospital/doctors";

    private static final String ADMIN_EMAIL = "lethang162005@gmail.com";
    private static final String ADMIN_PASSWORD = "123456";

    // ============================================================
    // HELPER LOCATORS
    // Lưu ý: Nếu UI thực tế có ID/CSS Selector khác, hãy cập nhật tại đây.
    // ============================================================

    private Locator loginEmailInput() {
        return page.locator("input[name='email'], input[type='email'], input[placeholder*='email']").first();
    }

    private Locator loginPasswordInput() {
        return page.locator("input[name='password'], input[type='password']").first();
    }

    private Locator loginSubmitButton() {
        return page.locator("button[type='submit']").first();
    }

    private Locator addDoctorButton() {
        return page.locator("button")
                .filter(new Locator.FilterOptions().setHasText("Thêm Bác Sĩ Mới"))
                .first();
    }

    private Locator createDoctorModal() {
        return page.locator("form")
                .filter(new Locator.FilterOptions().setHas(page.locator("#fullName")))
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
        return createDoctorModal().locator("button")
                .filter(new Locator.FilterOptions().setHasText("Lưu và Tạo Tài Khoản"))
                .first();
    }

    private Locator doctorTableBody() {
        // Loại trừ các bảng phụ khác nếu có trên trang
        return page.locator("table tbody")
                .filter(new Locator.FilterOptions().setHasNot(page.locator("#detailPatientsTableBody")))
                .first();
    }

    // ============================================================
    // HELPER METHODS (DỮ LIỆU & ĐỜI SỐNG TRANG)
    // ============================================================

    private String generateUniquePhone() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        return "09" + timestamp.substring(timestamp.length() - 8);
    }

    private String generateUniqueEmail(String prefix) {
        return prefix + System.currentTimeMillis() + "@benhvien.com";
    }

    private void waitForDoctorsPageLoad() {
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        doctorTableBody().waitFor();
    }

    // ============================================================
    // CẤU HÌNH JUNIT5 LIFECYCLE
    // ============================================================

    @BeforeAll
    static void setupAll() {
        playwright = Playwright.create();

        // Cấu hình trình duyệt (chạy có giao diện để dễ debug integration test)
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(false)
                        .setSlowMo(800)
        );
    }

    @BeforeEach
    void setup() {
        context = browser.newContext(
                new Browser.NewContextOptions().setViewportSize(1600, 900)
        );

        page = context.newPage();
        page.setDefaultTimeout(15_000);

        // 1. Đăng nhập hệ thống bằng quyền Admin
        page.navigate(LOGIN_URL);
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);

        loginEmailInput().fill(ADMIN_EMAIL);
        loginPasswordInput().fill(ADMIN_PASSWORD);
        loginSubmitButton().click();

        // Verification 1: Đăng nhập thành công (chuyển hướng sang Dashboard hoặc Doctors)
        page.waitForURL(
                Pattern.compile(".*(/hospital/dashboard|/hospital/doctors).*"),
                new Page.WaitForURLOptions().setTimeout(15_000)
        );
        assertTrue(
                page.url().contains("/hospital/dashboard") || page.url().contains("/hospital/doctors"),
                "VERIFY FAILED: Đăng nhập thất bại. URL hiện tại: " + page.url()
        );

        // 2. Điều hướng tới trang Quản lý Bác sĩ
        page.navigate(DOCTORS_URL);
        waitForDoctorsPageLoad();

        // Verification 2: Điều hướng đúng trang Doctor Management
        assertTrue(
                page.url().contains("/hospital/doctors"),
                "VERIFY FAILED: Không điều hướng tới được trang Doctor Management. URL hiện tại: " + page.url()
        );
    }

    // ============================================================
    // INTEGRATION TEST CASE
    // ============================================================

    @Test
    @DisplayName("IT-DOC-01: Kiểm tra luôn luồng Thêm Bác sĩ Mới và verify dữ liệu hiển thị")
    void testCreateDoctorSuccessfullyIT() {
        // --- STEP 3: Click "Thêm Bác Sĩ Mới" ---
        assertTrue(
                addDoctorButton().isVisible(),
                "VERIFY FAILED: Không tìm thấy nút 'Thêm Bác Sĩ Mới' trên màn hình."
        );
        addDoctorButton().click();

        // Verification 3: Mở form modal thêm bác sĩ thành công
        createDoctorModal().waitFor();
        assertTrue(
                createDoctorModal().isVisible(),
                "VERIFY FAILED: Modal form thêm bác sĩ chưa mở thành công."
        );

        // --- STEP 4: Nhập thông tin bác sĩ mới ---
        String expectedDoctorName = "Nguyễn Văn Anh";
        String expectedPhone = generateUniquePhone();
        String expectedEmail = generateUniqueEmail("doctor");
        String expectedCapacity = "40";
        String expectedGender = "Nam";
        String expectedSpecialty = "Tiểu đường";

        createDoctorNameInput().fill(expectedDoctorName);
        createDoctorPhoneInput().fill(expectedPhone);
        createDoctorEmailInput().fill(expectedEmail);
        createDoctorCapacityInput().fill(expectedCapacity);

        createDoctorGenderSelect().selectOption(new SelectOption().setLabel(expectedGender));
        createDoctorSpecialtySelect().selectOption(new SelectOption().setLabel(expectedSpecialty));

        // --- STEP 5: Click "Lưu và Tạo Tài Khoản" ---
        createDoctorSubmitButton().click();

        // Chờ hệ thống thực hiện lưu dữ liệu, gửi mail và render lại UI
        page.waitForTimeout(1_200);
        waitForDoctorsPageLoad();

        String bodyText = page.locator("body").innerText();
        String tableText = doctorTableBody().innerText();

        // Verification 4: Hiển thị thông báo thành công
        assertTrue(
                bodyText.contains("Thêm bác sĩ mới và gửi mail kích hoạt thành công!")
                        || bodyText.contains("thành công")
                        || bodyText.contains("Thành công"),
                "VERIFY FAILED: Không xuất hiện thông báo tạo bác sĩ thành công."
        );

        // Verification 5: Bác sĩ mới xuất hiện trong bảng danh sách
        assertTrue(
                tableText.contains(expectedDoctorName),
                "VERIFY FAILED: Tên bác sĩ mới (" + expectedDoctorName + ") không xuất hiện trong bảng."
        );

        // Verification 6: Email hiển thị chính xác trong bảng
        assertTrue(
                tableText.contains(expectedEmail),
                "VERIFY FAILED: Email bác sĩ mới (" + expectedEmail + ") không khớp trong bảng."
        );

        // Verification 7: Trạng thái mặc định là "Đang hoạt động"
        assertTrue(
                tableText.contains("Đang hoạt"),
                "VERIFY FAILED: Bác sĩ mới tạo chưa được đặt trạng thái 'Đang hoạt động'."
        );
    }

    // ============================================================
    // CLEANUP
    // ============================================================

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