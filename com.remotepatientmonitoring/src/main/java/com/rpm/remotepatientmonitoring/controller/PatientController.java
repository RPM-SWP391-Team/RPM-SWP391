package com.rpm.remotepatientmonitoring.controller;

import com.rpm.remotepatientmonitoring.model.DiseaseProfile;
import com.rpm.remotepatientmonitoring.model.Patient;
import com.rpm.remotepatientmonitoring.model.TreatmentPlan;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/patient")
public class PatientController {

    @GetMapping("/dashboard")
    public String getDashboard(Model model) {
        // Mock Patient data
        DiseaseProfile profile = DiseaseProfile.builder()
                .profileName("Đồng mắc (Tiểu đường & Tăng huyết áp)")
                .build();

        Patient patient = Patient.builder()
                .fullName("Nguyễn Văn A")
                .diseaseProfile(profile)
                .build();

        // Mock TreatmentPlan data
        TreatmentPlan plan = new TreatmentPlan();
        plan.setMedicalOrder("1. Metformin 500mg: Uống 1 viên sau ăn sáng (8:00) và 1 viên sau ăn tối (20:00)\n" +
                             "2. Amlodipine 5mg: Uống 1 viên vào buổi sáng (8:00)");
        plan.setExerciseGoal("Đi bộ nhẹ nhàng 30 phút mỗi ngày sau bữa ăn tối");

        // Mock Menu data (Today's meal list)
        List<Map<String, Object>> menuList = List.of(
                Map.of(
                        "mealName", "Bữa Sáng",
                        "food", "Cháo yến mạch củ quả nấu ức gà (1 bát), 1 cốc sữa đậu nành không đường (200ml)",
                        "calories", 380,
                        "salt", 0.5,
                        "fiber", 6.5
                ),
                Map.of(
                        "mealName", "Bữa Trưa",
                        "food", "Cơm gạo lứt (1 chén), Cá hồi áp chảo (150g), Súp lơ xanh luộc chấm nước tương nhạt",
                        "calories", 550,
                        "salt", 1.2,
                        "fiber", 8.0
                ),
                Map.of(
                        "mealName", "Bữa Tối",
                        "food", "Canh bí đỏ thịt bằm (ít muối), Đậu hũ nhồi thịt hấp, Salad rau xà lách cà chua bi",
                        "calories", 470,
                        "salt", 0.8,
                        "fiber", 5.5
                )
        );

        // Daily nutritional targets
        model.addAttribute("patient", patient);
        model.addAttribute("treatmentPlan", plan);
        model.addAttribute("menu", menuList);
        model.addAttribute("targetCalories", 1400);
        model.addAttribute("targetSalt", 2.5);
        model.addAttribute("targetFiber", 20.0);

        return "patient/dashboard";
    }
}
