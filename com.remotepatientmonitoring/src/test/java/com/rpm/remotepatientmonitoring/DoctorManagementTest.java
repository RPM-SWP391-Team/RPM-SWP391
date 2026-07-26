package com.rpm.remotepatientmonitoring;


import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.SelectOption;
import org.junit.jupiter.api.*;


import java.util.regex.Pattern;


import static org.junit.jupiter.api.Assertions.*;


public class DoctorManagementTest {




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
// SELECTOR DÙNG CHUNG
// ============================================================


    private Locator searchInput() {
        return page.locator("input[name='search']");
    }


    private Locator specialtyFilter() {
        return page.locator("form[action='/hospital/doctors'] select").first();
    }


    private Locator filterButton() {
        return page.locator("form[action='/hospital/doctors'] button[type='submit']");
    }


    /*
     * Bảng danh sách bác sĩ là tbody đầu tiên.
     * Bảng chi tiết bệnh nhân có id detailPatientsTableBody nên bị loại trừ.
     */
    private Locator doctorTableBody() {
        return page.locator("table tbody")
                .filter(new Locator.FilterOptions()
                        .setHasNot(page.locator("#detailPatientsTableBody")))
                .first();
    }


    private Locator doctorRows() {
        return doctorTableBody().locator("tr");
    }


    /*
     * Form thêm bác sĩ có input id="fullName".
     */
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


    private Locator addDoctorButton() {
        return page.locator("button")
                .filter(new Locator.FilterOptions()
                        .setHasText("Thêm Bác Sĩ Mới"))
                .first();
    }


    /*
     * Không dùng getByRole cho các nút Xem/Sửa/Bật/Tắt.
     * Vì icon nằm trong button có thể khiến accessible name khác text nhìn thấy.
     */
    private Locator actionButtonInRow(Locator row, String buttonText) {
        return row.locator("button")
                .filter(new Locator.FilterOptions().setHasText(buttonText))
                .first();
    }


    private String doctorCodeFromRow(Locator row) {
        return row.locator("td").first().innerText().trim();
    }


    private String doctorNameFromRow(Locator row) {
        return row.locator("td").nth(1).innerText().trim();
    }


    private Locator rowByDoctorCode(String doctorCode) {
        return doctorRows()
                .filter(new Locator.FilterOptions().setHasText(doctorCode))
                .first();
    }


    /*
     * Tìm hàng có nút Xem.
     */
    private Locator firstDoctorRowWithViewButton() {
        Locator rows = doctorRows();


        for (int i = 0; i < rows.count(); i++) {
            Locator row = rows.nth(i);


            if (actionButtonInRow(row, "Xem").count() > 0) {
                return row;
            }
        }


        return null;
    }


    /*
     * Bác sĩ đang hoạt động trên UI:
     * có badge "Đang hoạt động" hoặc bị rút gọn thành "Đang hoạt đ..."
     * và có nút Tắt.
     */
    private Locator firstActiveDoctorRow() {
        Locator rows = doctorRows();


        for (int i = 0; i < rows.count(); i++) {
            Locator row = rows.nth(i);
            String rowText = row.innerText();


            boolean hasActiveStatus = rowText.contains("Đang hoạt");
            boolean hasDisableButton = actionButtonInRow(row, "Tắt").count() > 0;


            if (hasActiveStatus && hasDisableButton) {
                return row;
            }
        }


        return null;
    }


    /*
     * Bác sĩ vô hiệu hóa:
     * có badge "Vô hiệu hóa" và có nút Bật.
     */
    private Locator firstDisabledDoctorRow() {
        Locator rows = doctorRows();


        for (int i = 0; i < rows.count(); i++) {
            Locator row = rows.nth(i);
            String rowText = row.innerText();


            boolean hasDisabledStatus = rowText.contains("Vô hiệu hóa");
            boolean hasEnableButton = actionButtonInRow(row, "Bật").count() > 0;


            if (hasDisabledStatus && hasEnableButton) {
                return row;
            }
        }


        return null;
    }


    /*
     * Hệ thống có thể dùng window.confirm().
     * Đăng ký listener trước click để không bị treo.
     */
    private void acceptBrowserDialog() {
        page.onceDialog(Dialog::accept);
    }


    /*
     * Một số giao diện dùng modal Bootstrap thay vì window.confirm().
     * Nếu modal xuất hiện, tìm nút xác nhận theo các text phổ biến.
     */
    private void confirmModalIfVisible() {
        Locator visibleModal = page.locator(
                ".modal.show, [role='dialog']:visible, .swal2-popup:visible"
        ).last();


        if (visibleModal.count() == 0 || !visibleModal.isVisible()) {
            return;
        }


        Locator confirmButton = visibleModal.locator("button").filter(
                new Locator.FilterOptions().setHasText(
                        Pattern.compile("(?i)xác nhận|đồng ý|ok|có|tiếp tục")
                )
        ).first();


        if (confirmButton.count() > 0 && confirmButton.isVisible()) {
            confirmButton.click();
        }
    }


