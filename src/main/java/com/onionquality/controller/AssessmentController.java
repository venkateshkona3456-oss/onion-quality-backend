package com.onionquality.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.onionquality.entity.Assessment;
import com.onionquality.service.AssessmentService;

@RestController
@RequestMapping("/api/assessments")
@CrossOrigin(origins = "*")
public class AssessmentController {

    @Autowired
    private AssessmentService assessmentService;

    @Autowired
    private RestTemplate restTemplate;

    @PostMapping
    public ResponseEntity<Assessment> createAssessment(@RequestBody Assessment assessment) {
        return ResponseEntity.ok(assessmentService.createAssessment(assessment));
    }

    @GetMapping
    public ResponseEntity<List<Assessment>> getAllAssessments() {
        return ResponseEntity.ok(assessmentService.getAllAssessments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Assessment> getAssessmentById(@PathVariable Long id) {
        return ResponseEntity.ok(assessmentService.getAssessmentById(id));
    }

    @PostMapping("/{id}/upload")
    public ResponseEntity<Map<String, Object>> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws java.io.IOException {
        String uploadDir = "uploads/";
        java.io.File dir = new java.io.File(uploadDir);
        if (!dir.exists()) dir.mkdirs();

        String fileName = id + "_" + file.getOriginalFilename();
        java.nio.file.Path path = java.nio.file.Paths.get(uploadDir + fileName);
        java.nio.file.Files.write(path, file.getBytes());

        org.springframework.util.LinkedMultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
        body.add("file", new org.springframework.core.io.FileSystemResource(path.toFile()));

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);

        org.springframework.http.HttpEntity<org.springframework.util.LinkedMultiValueMap<String, Object>> requestEntity =
                new org.springframework.http.HttpEntity<>(body, headers);

        Map<String, Object> aiResponse = restTemplate.postForObject(
                "http://localhost:8000/predict", requestEntity, Map.class);

        // Calculate Grade A % and URS % (proportional to detected defects)
        Map<String, Object> classCounts = (Map<String, Object>) aiResponse.get("class_counts");
        int defectiveCount = 0;
        int totalCount = 0;

        if (classCounts != null) {
            for (Map.Entry<String, Object> entry : classCounts.entrySet()) {
                int count = ((Number) entry.getValue()).intValue();
                totalCount += count;
                if (!entry.getKey().equalsIgnoreCase("onion")) {
                    defectiveCount += count;
                }
            }
        }

        double ursPercentage = totalCount > 0 ? (defectiveCount * 100.0 / totalCount) : 0.0;
        double gradeAPercentage = 100.0 - ursPercentage;

        Assessment assessment = assessmentService.getAssessmentById(id);
        assessment.setImagePath(path.toString());
        assessment.setGradeAPercentage(gradeAPercentage);
        assessment.setUrsPercentage(ursPercentage);
        assessmentService.createAssessment(assessment);

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("message", "Image uploaded and analyzed");
        response.put("aiResult", aiResponse);
        response.put("gradeAPercentage", gradeAPercentage);
        response.put("ursPercentage", ursPercentage);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/report")
    public ResponseEntity<Map<String, Object>> getReport(@PathVariable Long id) {
        Assessment assessment = assessmentService.getAssessmentById(id);

        Map<String, Object> report = new java.util.HashMap<>();
        report.put("lotNumber", assessment.getLotNumber());
        report.put("supplierName", assessment.getSupplierName());
        report.put("procurementCenter", assessment.getProcurementCenter());
        report.put("assessmentDate", assessment.getAssessmentDate());
        report.put("sampleWeight", assessment.getSampleWeight());
        report.put("gradeAPercentage", assessment.getGradeAPercentage());
        report.put("ursPercentage", assessment.getUrsPercentage());
        report.put("imagePath", assessment.getImagePath());

        return ResponseEntity.ok(report);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        List<Assessment> all = assessmentService.getAllAssessments();

        int total = all.size();
        long gradeALots = all.stream().filter(a -> a.getGradeAPercentage() != null && a.getGradeAPercentage() >= 50).count();
        double avgQuality = all.stream()
                .filter(a -> a.getGradeAPercentage() != null)
                .mapToDouble(Assessment::getGradeAPercentage)
                .average()
                .orElse(0.0);

        Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("totalAssessments", total);
        stats.put("gradeALots", gradeALots);
        stats.put("averageQuality", Math.round(avgQuality * 10.0) / 10.0);
        stats.put("reportsGenerated", total);
        stats.put("recentAssessments", all.stream().sorted((a, b) -> b.getId().compareTo(a.getId())).limit(5).toList());

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        List<Assessment> all = assessmentService.getAllAssessments();

        long gradeA = all.stream().filter(a -> a.getGradeAPercentage() != null && a.getGradeAPercentage() >= 50).count();
        long urs = all.size() - gradeA;

        Map<String, Long> gradeDistribution = new java.util.LinkedHashMap<>();
        gradeDistribution.put("Grade A", gradeA);
        gradeDistribution.put("URS", urs);

        Map<String, Long> monthlyTrend = new java.util.LinkedHashMap<>();
        for (Assessment a : all) {
            if (a.getAssessmentDate() != null) {
                String month = a.getAssessmentDate().toString().substring(0, 7);
                monthlyTrend.merge(month, 1L, Long::sum);
            }
        }

        Map<String, Object> analytics = new java.util.HashMap<>();
        analytics.put("gradeDistribution", gradeDistribution);
        analytics.put("monthlyTrend", monthlyTrend);
        analytics.put("totalLots", all.size());

        return ResponseEntity.ok(analytics);
    }
}