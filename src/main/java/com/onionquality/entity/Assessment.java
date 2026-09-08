   package com.onionquality.entity;

   import java.time.LocalDate;

   import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

   @Entity
   @Table(name = "assessments")
   public class Assessment {

       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;

       private String supplierName;
       private String lotNumber;
       private String procurementCenter;
       private Double sampleWeight;
       private LocalDate assessmentDate;
       private String imagePath;
       private Double gradeAPercentage;
private Double ursPercentage;

public Double getGradeAPercentage() { return gradeAPercentage; }
public void setGradeAPercentage(Double gradeAPercentage) { this.gradeAPercentage = gradeAPercentage; }

public Double getUrsPercentage() { return ursPercentage; }
public void setUrsPercentage(Double ursPercentage) { this.ursPercentage = ursPercentage; }

public String getImagePath() { return imagePath; }
public void setImagePath(String imagePath) { this.imagePath = imagePath; }
private String createdBy;

public String getCreatedBy() { return createdBy; }
public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

       // Getters and Setters
       public Long getId() { return id; }
       public void setId(Long id) { this.id = id; }

       public String getSupplierName() { return supplierName; }
       public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

       public String getLotNumber() { return lotNumber; }
       public void setLotNumber(String lotNumber) { this.lotNumber = lotNumber; }

       public String getProcurementCenter() { return procurementCenter; }
       public void setProcurementCenter(String procurementCenter) { this.procurementCenter = procurementCenter; }

       public Double getSampleWeight() { return sampleWeight; }
       public void setSampleWeight(Double sampleWeight) { this.sampleWeight = sampleWeight; }

       public LocalDate getAssessmentDate() { return assessmentDate; }
       public void setAssessmentDate(LocalDate assessmentDate) { this.assessmentDate = assessmentDate; }
   }