    private void waitForDoctorsPage() {
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        doctorTableBody().waitFor();
    }


    /*
     * Sau click Bật/Tắt, bảng có thể render lại hoặc trang chuyển hướng lại.
     */
    private void waitForTableUpdate() {
        page.waitForTimeout(800);
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
        doctorTableBody().waitFor();
    }


    private String uniquePhone() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        return "09" + timestamp.substring(timestamp.length() - 8);
    }


    private String uniqueEmail(String prefix) {
        return prefix + System.currentTimeMillis() + "@benhvien.com";
    }


// ============================================================
// KHỞI TẠO
// ============================================================


    @BeforeAll
    static void setupAll() {
        playwright = Playwright.create();


        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                        .setHeadless(true)
                        .setSlowMo(1200)
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


        assertTrue(
                page.url().contains("/hospital/dashboard")
                        || page.url().contains("/hospital/doctors"),
                "Đăng nhập thất bại. URL hiện tại: " + page.url()
        );


        page.navigate(DOCTORS_URL);
        waitForDoctorsPage();


        assertTrue(
                page.url().contains("/hospital/doctors"),
                "Không vào được trang quản lý bác sĩ. URL hiện tại: " + page.url()
        );


        assertTrue(
                doctorRows().count() > 0,
                "Bảng danh sách bác sĩ không có dữ liệu để kiểm thử."
        );
    }


// ============================================================
// DOC-01: HIỂN THỊ TRANG QUẢN LÝ BÁC SĨ
// ============================================================


    @Test
    @DisplayName("[DOC-01] Kiểm tra hiển thị trang quản lý bác sĩ")
    void testDoctorsPageDisplay() {
        assertTrue(
                page.locator("body").innerText().contains("Danh sách Bác sĩ"),
                "Lỗi: Không hiển thị tiêu đề Danh sách Bác sĩ!"
        );


        assertTrue(
                searchInput().isVisible(),
                "Lỗi: Không hiển thị ô tìm kiếm bác sĩ!"
        );


        assertTrue(
                filterButton().isVisible(),
                "Lỗi: Không hiển thị nút Lọc!"
        );


        assertTrue(
                addDoctorButton().isVisible(),
                "Lỗi: Không hiển thị nút Thêm Bác Sĩ Mới!"
        );
    }


// ============================================================
// DOC-02: TÌM KIẾM THEO TÊN
// ============================================================


    @Test
    @DisplayName("[DOC-02] Kiểm tra tìm kiếm bác sĩ theo tên")
    void testSearchDoctorByName() {
        String existingName = doctorNameFromRow(doctorRows().first());


        searchInput().fill(existingName);
        filterButton().click();


        waitForDoctorsPage();


        assertTrue(
                doctorTableBody().innerText().contains(existingName),
                "Lỗi: Không tìm thấy bác sĩ \"" + existingName + "\" sau khi lọc!"
        );
    }


// ============================================================
// DOC-03: TÌM KIẾM THEO MÃ BÁC SĨ
// ============================================================


    @Test
    @DisplayName("[DOC-03] Kiểm tra tìm kiếm bác sĩ theo mã")
    void testSearchDoctorByCode() {
        String doctorCode = doctorCodeFromRow(doctorRows().first());


        searchInput().fill(doctorCode);
        filterButton().click();


        waitForDoctorsPage();


        Locator resultRows = doctorRows();


        assertTrue(
                resultRows.count() >= 1,
                "Lỗi: Không có kết quả khi tìm theo mã " + doctorCode
        );


        assertTrue(
                resultRows.first().innerText().contains(doctorCode),
                "Lỗi: Không tìm thấy bác sĩ có mã " + doctorCode
        );
    }


// ============================================================
// DOC-04: LỌC THEO CHUYÊN KHOA
// ============================================================


    @Test
    @DisplayName("[DOC-04] Kiểm tra lọc danh sách bác sĩ theo chuyên khoa")
    void testFilterDoctorsBySpecialty() {
        specialtyFilter().selectOption(
                new SelectOption().setLabel("Tiểu đường")
        );


        filterButton().click();
        waitForDoctorsPage();


        Locator rows = doctorRows();


        assertTrue(
                rows.count() > 0,
                "Lỗi: Không có bác sĩ thuộc chuyên khoa Tiểu đường!"
        );


        for (int i = 0; i < rows.count(); i++) {
            String rowText = rows.nth(i).innerText();


            assertTrue(
                    rowText.contains("Tiểu đường"),
                    "Lỗi: Kết quả lọc chứa bác sĩ không thuộc chuyên khoa Tiểu đường!"
            );
        }
    }


