package models;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AidDistribution {
    private int distributionId;
    private int familyId;
    private int organizationId;
    private int coordinatorId;
    private String aidType; // نوع المساعدة (طعام, دواء, إيواء, إلخ) - مهم لـ Bonus
    private LocalDate distributionDate;
    private String notes; // ملاحظات عن المساعدة
    private int quantity; // كمية المساعدة
    private String unit; // وحدة القياس (كيلو, علبة, قطعة, إلخ)
    
    // Additional fields for display (not stored in database)
    private String organizationName;
    private String familyName;
    private String coordinatorName;
    private String familyHeadName;
    private String vulnerabilityLevel;
    
    // Constructors
    public AidDistribution() {
        this.distributionDate = LocalDate.now();
    }
    
    public AidDistribution(int distributionId, int familyId, int organizationId, 
                           int coordinatorId, String aidType, LocalDate distributionDate, 
                           String notes) {
        this.distributionId = distributionId;
        this.familyId = familyId;
        this.organizationId = organizationId;
        this.coordinatorId = coordinatorId;
        this.aidType = aidType;
        this.distributionDate = distributionDate != null ? distributionDate : LocalDate.now();
        this.notes = notes;
    }
    
    public AidDistribution(int familyId, int organizationId, int coordinatorId, 
                           String aidType, LocalDate distributionDate, String notes) {
        this.familyId = familyId;
        this.organizationId = organizationId;
        this.coordinatorId = coordinatorId;
        this.aidType = aidType;
        this.distributionDate = distributionDate != null ? distributionDate : LocalDate.now();
        this.notes = notes;
    }
    
    // Getters and Setters
    public int getDistributionId() {
        return distributionId;
    }
    
    public void setDistributionId(int distributionId) {
        this.distributionId = distributionId;
    }
    
    public int getFamilyId() {
        return familyId;
    }
    
    public void setFamilyId(int familyId) {
        this.familyId = familyId;
    }
    
    public int getOrganizationId() {
        return organizationId;
    }
    
    public void setOrganizationId(int organizationId) {
        this.organizationId = organizationId;
    }
    
    public int getCoordinatorId() {
        return coordinatorId;
    }
    
    public void setCoordinatorId(int coordinatorId) {
        this.coordinatorId = coordinatorId;
    }
    
    public String getAidType() {
        return aidType;
    }
    
    public void setAidType(String aidType) {
        this.aidType = aidType;
    }
    
    public LocalDate getDistributionDate() {
        return distributionDate;
    }
    
    public void setDistributionDate(LocalDate distributionDate) {
        this.distributionDate = distributionDate;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public String getOrganizationName() {
        return organizationName;
    }
    
    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }
    
    public String getFamilyName() {
        return familyName;
    }
    
    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }
    
    public String getCoordinatorName() {
        return coordinatorName;
    }
    
    public void setCoordinatorName(String coordinatorName) {
        this.coordinatorName = coordinatorName;
    }
    
    public String getFamilyHeadName() {
        return familyHeadName;
    }
    
    public void setFamilyHeadName(String familyHeadName) {
        this.familyHeadName = familyHeadName;
    }
    
    public String getVulnerabilityLevel() {
        return vulnerabilityLevel;
    }
    
    public void setVulnerabilityLevel(String vulnerabilityLevel) {
        this.vulnerabilityLevel = vulnerabilityLevel;
    }
    
    // Helper methods
    public String getFormattedDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return distributionDate != null ? distributionDate.format(formatter) : "";
    }
    
    public boolean isWithinLast30Days() {
        if (distributionDate == null) return false;
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        return distributionDate.isAfter(thirtyDaysAgo) || distributionDate.isEqual(thirtyDaysAgo);
    }
    
    @Override
    public String toString() {
        return aidType + " - " + getFormattedDate();
    }
}