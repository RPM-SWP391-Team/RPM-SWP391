with open(r'd:\ki5\SWP391\RPM-SWP391\com.remotepatientmonitoring\src\main\resources\templates\doctor\change-requests.html', 'r', encoding='utf-8') as f:
    content = f.read()

pagination = '''
        <nav aria-label="Page navigation" th:if="${changeRequestPage.totalPages > 1}" class="mt-4 pb-3">
            <ul class="pagination justify-content-center">
                <li class="page-item" th:classappend="${changeRequestPage.first} ? 'disabled'">
                    <a class="page-link" th:href="@{/doctor/change-requests(tab=${activeTab}, startDate=${startDate}, endDate=${endDate}, page=${changeRequestPage.number - 1})}">
                        <i class="fa-solid fa-chevron-left"></i> Trước
                    </a>
                </li>
                <li class="page-item" th:each="i : ${#numbers.sequence(0, changeRequestPage.totalPages - 1)}"
                    th:classappend="${changeRequestPage.number == i} ? 'active'">
                    <a class="page-link" th:href="@{/doctor/change-requests(tab=${activeTab}, startDate=${startDate}, endDate=${endDate}, page=${i})}" th:text="${i + 1}">1</a>
                </li>
                <li class="page-item" th:classappend="${changeRequestPage.last} ? 'disabled'">
                    <a class="page-link" th:href="@{/doctor/change-requests(tab=${activeTab}, startDate=${startDate}, endDate=${endDate}, page=${changeRequestPage.number + 1})}">
                        Sau <i class="fa-solid fa-chevron-right"></i>
                    </a>
                </li>
            </ul>
        </nav>
'''

if '<!-- Process Modal -->' in content:
    start_modal = content.find('<!-- Process Modal -->')
    last_div_idx = content.rfind('</div>', 0, start_modal)
    if last_div_idx != -1:
        new_content = content[:last_div_idx] + '        </div>\n' + pagination + content[last_div_idx + 6:]
        with open(r'd:\ki5\SWP391\RPM-SWP391\com.remotepatientmonitoring\src\main\resources\templates\doctor\change-requests.html', 'w', encoding='utf-8') as f:
            f.write(new_content)
        print('Pagination added!')
