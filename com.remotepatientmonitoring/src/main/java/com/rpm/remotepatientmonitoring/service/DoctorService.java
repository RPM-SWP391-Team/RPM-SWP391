package com.rpm.remotepatientmonitoring.service;

import com.rpm.remotepatientmonitoring.model.Account;
import com.rpm.remotepatientmonitoring.model.Doctor;
import com.rpm.remotepatientmonitoring.model.Hospital;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.repository.AccountRepository;
import com.rpm.remotepatientmonitoring.repository.DoctorRepository;
import com.rpm.remotepatientmonitoring.repository.HospitalRepository;
import com.rpm.remotepatientmonitoring.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private HospitalRepository hospitalRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Doctor> getDoctorsByHospital(Integer hospitalId) {
        return doctorRepository.findByHospitalId(hospitalId);
    }

    @Transactional
    public Doctor createDoctor(Integer hospitalId, String doctorCode, String fullName, String phone, String email, String password, String specialty, Integer capacityLimit) {

        // CHÈN THÊM LUẬT CHẶN TÊN LẠ VÀO ĐẦU HÀM:
        String nameRegex = "^[a-zA-ZÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯĂÂÊÔƠƠƯỨỨỬỮỰẤẤẨẪẬẮẮẲẴẬéèẻẽéêềếểễệíìỉĩịóòỏõọôồốổỗộơờớởỡợúùủũụưừứửữựýỳỷỹỵ\\s]{2,50}$";
        if (fullName == null || !fullName.trim().matches(nameRegex)) {
            throw new IllegalArgumentException("Họ và tên bác sĩ không hợp lệ. Tên chỉ được phép chứa chữ cái và khoảng trắng.");
        }
        // 1. CHẶN LOGIC CHUYÊN KHOA CHUẨN UNICODE (Đồng bộ khít 100% với DoctorDTO)
        if (specialty == null || (!specialty.equals("Tiểu đường") && !specialty.equals("Huyết áp") && !specialty.equals("Cả tiểu đường và huyết áp"))) {
            throw new IllegalArgumentException("Chuyên khoa lâm sàng không hợp lệ. Hệ thống chỉ chấp nhận: 'Tiểu đường', 'Huyết áp' hoặc 'Cả tiểu đường và huyết áp'.");
        }

        if (doctorRepository.existsByDoctorCode(doctorCode)) {
            throw new IllegalArgumentException("Mã bác sĩ đã tồn tại trong hệ thống.");
        }
        if (doctorRepository.existsByPhone(phone)) {
            throw new IllegalArgumentException("Số điện thoại đã được đăng ký.");
        }
        if (accountRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email đã được đăng ký tài khoản.");
        }

        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bệnh viện với ID: " + hospitalId));

        Account account = Account.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role("DOCTOR")
                .isEmailVerified(true)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        account = accountRepository.save(account);

        Doctor doctor = Doctor.builder()
                .account(account)
                .hospital(hospital)
                .doctorCode(doctorCode)
                .fullName(fullName)
                .phone(phone)
                .specialty(specialty)
                .capacityLimit(capacityLimit)
                .currentPatientCount(0)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return doctorRepository.save(doctor);
    }

    @Transactional
    public void deactivateDoctor(Integer doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bác sĩ với ID: " + doctorId));

        // 1. Thực hiện luồng phân bổ chuyển giao bệnh nhân tự động
        List<Patient> activePatients = patientRepository.findByDoctorIdAndIsActiveTrue(doctorId);

        if (!activePatients.isEmpty()) {
            List<ReplacementDoctorDto> replacements = getReplacementCapacityList(doctor.getHospital().getId(), doctorId);

            if (replacements.isEmpty()) {
                throw new IllegalStateException("Không thể vô hiệu hóa! Toàn bộ bác sĩ khác trong viện đều đã QUÁ TẢI, không có ai nhận bàn giao " + activePatients.size() + " bệnh nhân.");
            }

            int replacementIndex = 0;
            for (Patient patient : activePatients) {
                while (replacementIndex < replacements.size() &&
                        replacements.get(replacementIndex).getTempCount() >= replacements.get(replacementIndex).getCapacityLimit()) {
                    replacementIndex++;
                }

                if (replacementIndex >= replacements.size()) {
                    throw new IllegalStateException("Cạn kiệt hạn ngạch tiếp nhận của toàn viện giữa chừng! Vui lòng nâng Capacity Limit của các bác sĩ khác trước.");
                }

                ReplacementDoctorDto targetDto = replacements.get(replacementIndex);
                Doctor replacementDoctor = targetDto.getDoctor();

                // Đồng bộ chính xác theo JPA Object Mapping của bạn
                patient.setDoctor(replacementDoctor);
                patient.setUpdatedAt(LocalDateTime.now());
                patientRepository.save(patient);

                targetDto.setTempCount(targetDto.getTempCount() + 1);
                replacementDoctor.setCurrentPatientCount(targetDto.getTempCount());
                doctorRepository.save(replacementDoctor);
            }
        }

        // 2. Khóa trạng thái hành chính lâm sàng
        doctor.setCurrentPatientCount(0);
        doctor.setIsActive(false);
        doctor.setUpdatedAt(LocalDateTime.now());
        doctorRepository.save(doctor);

        // 3. Khóa đồng bộ tài khoản đăng nhập bảo mật
        if (doctor.getAccount() != null) {
            Account account = doctor.getAccount();
            account.setIsActive(false);
            account.setUpdatedAt(LocalDateTime.now());
            accountRepository.save(account);
        }
    }

    private List<ReplacementDoctorDto> getReplacementCapacityList(Integer hospitalId, Integer currentDoctorId) {
        List<Doctor> docs = doctorRepository.findBestReplacementDoctors(hospitalId, currentDoctorId);
        return docs.stream()
                .map(d -> new ReplacementDoctorDto(d, d.getCurrentPatientCount(), d.getCapacityLimit()))
                .collect(Collectors.toList());
    }

    private static class ReplacementDoctorDto {
        private final Doctor doctor;
        private int tempCount;
        private final int capacityLimit;

        public ReplacementDoctorDto(Doctor doctor, int tempCount, int capacityLimit) {
            this.doctor = doctor;
            this.tempCount = tempCount;
            this.capacityLimit = capacityLimit;
        }
        public Doctor getDoctor() { return doctor; }
        public int getTempCount() { return tempCount; }
        public void setTempCount(int tempCount) { this.tempCount = tempCount; }
        public int getCapacityLimit() { return capacityLimit; }
    }
}