// ============================================================
// DOC-05: THÊM BÁC SĨ THÀNH CÔNG
// ============================================================


    @Test
    @DisplayName("[DOC-05] Kiểm tra thêm mới tài khoản bác sĩ thành công")
    void testCreateDoctorSuccessfully() {
        addDoctorButton().click();


        Locator modal = createDoctorModal();
        modal.waitFor();


        String doctorName = "Nguyễn Văn Anh";
        String email = uniqueEmail("bacsi");
        String phone = uniquePhone();


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


        createDoctorSubmitButton().click();


        /*
         * Hệ thống của bạn thêm thành công và hiển thị bác sĩ ở đầu bảng.
         */
        page.waitForTimeout(1_000);
        waitForDoctorsPage();


        String pageText = page.locator("body").innerText();


        assertTrue(
                pageText.contains("thành công")
                        || pageText.contains("Thành công")
                        || doctorTableBody().innerText().contains(doctorName),
                "Lỗi: Không thấy thông báo thêm bác sĩ thành công hoặc bác sĩ mới trong bảng!"
        );


        assertTrue(
                doctorRows().first().innerText().contains(doctorName),
                "Lỗi: Bác sĩ mới chưa xuất hiện ở đầu danh sách!"
        );


        assertTrue(
                doctorRows().first().innerText().contains(email),
                "Lỗi: Email của bác sĩ mới không đúng trong bảng!"
        );
    }


// ============================================================
// DOC-06: KHÔNG THỂ THÊM KHI BỎ TRỐNG HỌ TÊN
// ============================================================


    @Test
    @DisplayName("[DOC-06] Thêm mới thất bại khi để trống Họ và Tên")
    void testCreateDoctorMissingName() {
        addDoctorButton().click();
        createDoctorModal().waitFor();


        createDoctorPhoneInput().fill(uniquePhone());
        createDoctorEmailInput().fill(uniqueEmail("missingname"));


        createDoctorSubmitButton().click();
        page.waitForTimeout(300);


        assertTrue(
                createDoctorNameInput().isVisible(),
                "Lỗi: Form bị đóng dù chưa nhập Họ và Tên!"
        );


        assertEquals(
                "",
                createDoctorNameInput().inputValue(),
                "Lỗi: Trường Họ và Tên phải để trống!"
        );
    }


// ============================================================
// DOC-07: KHÔNG THỂ THÊM KHI BỎ TRỐNG SỐ ĐIỆN THOẠI
// ============================================================


    @Test
    @DisplayName("[DOC-07] Thêm mới thất bại khi để trống số điện thoại")
    void testCreateDoctorMissingPhone() {
        addDoctorButton().click();
        createDoctorModal().waitFor();


        createDoctorNameInput().fill("Bác sĩ thiếu số điện thoại");
        createDoctorEmailInput().fill(uniqueEmail("missingphone"));


        createDoctorSubmitButton().click();
        page.waitForTimeout(300);


        assertTrue(
                createDoctorPhoneInput().isVisible(),
                "Lỗi: Form bị đóng dù chưa nhập số điện thoại!"
        );


        assertEquals(
                "",
                createDoctorPhoneInput().inputValue(),
                "Lỗi: Trường số điện thoại phải để trống!"
        );
    }


// ============================================================
// DOC-08: KHÔNG THỂ THÊM KHI BỎ TRỐNG EMAIL
// ============================================================


    @Test
    @DisplayName("[DOC-08] Thêm mới thất bại khi để trống email")
    void testCreateDoctorMissingEmail() {
        addDoctorButton().click();
        createDoctorModal().waitFor();


        createDoctorNameInput().fill("Bác sĩ thiếu email");
        createDoctorPhoneInput().fill(uniquePhone());


        createDoctorSubmitButton().click();
        page.waitForTimeout(300);


        assertTrue(
                createDoctorEmailInput().isVisible(),
                "Lỗi: Form bị đóng dù chưa nhập email!"
        );


        assertEquals(
                "",
                createDoctorEmailInput().inputValue(),
                "Lỗi: Trường email phải để trống!"
        );
    }


// ============================================================
// DOC-09: TÌM KIẾM KHÔNG CÓ KẾT QUẢ
// ============================================================


    @Test
    @DisplayName("[DOC-09] Kiểm tra tìm kiếm bác sĩ không tồn tại")
    void testSearchNonExistingDoctor() {
        String keyword = "KHONGTONTAI_" + System.currentTimeMillis();


        searchInput().fill(keyword);
        filterButton().click();


        waitForDoctorsPage();


        String tableText = doctorTableBody().innerText();


        assertFalse(
                tableText.contains(keyword),
                "Lỗi: Kết quả tìm kiếm chứa dữ liệu không đúng với từ khóa không tồn tại!"
        );
    }


