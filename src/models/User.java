package models;

import java.time.LocalDateTime;

public class User {
    private int userId;
    private String fullName;
    private String username;
    private String password;
    private String email;
    private String role; // "ADMIN" or "COORDINATOR"
    private int organizationId;
    private String photoPath; // مسار الصورة (Bonus)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Additional fields for display (not stored in database)
    private String organizationName;
    
    // Constructors
    public User() {}
    
    public User(int userId, String fullName, String username, String password, 
                String email, String role, int organizationId, String photoPath) {
        this.userId = userId;
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.organizationId = organizationId;
        this.photoPath = photoPath;
    }
    
    public User(String fullName, String username, String password, 
                String email, String role, int organizationId, String photoPath) {
        this.fullName = fullName;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.organizationId = organizationId;
        this.photoPath = photoPath;
    }
    
    // Getters and Setters
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public int getOrganizationId() {
        return organizationId;
    }
    
    public void setOrganizationId(int organizationId) {
        this.organizationId = organizationId;
    }
    
    public String getPhotoPath() {
        return photoPath;
    }
    
    public void setPhotoPath(String photoPath) {
        this.photoPath = photoPath;
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
    
    public String getOrganizationName() {
        return organizationName;
    }
    
    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }
    
    // Helper methods
    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }
    
    public boolean isCoordinator() {
        return "COORDINATOR".equalsIgnoreCase(role);
    }
    
    @Override
    public String toString() {
        return fullName + " (" + username + ")";
    }
}