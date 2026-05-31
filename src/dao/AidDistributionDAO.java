package dao;

import models.AidDistribution;
import models.Family;
import utils.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AidDistributionDAO {
    
    private FamilyDAO familyDAO = new FamilyDAO();
    
    // ==================== DUPLICATE CHECK (BONUS WITH AID_TYPE) ====================
    
    /**
     * Checks if a family has received the SAME AID TYPE within the last 30 days
     * @param familyId The family ID
     * @param aidType The type of aid being distributed
     * @return true if received same aid type in last 30 days
     */
    public boolean hasReceivedSameAidTypeWithinLast30Days(int familyId, String aidType) throws SQLException {
        String sql = "SELECT COUNT(*) FROM aid_distributions "
                   + "WHERE family_id = ? AND aid_type = ? "
                   + "AND distribution_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, familyId);
            stmt.setString(2, aidType);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    
    /**
     * Checks if a family has received ANY aid within the last 30 days (for backward compatibility)
     */
    public boolean hasReceivedAnyAidWithinLast30Days(int familyId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM aid_distributions "
                   + "WHERE family_id = ? AND distribution_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, familyId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    
    /**
     * Main duplicate check logic according to project requirements + bonus
     * HIGH vulnerability: ALWAYS allowed
     * MEDIUM or LOW: NOT allowed if received SAME AID TYPE in last 30 days
     */
    public DuplicateCheckResult canDistributeAid(int familyId, String aidType) throws SQLException {
        Family family = familyDAO.getFamilyById(familyId);
        if (family == null) {
            return new DuplicateCheckResult(false, "العائلة غير موجودة", null);
        }
        
        String vulnerabilityLevel = family.getVulnerabilityLevel();
        
        // HIGH vulnerability always allowed
        if ("HIGH".equals(vulnerabilityLevel)) {
            return new DuplicateCheckResult(true, "مسموح - حالة العائلة عالية الخطورة", null);
        }
        
        // Check if received SAME AID TYPE in last 30 days
        boolean receivedSameAidType = hasReceivedSameAidTypeWithinLast30Days(familyId, aidType);
        
        if (receivedSameAidType) {
            // Get details of the previous distribution
            AidDistribution lastDistribution = getLastDistributionByFamilyAndAidType(familyId, aidType);
            String message = String.format(
                "❌ غير مسموح!\n\n" +
                "اسم العائلة: %s\n" +
                "مستوى الخطورة: %s\n" +
                "نوع المساعدة: %s\n" +
                "المنظمة المانحة: %s\n" +
                "تاريخ المساعدة السابقة: %s\n\n" +
                "لا يمكن صرف نفس نوع المساعدة لعائلة ذات خطورة %s خلال آخر 30 يومًا.",
                family.getHeadName(),
                vulnerabilityLevel,
                aidType,
                lastDistribution != null ? lastDistribution.getOrganizationName() : "غير معروف",
                lastDistribution != null ? lastDistribution.getDistributionDate() : null,
                vulnerabilityLevel
            );
            return new DuplicateCheckResult(false, message, lastDistribution);
        }
        
        return new DuplicateCheckResult(true, "مسموح - لم يتم صرف هذا النوع من المساعدة خلال آخر 30 يومًا", null);
    }
    
    /**
     * Get last distribution for a specific family and aid type
     */
    public AidDistribution getLastDistributionByFamilyAndAidType(int familyId, String aidType) throws SQLException {
        String sql = "SELECT ad.*, o.name as organization_name, f.head_name as family_name "
                   + "FROM aid_distributions ad "
                   + "JOIN organizations o ON ad.organization_id = o.id "
                   + "JOIN families f ON ad.family_id = f.id "
                   + "WHERE ad.family_id = ? AND ad.aid_type = ? "
                   + "ORDER BY ad.distribution_date DESC LIMIT 1";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, familyId);
            stmt.setString(2, aidType);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractAidDistributionWithNames(rs);
            }
        }
        return null;
    }
    
    // ==================== CREATE ====================
    
    public void createAidDistribution(AidDistribution distribution) throws SQLException {
        // First check if allowed
        DuplicateCheckResult check = canDistributeAid(distribution.getFamilyId(), distribution.getAidType());
        if (!check.isAllowed()) {
            throw new SQLException(check.getMessage());
        }
        
        String sql = "INSERT INTO aid_distributions (family_id, organization_id, coordinator_id, "
                   + "aid_type, distribution_date, notes) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, distribution.getFamilyId());
            stmt.setInt(2, distribution.getOrganizationId());
            stmt.setInt(3, distribution.getCoordinatorId());
            stmt.setString(4, distribution.getAidType());
            stmt.setDate(5, Date.valueOf(distribution.getDistributionDate()));
            stmt.setString(6, distribution.getNotes());
            stmt.executeUpdate();
            
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                distribution.setDistributionId(generatedKeys.getInt(1));
            }
        }
    }
    
    // ==================== READ ====================
    
    public AidDistribution getAidDistributionById(int id) throws SQLException {
        String sql = "SELECT ad.*, o.name as organization_name, f.head_name as family_name, u.full_name as coordinator_name "
                   + "FROM aid_distributions ad "
                   + "JOIN organizations o ON ad.organization_id = o.id "
                   + "JOIN families f ON ad.family_id = f.id "
                   + "JOIN users u ON ad.coordinator_id = u.id "
                   + "WHERE ad.id = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractAidDistributionWithNames(rs);
            }
        }
        return null;
    }
    
    public List<AidDistribution> getAllAidDistributions() throws SQLException {
        List<AidDistribution> distributions = new ArrayList<>();
        String sql = "SELECT ad.*, o.name as organization_name, f.head_name as family_name, u.full_name as coordinator_name "
                   + "FROM aid_distributions ad "
                   + "JOIN organizations o ON ad.organization_id = o.id "
                   + "JOIN families f ON ad.family_id = f.id "
                   + "JOIN users u ON ad.coordinator_id = u.id "
                   + "ORDER BY ad.distribution_date DESC";
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                distributions.add(extractAidDistributionWithNames(rs));
            }
        }
        return distributions;
    }
    
    public List<AidDistribution> getDistributionsByOrganization(int organizationId) throws SQLException {
        List<AidDistribution> distributions = new ArrayList<>();
        String sql = "SELECT ad.*, o.name as organization_name, f.head_name as family_name, u.full_name as coordinator_name "
                   + "FROM aid_distributions ad "
                   + "JOIN organizations o ON ad.organization_id = o.id "
                   + "JOIN families f ON ad.family_id = f.id "
                   + "JOIN users u ON ad.coordinator_id = u.id "
                   + "WHERE ad.organization_id = ? "
                   + "ORDER BY ad.distribution_date DESC";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, organizationId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                distributions.add(extractAidDistributionWithNames(rs));
            }
        }
        return distributions;
    }
    
    public List<AidDistribution> getDistributionsByFamily(int familyId) throws SQLException {
        List<AidDistribution> distributions = new ArrayList<>();
        String sql = "SELECT ad.*, o.name as organization_name, f.head_name as family_name, u.full_name as coordinator_name "
                   + "FROM aid_distributions ad "
                   + "JOIN organizations o ON ad.organization_id = o.id "
                   + "JOIN families f ON ad.family_id = f.id "
                   + "JOIN users u ON ad.coordinator_id = u.id "
                   + "WHERE ad.family_id = ? "
                   + "ORDER BY ad.distribution_date DESC";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, familyId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                distributions.add(extractAidDistributionWithNames(rs));
            }
        }
        return distributions;
    }
    
    public List<AidDistribution> getDistributionsByDateRange(LocalDate startDate, LocalDate endDate) throws SQLException {
        List<AidDistribution> distributions = new ArrayList<>();
        String sql = "SELECT ad.*, o.name as organization_name, f.head_name as family_name, u.full_name as coordinator_name "
                   + "FROM aid_distributions ad "
                   + "JOIN organizations o ON ad.organization_id = o.id "
                   + "JOIN families f ON ad.family_id = f.id "
                   + "JOIN users u ON ad.coordinator_id = u.id "
                   + "WHERE ad.distribution_date BETWEEN ? AND ? "
                   + "ORDER BY ad.distribution_date DESC";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                distributions.add(extractAidDistributionWithNames(rs));
            }
        }
        return distributions;
    }
    
    // ==================== UPDATE ====================
    
    public void updateAidDistribution(AidDistribution distribution) throws SQLException {
        // Check duplicate for update (if family or aid type changed)
        AidDistribution existing = getAidDistributionById(distribution.getDistributionId());
        if (existing.getFamilyId() != distribution.getFamilyId() || 
            !existing.getAidType().equals(distribution.getAidType())) {
            DuplicateCheckResult check = canDistributeAid(distribution.getFamilyId(), distribution.getAidType());
            if (!check.isAllowed()) {
                throw new SQLException(check.getMessage());
            }
        }
        
        String sql = "UPDATE aid_distributions SET aid_type = ?, distribution_date = ?, notes = ? WHERE id = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, distribution.getAidType());
            stmt.setDate(2, Date.valueOf(distribution.getDistributionDate()));
            stmt.setString(3, distribution.getNotes());
            stmt.setInt(4, distribution.getDistributionId());
            stmt.executeUpdate();
        }
    }
    
    // ==================== DELETE ====================
    
    public void deleteAidDistribution(int id) throws SQLException {
        String sql = "DELETE FROM aid_distributions WHERE id = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
    
    // ==================== STATISTICS ====================
    
    public int getTotalDistributionsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM aid_distributions";
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    public int getDistributionsCountByOrganization(int organizationId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM aid_distributions WHERE organization_id = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, organizationId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    // ==================== HELPER METHODS ====================
    
    private AidDistribution extractAidDistributionWithNames(ResultSet rs) throws SQLException {
        AidDistribution distribution = new AidDistribution();
        distribution.setDistributionId(rs.getInt("id"));
        distribution.setFamilyId(rs.getInt("family_id"));
        distribution.setOrganizationId(rs.getInt("organization_id"));
        distribution.setCoordinatorId(rs.getInt("coordinator_id"));
        distribution.setAidType(rs.getString("aid_type"));
        distribution.setDistributionDate(rs.getDate("distribution_date").toLocalDate());
        distribution.setNotes(rs.getString("notes"));
        
        // Set additional fields for display
        try {
            distribution.setOrganizationName(rs.getString("organization_name"));
            distribution.setFamilyName(rs.getString("family_name"));
            distribution.setCoordinatorName(rs.getString("coordinator_name"));
        } catch (SQLException e) {
            // These fields might not be available in all queries
        }
        
        return distribution;
    }
    
    // ==================== INNER CLASS FOR RESULT ====================
    
    public static class DuplicateCheckResult {
        private final boolean allowed;
        private final String message;
        private final AidDistribution lastDistribution;
        
        public DuplicateCheckResult(boolean allowed, String message, AidDistribution lastDistribution) {
            this.allowed = allowed;
            this.message = message;
            this.lastDistribution = lastDistribution;
        }
        
        public boolean isAllowed() { return allowed; }
        public String getMessage() { return message; }
        public AidDistribution getLastDistribution() { return lastDistribution; }
    }
}