// ============================================================
// DOC-10: XEM CHI TIẾT BÁC SĨ
// ============================================================


    @Test
    @DisplayName("[DOC-10] Kiểm tra xem chi tiết bác sĩ")
    void testViewDoctorDetail() {
        Locator selectedRow = firstDoctorRowWithViewButton();


        assertNotNull(
                selectedRow,
                "Lỗi: Không tìm thấy bác sĩ nào có nút Xem!"
        );


        String doctorCode = doctorCodeFromRow(selectedRow);
        String doctorName = doctorNameFromRow(selectedRow);


        Locator viewButton = actionButtonInRow(selectedRow, "Xem");


        assertTrue(
                viewButton.count() > 0 && viewButton.isVisible(),
                "Lỗi: Không tìm thấy nút Xem của bác sĩ " + doctorCode
        );


        viewButton.click();
        page.waitForTimeout(600);


        /*
         * Chi tiết có thể mở modal hoặc vùng nội dung ngay trên trang.
         * Kiểm tra thông tin bác sĩ vẫn xuất hiện sau thao tác.
         */
        String bodyText = page.locator("body").innerText();


        assertTrue(
                bodyText.contains(doctorCode) || bodyText.contains(doctorName),
                "Lỗi: Không hiển thị thông tin chi tiết của bác sĩ " + doctorCode
        );
    }


// ============================================================
// DOC-11: VÔ HIỆU HÓA BÁC SĨ ĐANG HOẠT ĐỘNG
// ============================================================


    @Test
    @DisplayName("[DOC-11] Kiểm tra vô hiệu hóa tài khoản bác sĩ đang hoạt động")
    void testDisableActiveDoctor() {
        Locator activeRow = firstActiveDoctorRow();


        assertNotNull(
                activeRow,
                "Lỗi: Không tìm thấy bác sĩ đang hoạt động để vô hiệu hóa!"
        );


        String doctorCode = doctorCodeFromRow(activeRow);
        String doctorName = doctorNameFromRow(activeRow);


        Locator disableButton = actionButtonInRow(activeRow, "Tắt");


        assertTrue(
                disableButton.count() > 0 && disableButton.isVisible(),
                "Lỗi: Không tìm thấy nút Tắt của bác sĩ " + doctorCode
        );


        acceptBrowserDialog();
        disableButton.click();


        confirmModalIfVisible();
        waitForTableUpdate();


        Locator updatedRow = rowByDoctorCode(doctorCode);


        assertTrue(
                updatedRow.count() > 0,
                "Lỗi: Không tìm thấy bác sĩ " + doctorCode + " sau khi vô hiệu hóa!"
        );


        assertTrue(
                updatedRow.innerText().contains("Vô hiệu hóa"),
                "Lỗi: Bác sĩ " + doctorName + " chưa chuyển sang trạng thái Vô hiệu hóa!"
        );


        assertTrue(
                actionButtonInRow(updatedRow, "Bật").count() > 0,
                "Lỗi: Sau khi vô hiệu hóa, nút Bật chưa xuất hiện!"
        );
    }


// ============================================================
// DOC-12: KÍCH HOẠT LẠI BÁC SĨ ĐÃ VÔ HIỆU HÓA
// ============================================================


    @Test
    @DisplayName("[DOC-12] Kiểm tra kích hoạt lại tài khoản bác sĩ đã vô hiệu hóa")
    void testEnableDisabledDoctor() {
        Locator disabledRow = firstDisabledDoctorRow();


        assertNotNull(
                disabledRow,
                "Lỗi: Không tìm thấy bác sĩ đang bị vô hiệu hóa để kích hoạt!"
        );


        String doctorCode = doctorCodeFromRow(disabledRow);
        String doctorName = doctorNameFromRow(disabledRow);


        Locator enableButton = actionButtonInRow(disabledRow, "Bật");


        assertTrue(
                enableButton.count() > 0 && enableButton.isVisible(),
                "Lỗi: Không tìm thấy nút Bật của bác sĩ " + doctorCode
        );


        acceptBrowserDialog();
        enableButton.click();


        confirmModalIfVisible();
        waitForTableUpdate();


        Locator updatedRow = rowByDoctorCode(doctorCode);


        assertTrue(
                updatedRow.count() > 0,
                "Lỗi: Không tìm thấy bác sĩ " + doctorCode + " sau khi kích hoạt!"
        );


        assertTrue(
                updatedRow.innerText().contains("Đang hoạt"),
                "Lỗi: Bác sĩ " + doctorName + " chưa chuyển sang trạng thái Đang hoạt động!"
        );


        assertTrue(
                actionButtonInRow(updatedRow, "Tắt").count() > 0,
                "Lỗi: Sau khi kích hoạt, nút Tắt chưa xuất hiện!"
        );
    }


// ============================================================
// DỌN DẸP
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

