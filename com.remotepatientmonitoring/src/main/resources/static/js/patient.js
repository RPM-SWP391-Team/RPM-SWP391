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
        const categories = [
            {
                id: 'category-bp',
                name: 'Cẩm nang Huyết áp',
                icon: 'fa-heart-pulse text-danger',
                badgeClass: 'bg-danger text-white',
                conditionTypes: ['HYPERTENSIVE_CRISIS']
            },
            {
                id: 'category-glucose',
                name: 'Cẩm nang Đường huyết',
                icon: 'fa-droplet text-info',
                badgeClass: 'bg-info text-dark',
                conditionTypes: ['HYPOGLYCEMIA', 'HYPERGLYCEMIA']
            }
        ];

        let categoryTabsHtml = '';
        let categoryPanesHtml = '';

        categories.forEach((cat, idx) => {
            const isActive = idx === 0 ? 'active' : '';
            const isSelected = idx === 0 ? 'true' : 'false';
            const showActive = idx === 0 ? 'show active' : '';
            const tabId = `handbook-cat-tab-${cat.id}`;
            const paneId = `handbook-cat-pane-${cat.id}`;

            categoryTabsHtml += `
                <button class="nav-link w-100 text-start py-3 px-3 border-0 border-bottom d-flex align-items-center gap-2 ${isActive}" 
                        id="${tabId}" data-bs-toggle="pill" data-bs-target="#${paneId}" 
                        type="button" role="tab" aria-controls="${paneId}" aria-selected="${isSelected}"
                        style="font-weight: 600; font-size: 0.88rem; transition: all 0.2s;">
                    <i class="fa-solid ${cat.icon} fa-lg me-1"></i>
                    <span>${cat.name}</span>
                </button>
            `;

            categoryPanesHtml += `
                <div class="tab-pane fade ${showActive}" id="${paneId}" role="tabpanel" aria-labelledby="${tabId}">
                    <!-- Search & Sort Header -->
                    <div class="p-3 bg-white border-bottom shadow-sm">
                        <h6 class="fw-bold text-dark mb-2" style="font-size: 0.95rem;">
                            <i class="fa-solid ${cat.icon} me-2"></i>Các bài viết về ${cat.name}
                        </h6>
                        <div class="row g-2 align-items-center">
                            <div class="col-7">
                                <div class="input-group input-group-sm">
                                    <span class="input-group-text bg-light border-end-0"><i class="fa-solid fa-magnifying-glass text-muted"></i></span>
                                    <input type="text" class="form-control border-start-0 category-search-input" data-category-id="${cat.id}" placeholder="Tìm bài viết...">
                                </div>
                            </div>
                            <div class="col-5 d-flex align-items-center justify-content-end">
                                <span class="text-muted me-1 text-nowrap" style="font-size: 0.7rem;"><i class="fa-solid fa-arrow-down-wide-short me-1"></i>Sắp xếp:</span>
                                <select class="form-select form-select-sm category-sort-select border w-auto" data-category-id="${cat.id}" style="font-size: 0.7rem; font-weight: 600; padding-top: 2px; padding-bottom: 2px;">
                                    <option value="newest" selected>Mới nhất</option>
                                    <option value="oldest">Cũ nhất</option>
                                    <option value="az">A - Z</option>
                                </select>
                            </div>
                        </div>
                    </div>

                    <!-- Scrollable Articles List -->
                    <div class="p-3 category-articles-list" data-category-id="${cat.id}" style="max-height: 380px; overflow-y: auto;">
                        <!-- Articles dynamically loaded -->
                    </div>
                </div>
            `;
        });

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
                                    <div class="col-md-4 border-end bg-light" style="min-height: 430px; display: flex; flex-direction: column;">
                                        <div class="nav flex-column nav-pills flex-grow-1" id="handbook-pills-tab" role="tablist" aria-orientation="vertical">
                                            ${categoryTabsHtml}
                                        </div>
                                        
                                        <!-- Quiz Link (Sticky at bottom) -->
                                        <div class="mt-auto border-top bg-white">
                                            <a href="/patient/quiz" class="nav-link w-100 text-start py-3 px-3 border-0 d-flex align-items-center gap-2 text-decoration-none" 
                                               style="font-weight: 600; font-size: 0.88rem; transition: all 0.2s; color: #10b981;">
                                                <i class="fa-solid fa-graduation-cap text-success fa-lg me-1"></i>
                                                <span>Kiểm tra kiến thức</span>
                                            </a>
                                        </div>
                                    </div>
                                    <!-- Right Content Panel -->
                                    <div class="col-md-8">
                                        <div class="tab-content" id="handbook-pills-tabContent">
                                            ${categoryPanesHtml}
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
        const handbookModalEl = handbookContainer.firstElementChild;
        document.body.appendChild(handbookModalEl);

        function renderHandbookForCategory(catId, searchTerm = '', sortBy = 'newest') {
            const cat = categories.find(c => c.id === catId);
            if (!cat) return;

            const listContainer = handbookModalEl.querySelector(`.category-articles-list[data-category-id="${catId}"]`);
            if (!listContainer) return;

            let filtered = [];
            if (protocols && protocols.length > 0) {
                filtered = protocols.filter(p => {
                    if (!cat.conditionTypes.includes(p.conditionType)) return false;
                    
                    const term = searchTerm.toLowerCase().trim();
                    if (!term) return true;
                    return (p.title && p.title.toLowerCase().includes(term)) ||
                           (p.warningSigns && p.warningSigns.toLowerCase().includes(term)) ||
                           (p.instructionContent && p.instructionContent.toLowerCase().includes(term));
                });

                // Sort
                filtered.sort((a, b) => {
                    if (sortBy === 'newest' || sortBy === 'oldest') {
                        const dateA = new Date(a.updatedAt || a.updated_at || a.createdAt || a.created_at || 0);
                        const dateB = new Date(b.updatedAt || b.updated_at || b.createdAt || b.created_at || 0);
                        return sortBy === 'newest' ? dateB - dateA : dateA - dateB;
                    } else if (sortBy === 'az') {
                        const titleA = (a.title || '').toLowerCase();
                        const titleB = (b.title || '').toLowerCase();
                        return titleA.localeCompare(titleB, 'vi');
                    }
                    return 0;
                });
            }

            let articlesHtml = '';

            if (filtered.length > 0) {
                filtered.forEach((p) => {
                    const dateVal = p.updatedAt || p.updated_at || p.createdAt || p.created_at;
                    let dateStr = '';
                    if (dateVal) {
                        const d = new Date(dateVal);
                        dateStr = d.toLocaleDateString('vi-VN', {
                            day: '2-digit',
                            month: '2-digit',
                            year: 'numeric'
                        }) + ' ' + d.toLocaleTimeString('vi-VN', {
                            hour: '2-digit',
                            minute: '2-digit'
                        });
                    }

                    const instructionLines = p.instructionContent ? p.instructionContent.split('\n').filter(s => s.trim().length > 0).map(s => {
                        const line = s.trim();
                        if (line.startsWith('-')) {
                            return `<li class="small text-secondary mb-1">${line.substring(1).trim()}</li>`;
                        } else if (line.startsWith('Bước') || line.startsWith('Quy trình') || line.startsWith('Cách xử trí') || line.startsWith('Biện pháp') || line.startsWith('Khi nào')) {
                            return `<h6 class="fw-bold mt-3 mb-2 text-dark" style="font-size: 0.9rem;">${line}</h6>`;
                        } else {
                            return `<p class="mb-1 text-secondary small" style="font-size: 0.82rem; line-height: 1.45;">${line}</p>`;
                        }
                    }).join('') : '';

                    const warningPreview = p.warningSigns ? p.warningSigns.substring(0, 100) + (p.warningSigns.length > 100 ? '...' : '') : 'Không có dấu hiệu đặc biệt.';

                    articlesHtml += `
                        <div class="card border-0 shadow-sm p-3 mb-3 bg-white rounded-3 hover-shadow" style="transition: all 0.2s; border-left: 4px solid #10b981 !important;">
                            <div class="d-flex justify-content-between align-items-start mb-2">
                                <h6 class="fw-bold text-dark mb-0" style="font-size: 0.88rem; line-height: 1.4;">${p.title}</h6>
                                <span class="badge ${cat.badgeClass} text-uppercase px-1.5 py-0.5" style="font-size: 0.6rem; border-radius: 4px;">${p.conditionType}</span>
                            </div>
                            
                            ${dateStr ? `
                                <div class="text-muted mb-2" style="font-size: 0.7rem; font-weight: 500;">
                                    <i class="fa-regular fa-clock me-1"></i>Cập nhật: ${dateStr}
                                </div>
                            ` : ''}
                            
                            <p class="text-secondary mb-2" style="font-size: 0.78rem; line-height: 1.4;">
                                <strong>Dấu hiệu:</strong> ${warningPreview}
                            </p>
                            
                            <button class="btn btn-sm btn-outline-primary w-100 py-1 fw-bold mt-2" 
                                    type="button" 
                                    data-bs-toggle="collapse" 
                                    data-bs-target="#article-collapse-${p.id}" 
                                    aria-expanded="false" 
                                    aria-controls="article-collapse-${p.id}"
                                    style="font-size: 0.75rem;">
                                <i class="fa-solid fa-chevron-down me-1"></i>Xem chi tiết bài cẩm nang
                            </button>
                            
                            <div class="collapse" id="article-collapse-${p.id}">
                                <div class="pt-3 mt-3 border-top">
                                    ${p.warningSigns ? `
                                        <div class="alert alert-danger border-0 p-2.5 mb-3" style="border-radius: 8px;">
                                            <strong class="small d-block mb-1 text-danger"><i class="fa-solid fa-triangle-exclamation me-1"></i>Dấu hiệu đầy đủ:</strong>
                                            <p class="mb-0 small text-secondary lh-base">${p.warningSigns}</p>
                                        </div>
                                    ` : ''}
                                    <div class="instruction-body">
                                        <strong class="small d-block mb-2 text-dark"><i class="fa-solid fa-list-check me-1 text-primary"></i>Hướng dẫn chi tiết:</strong>
                                        <ul class="ps-3 mb-0">
                                            ${instructionLines}
                                        </ul>
                                    </div>
                                </div>
                            </div>
                        </div>
                    `;
                });
            } else {
                articlesHtml = `
                    <div class="text-center py-5 text-muted small bg-light rounded-3 border">
                        <i class="fa-solid fa-file-circle-xmark fa-3x mb-2 text-muted"></i>
                        <p class="mb-0 fs-6 fw-semibold text-dark">Không có bài viết cẩm nang nào.</p>
                        <p class="text-muted small mt-1">Vui lòng thay đổi từ khóa hoặc bộ lọc tìm kiếm.</p>
                    </div>
                `;
            }

            listContainer.innerHTML = articlesHtml;

            const collapses = listContainer.querySelectorAll('.collapse');
            collapses.forEach(c => {
                const btn = listContainer.querySelector(`[data-bs-target="#${c.id}"]`);
                if (btn) {
                    c.addEventListener('show.bs.collapse', () => {
                        btn.innerHTML = '<i class="fa-solid fa-chevron-up me-1"></i>Thu gọn nội dung';
                        btn.classList.replace('btn-outline-primary', 'btn-primary');
                    });
                    c.addEventListener('hide.bs.collapse', () => {
                        btn.innerHTML = '<i class="fa-solid fa-chevron-down me-1"></i>Xem chi tiết bài cẩm nang';
                        btn.classList.replace('btn-primary', 'btn-outline-primary');
                    });
                }
            });
        }

        // Initial render for all categories
        categories.forEach(cat => {
            renderHandbookForCategory(cat.id);
        });

        // Bind events for search and sort elements in each category
        const searchInputs = handbookModalEl.querySelectorAll('.category-search-input');
        searchInputs.forEach(input => {
            const catId = input.getAttribute('data-category-id');
            input.addEventListener('input', (e) => {
                const sortSelect = handbookModalEl.querySelector(`.category-sort-select[data-category-id="${catId}"]`);
                renderHandbookForCategory(catId, e.target.value, sortSelect ? sortSelect.value : 'newest');
            });
        });

        const sortSelects = handbookModalEl.querySelectorAll('.category-sort-select');
        sortSelects.forEach(select => {
            const catId = select.getAttribute('data-category-id');
            select.addEventListener('change', (e) => {
                const searchInput = handbookModalEl.querySelector(`.category-search-input[data-category-id="${catId}"]`);
                renderHandbookForCategory(catId, searchInput ? searchInput.value : '', e.target.value);
            });
        });



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
