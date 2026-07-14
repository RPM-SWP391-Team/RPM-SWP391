import os
import io

path = r'd:\ki5\SWP391\RPM-SWP391\com.remotepatientmonitoring\src\main\java\com\rpm\remotepatientmonitoring\controller\doctor\DoctorViewController.java'
with io.open(path, 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('\"Không tìm th?y bác si\"', 'MSG_DOCTOR_NOT_FOUND')
content = content.replace('\"errorMsg\"', 'ATTR_ERROR_MSG')
content = content.replace('\"successMsg\"', 'ATTR_SUCCESS_MSG')
content = content.replace('\"redirect:/auth/login\"', 'REDIRECT_LOGIN')
content = content.replace('\"redirect:/doctor/profile\"', 'REDIRECT_PROFILE')
content = content.replace('\"redirect:/doctor/dashboard\"', 'REDIRECT_DASHBOARD')
content = content.replace('\"redirect:/doctor/appointments\"', 'REDIRECT_APPOINTMENTS')
content = content.replace('\"redirect:/doctor/change-requests\"', 'REDIRECT_CHANGE_REQUESTS')
content = content.replace('model.addAttribute(\"doctor\",', 'model.addAttribute(ATTR_DOCTOR,')
content = content.replace('System.out.println', 'log.error')
content = content.replace('e.printStackTrace()', 'log.error(\"Exception: \", e)')

with io.open(path, 'w', encoding='utf-8') as f:
    f.write(content)
print('Done!')
