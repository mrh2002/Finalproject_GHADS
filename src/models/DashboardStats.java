package models;

public class DashboardStats {
    // Admin Dashboard Stats
    private int totalOrganizations;
    private int totalCoordinators;
    private int totalFamilies;
    private int servedFamilies;
    private int unservedFamilies;
    
    // Coordinator Dashboard Stats
    private int familiesServedByMyOrganization;
    private int myOrganizationDistributionsCount;
    
    // Common Stats
    private int totalDistributions;
    private int highVulnerabilityFamilies;
    private int mediumVulnerabilityFamilies;
    private int lowVulnerabilityFamilies;
    
    // Constructors
    public DashboardStats() {}
    
    // Getters and Setters
    public int getTotalOrganizations() {
        return totalOrganizations;
    }
    
    public void setTotalOrganizations(int totalOrganizations) {
        this.totalOrganizations = totalOrganizations;
    }
    
    public int getTotalCoordinators() {
        return totalCoordinators;
    }
    
    public void setTotalCoordinators(int totalCoordinators) {
        this.totalCoordinators = totalCoordinators;
    }
    
    public int getTotalFamilies() {
        return totalFamilies;
    }
    
    public void setTotalFamilies(int totalFamilies) {
        this.totalFamilies = totalFamilies;
    }
    
    public int getServedFamilies() {
        return servedFamilies;
    }
    
    public void setServedFamilies(int servedFamilies) {
        this.servedFamilies = servedFamilies;
    }
    
    public int getUnservedFamilies() {
        return unservedFamilies;
    }
    
    public void setUnservedFamilies(int unservedFamilies) {
        this.unservedFamilies = unservedFamilies;
    }
    
    public int getFamiliesServedByMyOrganization() {
        return familiesServedByMyOrganization;
    }
    
    public void setFamiliesServedByMyOrganization(int familiesServedByMyOrganization) {
        this.familiesServedByMyOrganization = familiesServedByMyOrganization;
    }
    
    public int getMyOrganizationDistributionsCount() {
        return myOrganizationDistributionsCount;
    }
    
    public void setMyOrganizationDistributionsCount(int myOrganizationDistributionsCount) {
        this.myOrganizationDistributionsCount = myOrganizationDistributionsCount;
    }
    
    public int getTotalDistributions() {
        return totalDistributions;
    }
    
    public void setTotalDistributions(int totalDistributions) {
        this.totalDistributions = totalDistributions;
    }
    
    public int getHighVulnerabilityFamilies() {
        return highVulnerabilityFamilies;
    }
    
    public void setHighVulnerabilityFamilies(int highVulnerabilityFamilies) {
        this.highVulnerabilityFamilies = highVulnerabilityFamilies;
    }
    
    public int getMediumVulnerabilityFamilies() {
        return mediumVulnerabilityFamilies;
    }
    
    public void setMediumVulnerabilityFamilies(int mediumVulnerabilityFamilies) {
        this.mediumVulnerabilityFamilies = mediumVulnerabilityFamilies;
    }
    
    public int getLowVulnerabilityFamilies() {
        return lowVulnerabilityFamilies;
    }
    
    public void setLowVulnerabilityFamilies(int lowVulnerabilityFamilies) {
        this.lowVulnerabilityFamilies = lowVulnerabilityFamilies;
    }
    
    // Helper method to calculate served percentage
    public double getServedPercentage() {
        if (totalFamilies == 0) return 0;
        return (servedFamilies * 100.0) / totalFamilies;
    }
    
    public double getUnservedPercentage() {
        if (totalFamilies == 0) return 0;
        return (unservedFamilies * 100.0) / totalFamilies;
    }
}