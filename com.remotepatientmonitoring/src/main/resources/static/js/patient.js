/* ==========================================================================
   Patient Emergency Support & Health Evaluation Script (patient.js)
   Separates emergency SOS (navbar) and floating health warning indicators.
   ========================================================================== */

document.addEventListener("DOMContentLoaded", function() {
    // 1. Inject custom styles dynamically
    const style = document.createElement('style');
    style.innerHTML = `
        .btn-sos-nav {
            background: linear-gradient(135deg, #ff416c, #ff4b2b) !important;
            color: white !important;
            border: none !important;
            box-shadow: 0 0 10px rgba(255, 75, 43, 0.8) !important;
            font-weight: 700 !important;
            border-radius: 20px !important;
            padding: 6px 16px !important;
            animation: navSosPulse 1.5s infinite;
            text-decoration: none !important;
            display: inline-flex;
            align-items: center;
            gap: 4px;
        }
        .btn-sos-nav:hover {
            transform: scale(1.05);
            background: linear-gradient(135deg, #ff4b2b, #ff416c) !important;
        }
        @keyframes navSosPulse {
            0%   { box-shadow: 0 0 0 0 rgba(255, 75, 43, 0.7); }
            70%  { box-shadow: 0 0 0 10px rgba(255, 75, 43, 0); }
            100% { box-shadow: 0 0 0 0 rgba(255, 75, 43, 0); }
        }
        .btn-handbook-nav {
            border: 2px solid rgba(255,255,255,0.4) !important;
            color: white !important;
            border-radius: 20px !important;
            padding: 6px 16px !important;
            font-weight: 700 !important;
            transition: all 0.25s !important;
            text-decoration: none !important;
            display: inline-flex;
            align-items: center;
            gap: 4px;
        }
        .btn-handbook-nav:hover {
            background-color: rgba(255,255,255,0.15) !important;
            border-color: white !important;
            transform: scale(1.03);
        }
        .health-status-btn {
            position: fixed;
            left: 20px;
            top: 90px;
            width: 62px;
            height: 62px;
            border-radius: 50%;
            color: #ffffff !important;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            font-size: .65rem;
            font-weight: 800;
            z-index: 1050;
            cursor: pointer;
            border: 3px solid #ffffff;
            text-decoration: none !important;
            transition: transform .2s;
            text-align: center;
            box-shadow: 0 6px 20px rgba(0,0,0,0.15);
        }
        .health-status-btn:hover { transform: scale(1.08); }
        .health-status-btn i { font-size: 1.25rem; margin-bottom: 2px; }
        .health-status-btn.level-1 { background: var(--gradient-teal) !important; }
        .health-status-btn.level-2 { background: #f1c40f !important; box-shadow: 0 6px 20px rgba(241,196,15,.4); }
        .health-status-btn.level-3 { background: #fd7e14 !important; box-shadow: 0 6px 20px rgba(253,126,20,.4); }
        .health-status-btn.level-4 { background: var(--gradient-danger) !important; animation: healthPulse 1.5s infinite; }
        @keyframes healthPulse {
            0%   { transform: scale(1);    box-shadow: 0 0 0 0 rgba(245,87,108,.6); }
            70%  { transform: scale(1.08); box-shadow: 0 0 0 16px rgba(245,87,108,0); }
            100% { transform: scale(1);    box-shadow: 0 0 0 0 rgba(245,87,108,0); }
        }
        .bg-orange {
            background-color: #fd7e14 !important;
        }
        #handbookModal .nav-link {
            color: #495057;
            border-radius: 0;
            background: transparent;
            border-left: 4px solid transparent;
        }
        #handbookModal .nav-link.active {
            color: #4361ee !important;
            background-color: rgba(67, 97, 238, 0.08) !important;
            border-left-color: #4361ee !important;
        }
        #handbookModal .nav-link:hover:not(.active) {
            background-color: rgba(0,0,0,0.02);
            border-left-color: rgba(0,0,0,0.05);
        }
    `;
    document.head.appendChild(style);

    // 2. Fetch emergency status and protocols from API
    Promise.all([
        fetch('/api/patient/health/emergency-status').then(res => res.json()),
        fetch('/api/patient/health/protocols').then(res => res.json())
    ])
    .then(([data, protocols]) => {
        const level = data.level || 1;
        const contactName = data.emergencyContactName || "Chưa thiết lập";
        const contactPhone = data.emergencyContactPhone || "";

        // 3. Create SOS & Handbook buttons in top navigation bar
        const navActions = document.querySelector('.navbar .d-flex.align-items-center');
        if (navActions) {
            // Handbook button
            const handbookNavBtn = document.createElement('button');
            handbookNavBtn.type = 'button';
            handbookNavBtn.className = 'btn btn-handbook-nav btn-outline-light btn-sm rounded-pill px-3 fw-bold me-2';
            handbookNavBtn.innerHTML = '<i class="fa-solid fa-book-open"></i> Cẩm nang';
            handbookNavBtn.setAttribute('data-bs-toggle', 'modal');
            handbookNavBtn.setAttribute('data-bs-target', '#handbookModal');
            navActions.insertBefore(handbookNavBtn, navActions.firstChild);

            // SOS button
            const sosNavBtn = document.createElement('button');
            sosNavBtn.type = 'button';
            sosNavBtn.className = 'btn btn-sos-nav btn-danger btn-sm rounded-pill px-3 fw-bold me-3';
            sosNavBtn.innerHTML = '<i class="fa-solid fa-circle-exclamation"></i> SOS';
            sosNavBtn.setAttribute('data-bs-toggle', 'modal');
            sosNavBtn.setAttribute('data-bs-target', '#emergencyModal');
            navActions.insertBefore(sosNavBtn, handbookNavBtn.nextSibling);
        }

        // 4. Create floating health warning button (always visible)
        const healthBtn = document.createElement('div');
        healthBtn.id = 'healthStatusFloatingButton';
        healthBtn.className = `health-status-btn level-${level}`;
        healthBtn.innerHTML = '<i class="fa-solid fa-heart-pulse"></i><span>Chỉ số</span>';
        healthBtn.setAttribute('data-bs-toggle', 'modal');
        healthBtn.setAttribute('data-bs-target', '#healthEvaluationModal');
        document.body.appendChild(healthBtn);

        // 5. Build emergency SOS guides HTML
        let bpGuidesHtml = '';
        let glucoseGuidesHtml = '';
        
        const defaultGuides = [
            {
                title: "HẠ ĐƯỜNG HUYẾT (GLUCOSE < 4.4 MMOL/L)",
                metricType: "GLUCOSE",
                content: "Triệu chứng: Bủn rủn chân tay, vã mồ hôi lạnh, chóng mặt, tim đập nhanh, đói cồn cào.\nSơ cứu: Ăn hoặc uống ngay 15g đường nhanh (1 cốc nước đường, 1 ly nước trái cây ngọt, 3-4 viên kẹo hoặc 1 thìa mật ong).\nNằm nghỉ ngơi thoải mái ở nơi thoáng mát, đo lại đường huyết sau 15 phút.\nLưu ý: Nếu người bệnh lơ mơ hoặc hôn mê, tuyệt đối không cho ăn uống qua đường miệng để tránh sặc phổi, lập tức gọi cấp cứu 115."
            },
            {
                title: "TĂNG HUYẾT ÁP KHẨN CẤP (HA >= 180/110 MMHG)",
                metricType: "BLOOD_PRESSURE",
                content: "Triệu chứng: Đau đầu dữ dội, hoa mắt, chóng mặt, buồn nôn, tức ngực, khó thở.\nSơ cứu: Nằm nghỉ ngơi yên tĩnh ở phòng thoáng, nâng đầu cao khoảng 30 độ.\nTuyệt đối không tự ý uống các loại thuốc hạ huyết áp nhanh (nhỏ dưới lưỡi...) nếu không có chỉ định từ bác sĩ.\nTheo dõi sát sao các dấu hiệu đột quỵ (méo miệng, nói ngọng, yếu nửa người) và lập tức gọi 115 nếu triệu chứng xấu đi."
            }
        ];

        const allGuides = (data.guides && data.guides.length > 0) ? data.guides : defaultGuides;

        allGuides.forEach(g => {
            const paragraphs = g.content.split('\n')
                .map(p => p.trim())
                .filter(p => p.length > 0)
                .map(p => `<li>${p}</li>`)
                .join('');
            
            const guideCard = `
                <div class="card card-custom border-danger p-3 mb-3 bg-light" style="border-radius: 10px;">
                    <h6 class="fw-bold text-danger mb-2" style="font-size: 0.92rem;">
                        <i class="fa-solid fa-kit-medical me-2"></i>${g.title}
                    </h6>
                    <div class="small text-dark lh-base">
                        <ul class="ps-3 mb-0 text-secondary" style="font-size: 0.82rem;">
                            ${paragraphs}
                        </ul>
                    </div>
                </div>
            `;

            let type = g.metricType;
            if (!type) {
                if (g.title.toLowerCase().includes("huyết áp") || g.title.toLowerCase().includes("ha") || g.title.toLowerCase().includes("bp")) {
                    type = "BLOOD_PRESSURE";
                } else {
                    type = "GLUCOSE";
                }
            }

            if (type === 'BLOOD_PRESSURE') {
                bpGuidesHtml += guideCard;
            } else {
                glucoseGuidesHtml += guideCard;
            }
        });

        // 6. Create Emergency SOS Modal HTML
        const emergencyModalHtml = `
            <div class="modal fade" id="emergencyModal" tabindex="-1" aria-labelledby="emergencyModalLabel" aria-hidden="true">
                <div class="modal-dialog modal-dialog-centered modal-lg">
                    <div class="modal-content modal-content-custom border-danger" style="border-radius: 16px; overflow: hidden;">
                        <div class="modal-header modal-header-custom bg-danger text-white border-0 py-3">
                            <h5 class="modal-title fw-bold" id="emergencyModalLabel">
                                <i class="fa-solid fa-triangle-exclamation me-2"></i>CỨU TRỢ KHẨN CẤP (SOS)
                            </h5>
                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body modal-body-custom p-4">
                            <!-- Call options (Top) -->
                            <div class="mb-4">
                                <h6 class="fw-bold text-dark mb-3"><i class="fa-solid fa-phone me-2 text-danger"></i>Gọi khẩn cấp</h6>
                                <div class="row g-2">
                                    <div class="col-md-6">
                                        <a href="tel:115" class="btn btn-danger btn-lg w-100 py-3 fw-bold rounded-3 text-white d-flex align-items-center justify-content-center gap-2 shadow">
                                            <i class="fa-solid fa-phone-flip"></i> GỌI CẤP CỨU 115
                                        </a>
                                    </div>
                                    <div class="col-md-6">
                                        ${contactPhone ? `
                                            <a href="tel:${contactPhone}" class="btn btn-warning btn-lg w-100 py-3 fw-bold rounded-3 text-white d-flex align-items-center justify-content-center gap-2 shadow">
                                                <i class="fa-solid fa-user-shield"></i> NGƯỜI THÂN: ${contactName}
                                            </a>
                                        ` : `
                                            <button class="btn btn-secondary btn-lg w-100 py-3 fw-bold rounded-3 text-white d-flex align-items-center justify-content-center gap-2" disabled>
                                                <i class="fa-solid fa-user-slash"></i> Người thân: Chưa thiết lập SĐT
                                            </button>
                                        `}
                                    </div>
                                </div>
                            </div>

                            <hr class="my-4">

                            <!-- Two sections for guides -->
                            <div class="row g-3">
                                <!-- Blood Pressure Guides -->
                                <div class="col-md-6 border-end">
                                    <h6 class="fw-bold text-primary mb-3">
                                        <i class="fa-solid fa-heart-pulse me-2"></i>HƯỚNG DẪN HUYẾT ÁP
                                    </h6>
                                    <div style="max-height: 400px; overflow-y: auto; padding-right: 5px;">
                                        ${bpGuidesHtml ? bpGuidesHtml : '<p class="text-muted small">Không có hướng dẫn huyết áp nào.</p>'}
                                    </div>
                                </div>

                                <!-- Glucose Guides -->
                                <div class="col-md-6">
                                    <h6 class="fw-semibold mb-3" style="color: #0891b2;">
                                        <i class="fa-solid fa-droplet me-2"></i>HƯỚNG DẪN ĐƯỜNG HUYẾT
                                    </h6>
                                    <div style="max-height: 400px; overflow-y: auto; padding-right: 5px;">
                                        ${glucoseGuidesHtml ? glucoseGuidesHtml : '<p class="text-muted small">Không có hướng dẫn đường huyết nào.</p>'}
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;

        // Append Emergency Modal
        const emergencyContainer = document.createElement('div');
        emergencyContainer.innerHTML = emergencyModalHtml;
        document.body.appendChild(emergencyContainer.firstElementChild);

        // 7. Build Handbook (Cẩm nang) Modal HTML dynamically
        let handbookTabsHtml = '';
        let handbookContentHtml = '';

        if (protocols && protocols.length > 0) {
            protocols.forEach((p, idx) => {
                const isActive = idx === 0 ? 'active' : '';
                const isSelected = idx === 0 ? 'true' : 'false';
                const showActive = idx === 0 ? 'show active' : '';
                const tabId = `handbook-tab-${p.id}`;
                const paneId = `handbook-pane-${p.id}`;

                let icon = 'fa-book-medical';
                let badgeClass = 'bg-primary text-white';
                if (p.conditionType === 'HYPERTENSIVE_CRISIS') {
                    icon = 'fa-heart-pulse text-danger';
                    badgeClass = 'bg-danger text-white';
                } else if (p.conditionType === 'HYPOGLYCEMIA') {
                    icon = 'fa-droplet text-info';
                    badgeClass = 'bg-info text-dark';
                } else if (p.conditionType === 'HYPERGLYCEMIA') {
                    icon = 'fa-circle-exclamation text-warning';
                    badgeClass = 'bg-warning text-dark';
                }

                // Tab link
                handbookTabsHtml += `
                    <button class="nav-link w-100 text-start py-3 px-3 border-0 border-bottom d-flex align-items-center gap-2 ${isActive}" 
                            id="${tabId}" data-bs-toggle="pill" data-bs-target="#${paneId}" 
                            type="button" role="tab" aria-controls="${paneId}" aria-selected="${isSelected}"
                            style="font-weight: 600; font-size: 0.88rem; transition: all 0.2s;">
                        <i class="fa-solid ${icon} fa-lg me-1"></i>
                        <span>${p.title}</span>
                    </button>
                `;

                // Format instruction lines
                const instructionLines = p.instructionContent ? p.instructionContent.split('\n').filter(s => s.trim().length > 0).map(s => {
                    const line = s.trim();
                    if (line.startsWith('-')) {
                        return `<li class="small text-secondary mb-1">${line.substring(1).trim()}</li>`;
                    } else if (line.startsWith('Bước') || line.startsWith('Quy trình') || line.startsWith('Cách xử trí') || line.startsWith('Biện pháp') || line.startsWith('Khi nào')) {
                        return `<h6 class="fw-bold mt-3 mb-2 text-dark" style="font-size: 0.95rem;">${line}</h6>`;
                    } else {
                        return `<p class="mb-1 text-secondary small" style="font-size: 0.85rem;">${line}</p>`;
                    }
                }).join('') : '';

                // Tab pane content
                handbookContentHtml += `
                    <div class="tab-pane fade ${showActive}" id="${paneId}" role="tabpanel" aria-labelledby="${tabId}">
                        <div class="p-4" style="max-height: 480px; overflow-y: auto;">
                            <div class="d-flex align-items-center gap-2 mb-3">
                                <span class="badge ${badgeClass} text-uppercase px-2 py-1" style="font-size: 0.68rem; border-radius: 4px;">${p.conditionType}</span>
                                <h5 class="fw-bold text-dark mb-0">${p.title}</h5>
                            </div>

                            ${p.warningSigns ? `
                                <div class="card border-0 bg-light p-3 mb-3 shadow-sm" style="border-radius: 10px; border-left: 4px solid #dc3545 !important;">
                                    <h6 class="fw-bold text-danger mb-2" style="font-size: 0.88rem;"><i class="fa-solid fa-triangle-exclamation me-2"></i>Dấu hiệu nhận biết</h6>
                                    <p class="text-secondary small mb-0 lh-base" style="font-size: 0.82rem;">${p.warningSigns}</p>
                                </div>
                            ` : ''}

                            <div class="instruction-body">
                                ${instructionLines}
                            </div>
                        </div>
                    </div>
                `;
            });
        } else {
            // Default fallback
            handbookTabsHtml = `
                <button class="nav-link w-100 text-start py-3 px-3 border-0 border-bottom d-flex align-items-center gap-2 active" 
                        id="handbook-tab-default-bp" data-bs-toggle="pill" data-bs-target="#handbook-pane-default-bp" 
                        type="button" role="tab" aria-selected="true" style="font-weight: 600; font-size: 0.88rem;">
                    <i class="fa-solid fa-heart-pulse text-danger fa-lg me-1"></i>
                    <span>Cẩm nang Huyết áp</span>
                </button>
            `;
            handbookContentHtml = `
                <div class="tab-pane fade show active" id="handbook-pane-default-bp" role="tabpanel">
                    <div class="p-4" style="max-height: 480px; overflow-y: auto;">
                        <h5 class="fw-bold text-dark mb-3">Cẩm nang Huyết áp</h5>
                        <p class="small text-muted">Vui lòng tải lại trang hoặc kiểm tra kết nối với hệ thống dữ liệu để cập nhật đầy đủ cẩm nang chi tiết.</p>
                    </div>
                </div>
            `;
        }

        const handbookModalHtml = `
            <div class="modal fade" id="handbookModal" tabindex="-1" aria-labelledby="handbookModalLabel" aria-hidden="true">
                <div class="modal-dialog modal-dialog-centered modal-lg">
                    <div class="modal-content border-0" style="border-radius: 16px; overflow: hidden; box-shadow: 0 10px 30px rgba(0,0,0,0.18);">
                        <div class="modal-header bg-primary text-white border-0 py-3 px-4">
                            <h5 class="modal-title fw-bold" id="handbookModalLabel">
                                <i class="fa-solid fa-book-open me-2"></i>CẨM NANG KIẾN THỨC Y TẾ
                            </h5>
                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body p-0">
                            <div class="container-fluid p-0">
                                <div class="row g-0">
                                    <!-- Left Navigation Sidebar -->
                                    <div class="col-md-4 border-end bg-light" style="min-height: 400px;">
                                        <div class="nav flex-column nav-pills" id="handbook-pills-tab" role="tablist" aria-orientation="vertical">
                                            ${handbookTabsHtml}
                                            <a href="/patient/quiz" class="nav-link w-100 text-start py-3 px-3 border-0 border-bottom d-flex align-items-center gap-2 text-decoration-none" 
                                               style="font-weight: 600; font-size: 0.88rem; transition: all 0.2s; color: #10b981;">
                                                <i class="fa-solid fa-graduation-cap text-success fa-lg me-1"></i>
                                                <span>Kiểm tra kiến thức</span>
                                            </a>
                                        </div>
                                    </div>
                                    <!-- Right Content Panel -->
                                    <div class="col-md-8">
                                        <div class="tab-content" id="handbook-pills-tabContent">
                                            ${handbookContentHtml}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;

        // Append Handbook Modal
        const handbookContainer = document.createElement('div');
        handbookContainer.innerHTML = handbookModalHtml;
        document.body.appendChild(handbookContainer.firstElementChild);

        // 8. Create Health Evaluation Modal HTML
        let levelText = 'Bình thường';
        let levelBadgeClass = 'bg-success';
        let evaluationDesc = 'Các chỉ số sức khỏe của bạn đang ở ngưỡng an toàn. Hãy tiếp tục duy trì chế độ sinh hoạt và ăn uống lành mạnh.';
        
        if (level === 2) {
            levelText = 'Chú ý';
            levelBadgeClass = 'bg-warning text-dark';
            evaluationDesc = 'Chỉ số huyết áp hoặc đường huyết của bạn đang ở mức chú ý (Vàng). Hãy nghỉ ngơi, giảm bớt căng thẳng và hạn chế ăn mặn, ngọt.';
        } else if (level === 3) {
            levelText = 'Nguy hiểm';
            levelBadgeClass = 'bg-orange text-white';
            evaluationDesc = 'Chỉ số sức khỏe của bạn đang ở mức nguy hiểm (Cam). Hãy chú ý nghỉ ngơi, uống thuốc đúng giờ và báo cho bác sĩ nếu chỉ số không giảm.';
        } else if (level === 4) {
            levelText = 'Cấp cứu';
            levelBadgeClass = 'bg-danger text-white';
            evaluationDesc = 'Chỉ số sức khỏe của bạn đang ở mức cấp cứu nguy kịch (Đỏ)! Hãy dừng mọi hoạt động, nghỉ ngơi ngay lập tức, báo cho người thân và bác sĩ, hoặc bấm nút SOS trên thanh tiêu đề để gọi 115.';
        }

        const healthModalHtml = `
            <div class="modal fade" id="healthEvaluationModal" tabindex="-1" aria-labelledby="healthEvalLabel" aria-hidden="true">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content modal-content-custom" style="border-radius: 16px; overflow: hidden;">
                        <div class="modal-header modal-header-custom bg-light border-0 py-3 px-4">
                            <h5 class="modal-title fw-bold text-dark mb-0" id="healthEvalLabel">
                                <i class="fa-solid fa-heart-pulse text-danger me-2"></i>ĐÁNH GIÁ SỨC KHỎE HÔM NAY
                            </h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body p-4">
                            <div class="text-center mb-4">
                                <div class="d-inline-block px-4 py-2 rounded-pill fw-bold ${levelBadgeClass} mb-2" style="font-size: 1.1rem; box-shadow: 0 4px 10px rgba(0,0,0,0.08);">
                                    Trạng thái: ${levelText}
                                </div>
                                <p class="text-muted small mb-0">Cập nhật dựa trên các chỉ số đo gần đây nhất trong ngày</p>
                            </div>
                            
                            <div class="row g-3 mb-4">
                                <div class="col-6">
                                    <div class="p-3 bg-light rounded-3 text-center border">
                                        <small class="text-muted fw-semibold d-block mb-1">Huyết áp</small>
                                        <span class="fw-bold fs-5 text-dark">
                                            ${(data.latestSystolic && data.latestDiastolic) ? `${data.latestSystolic}/${data.latestDiastolic}` : '--/--'}
                                        </span>
                                        <small class="text-muted d-block mt-1 small">mmHg</small>
                                    </div>
                                </div>
                                <div class="col-6">
                                    <div class="p-3 bg-light rounded-3 text-center border">
                                        <small class="text-muted fw-semibold d-block mb-1">Đường huyết</small>
                                        <span class="fw-bold fs-5 text-dark">
                                            ${data.latestGlucose ? data.latestGlucose : '--.-'}
                                        </span>
                                        <small class="text-muted d-block mt-1 small">mmol/L</small>
                                    </div>
                                </div>
                            </div>

                            <div class="card p-3 border-0 bg-light rounded-3 shadow-sm mb-2">
                                <h6 class="fw-bold text-secondary mb-2" style="font-size: 0.95rem;"><i class="fa-solid fa-comment-medical me-2 text-primary"></i>Nhận xét & Khuyên dùng</h6>
                                <p class="small text-secondary mb-0 lh-base" style="font-size: 0.88rem;">${evaluationDesc}</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;

        // Append Health Evaluation Modal
        const healthContainer = document.createElement('div');
        healthContainer.innerHTML = healthModalHtml;
        document.body.appendChild(healthContainer.firstElementChild);
    })
    .catch(err => console.error("Error loading health & emergency details:", err));
});
