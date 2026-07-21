package com.rpm.remotepatientmonitoring.controller.patient;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
@RequestMapping("/patient")
public class PatientQuizController {

    public static class QuizQuestion {
        private int id;
        private String question;
        private List<String> options;
        private int correctOptionIndex;

        public QuizQuestion(int id, String question, List<String> options, int correctOptionIndex) {
            this.id = id;
            this.question = question;
            this.options = options;
            this.correctOptionIndex = correctOptionIndex;
        }

        public int getId() { return id; }
        public String getQuestion() { return question; }
        public List<String> getOptions() { return options; }
        public int getCorrectOptionIndex() { return correctOptionIndex; }
    }

    private static final List<QuizQuestion> QUESTIONS = new ArrayList<>();

    static {
        QUESTIONS.add(new QuizQuestion(1, 
            "Khi chỉ số đường huyết của bạn giảm dưới 4.4 mmol/L (hạ đường huyết), bước sơ cứu đầu tiên cần làm là gì?",
            Arrays.asList("A. Uống ngay một cốc nước lọc và đi ngủ.",
                          "B. Ăn hoặc uống ngay 15g đường nhanh (1 cốc nước đường, ly nước trái cây ngọt hoặc vài viên kẹo).",
                          "C. Tiêm ngay insulin liều cao.",
                          "D. Tiếp tục tập thể dục cường độ cao."),
            1));

        QUESTIONS.add(new QuizQuestion(2, 
            "Khi bị tăng huyết áp khẩn cấp (Huyết áp >= 180/110 mmHg), tư thế nằm nghỉ ngơi nào sau đây là khuyên dùng?",
            Arrays.asList("A. Nằm sấp, đầu cúi thấp.",
                          "B. Nằm ngửa nghỉ ngơi yên tĩnh, nâng đầu cao khoảng 30 độ.",
                          "C. Đứng tựa vào tường hoặc đi bộ nhẹ nhàng.",
                          "D. Nằm nghiêng bên trái, gác chân cao."),
            1));

        QUESTIONS.add(new QuizQuestion(3, 
            "Dấu hiệu nào dưới đây KHÔNG phải là triệu chứng thường gặp của hạ đường huyết?",
            Arrays.asList("A. Bủn rủn chân tay, vã mồ hôi lạnh.",
                          "B. Đói cồn cào, tim đập nhanh, chóng mặt.",
                          "C. Sốt cao kèm co giật và phát ban đỏ.",
                          "D. Lơ mơ, nhìn mờ."),
            2));

        QUESTIONS.add(new QuizQuestion(4, 
            "Tại sao không nên tự ý cho người bệnh uống thuốc hạ huyết áp nhanh (nhỏ dưới lưỡi...) khi bị tăng huyết áp khẩn cấp tại nhà?",
            Arrays.asList("A. Vì thuốc có vị quá đắng khó uống.",
                          "B. Vì có thể gây hạ huyết áp quá nhanh đột ngột, làm giảm tưới máu não dẫn đến đột quỵ thiếu máu cục bộ.",
                          "C. Vì thuốc hạ huyết áp nhanh không có tác dụng gì.",
                          "D. Vì thuốc sẽ làm tăng đường huyết."),
            1));

        QUESTIONS.add(new QuizQuestion(5, 
            "Nếu người bệnh hạ đường huyết nặng rơi vào trạng thái lơ mơ hoặc hôn mê, bước xử trí đúng là gì?",
            Arrays.asList("A. Cố gắng đổ nước đường vào miệng để người bệnh nuốt.",
                          "B. Cho người bệnh ăn kẹo cứng.",
                          "C. Tuyệt đối không cho ăn uống qua đường miệng để tránh sặc phổi, và lập tức gọi cấp cứu 115.",
                          "D. Đắp chăn ấm và chờ người bệnh tự tỉnh dậy."),
            2));
    }

    @GetMapping("/quiz")
    public String showQuiz(Model model) {
        model.addAttribute("questions", QUESTIONS);
        model.addAttribute("isSubmitted", false);
        return "patient/quiz";
    }

    @PostMapping("/quiz/submit")
    public String submitQuiz(
            @RequestParam Map<String, String> allParams,
            Model model) {
        
        Map<Integer, Integer> userAnswers = new HashMap<>();
        int score = 0;

        for (QuizQuestion q : QUESTIONS) {
            String answerKey = "answer_" + q.getId();
            String answerValStr = allParams.get(answerKey);
            int answeredIndex = -1;
            if (answerValStr != null) {
                try {
                    answeredIndex = Integer.parseInt(answerValStr);
                } catch (NumberFormatException ignored) {}
            }
            userAnswers.put(q.getId(), answeredIndex);
            if (answeredIndex == q.getCorrectOptionIndex()) {
                score++;
            }
        }

        model.addAttribute("questions", QUESTIONS);
        model.addAttribute("isSubmitted", true);
        model.addAttribute("score", score);
        model.addAttribute("userAnswers", userAnswers);

        return "patient/quiz";
    }
}
