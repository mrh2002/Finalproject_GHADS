package models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Family {
    private int familyId;
    private String nationalId; // رقم الهوية الوطنية (فريد)
    private String headName; // اسم رب الأسرة
    private String phone; // رقم الهاتف
    private String address; // العنوان
    private int familyMembersCount; // عدد أفراد الأسرة
    private String vulnerabilityLevel; // LOW, MEDIUM, HIGH
    private LocalDate registrationDate; // تاريخ التسجيل
    private String notes; // ملاحظات إضافية
    
    // Additional fields for display
    private boolean isServed; // هل تلقت الأسرة مساعدة؟
    private int servedCount; // عدد مرات تلقي المساعدة
    private LocalDate lastAidDate; // تاريخ آخر مساعدة
    private String lastAidType; // نوع آخر مساعدة
    private List<AidDistribution> aidDistributions; // سجل المساعدات
    
    // Constructors
    public Family() {
        this.registrationDate = LocalDate.now();
        this.aidDistributions = new ArrayList<>();
    }
    
    public Family(int familyId, String nationalId, String headName, String phone, 
                  String address, int familyMembersCount, String vulnerabilityLevel, 
                  LocalDate registrationDate) {
        this.familyId = familyId;
        this.nationalId = nationalId;
        this.headName = headName;
        this.phone = phone;
        this.address = address;
        this.familyMembersCount = familyMembersCount;
        this.vulnerabilityLevel = vulnerabilityLevel;
        this.registrationDate = registrationDate != null ? registrationDate : LocalDate.now();
        this.aidDistributions = new ArrayList<>();
    }
    
    public Family(String nationalId, String headName, String phone, String address, 
                  int familyMembersCount, String vulnerabilityLevel) {
        this.nationalId = nationalId;
        this.headName = headName;
        this.phone = phone;
        this.address = address;
        this.familyMembersCount = familyMembersCount;
        this.vulnerabilityLevel = vulnerabilityLevel;
        this.registrationDate = LocalDate.now();
        this.aidDistributions = new ArrayList<>();
    }
    
    // Getters and Setters
    public int getFamilyId() {
        return familyId;
    }
    
    public void setFamilyId(int familyId) {
        this.familyId = familyId;
    }
    
    public String getNationalId() {
        return nationalId;
    }
    
    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }
    
    public String getHeadName() {
        return headName;
    }
    
    public void setHeadName(String headName) {
        this.headName = headName;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public int getFamilyMembersCount() {
        return familyMembersCount;
    }
    
    public void setFamilyMembersCount(int familyMembersCount) {
        this.familyMembersCount = familyMembersCount;
    }
    
    public String getVulnerabilityLevel() {
        return vulnerabilityLevel;
    }
    
    public void setVulnerabilityLevel(String vulnerabilityLevel) {
        this.vulnerabilityLevel = vulnerabilityLevel;
    }
    
    public LocalDate getRegistrationDate() {
        return registrationDate;
    }
    
    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public boolean isServed() {
        return isServed;
    }
    
    public void setServed(boolean served) {
        isServed = served;
    }
    
    public int getServedCount() {
        return servedCount;
    }
    
    public void setServedCount(int servedCount) {
        this.servedCount = servedCount;
    }
    
    public LocalDate getLastAidDate() {
        return lastAidDate;
    }
    
    public void setLastAidDate(LocalDate lastAidDate) {
        this.lastAidDate = lastAidDate;
    }
    
    public String getLastAidType() {
        return lastAidType;
    }
    
    public void setLastAidType(String lastAidType) {
        this.lastAidType = lastAidType;
    }
    
    public List<AidDistribution> getAidDistributions() {
        return aidDistributions;
    }
    
    public void setAidDistributions(List<AidDistribution> aidDistributions) {
        this.aidDistributions = aidDistributions;
    }
    
    // Helper methods
    public boolean isHighVulnerability() {
        return "HIGH".equalsIgnoreCase(vulnerabilityLevel);
    }
    
    public boolean isMediumVulnerability() {
        return "MEDIUM".equalsIgnoreCase(vulnerabilityLevel);
    }
    
    public boolean isLowVulnerability() {
        return "LOW".equalsIgnoreCase(vulnerabilityLevel);
    }
    
    public String getVulnerabilityLevelArabic() {
        switch (vulnerabilityLevel) {
            case "HIGH": return "عالية";
            case "MEDIUM": return "متوسطة";
            case "LOW": return "منخفضة";
            default: return vulnerabilityLevel;
        }
    }
    
    public String getVulnerabilityLevelColor() {
        switch (vulnerabilityLevel) {
            case "HIGH": return "#FF4444";
            case "MEDIUM": return "#FFA500";
            case "LOW": return "#4CAF50";
            default: return "#000000";
        }
    }
    
    @Override
    public String toString() {
        return headName + " - " + nationalId + " (" + getVulnerabilityLevelArabic() + ")";
    }
}