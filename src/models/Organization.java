package models;

import java.time.LocalDateTime;

public class Organization {
    private int organizationId;
    private String name;
    private String type; // إغاثي, طبي, غذائي, إلخ
    private String contactInfo; // هاتف, بريد, عنوان
    private String address;
    private String phone;
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional fields
    private int usersCount; // عدد المستخدمين في المنظمة
    private int distributionsCount; // عدد المساعدات المقدمة
    
    // Constructors
    public Organization() {}
    
    public Organization(int organizationId, String name, String type, String contactInfo) {
        this.organizationId = organizationId;
        this.name = name;
        this.type = type;
        this.contactInfo = contactInfo;
    }
    
    public Organization(String name, String type, String contactInfo) {
        this.name = name;
        this.type = type;
        this.contactInfo = contactInfo;
    }
    
    public Organization(int organizationId, String name, String type, 
                        String contactInfo, String address, String phone, String email) {
        this.organizationId = organizationId;
        this.name = name;
        this.type = type;
        this.contactInfo = contactInfo;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }
    
    // Getters and Setters
    public int getOrganizationId() {
        return organizationId;
    }
    
    public void setOrganizationId(int organizationId) {
        this.organizationId = organizationId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getContactInfo() {
        return contactInfo;
    }
    
    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public int getUsersCount() {
        return usersCount;
    }
    
    public void setUsersCount(int usersCount) {
        this.usersCount = usersCount;
    }
    
    public int getDistributionsCount() {
        return distributionsCount;
    }
    
    public void setDistributionsCount(int distributionsCount) {
        this.distributionsCount = distributionsCount;
    }
    
    @Override
    public String toString() {
        return name;
    }
}