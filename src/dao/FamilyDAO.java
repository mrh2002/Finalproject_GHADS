package dao;

import models.Family;
import utils.DBConnection;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FamilyDAO {
    
    // ==================== CREATE ====================
    
    public void createFamily(Family family) throws SQLException {
        // Check if National ID already exists
        if (isNationalIdExists(family.getNationalId())) {
            throw new SQLException("رقم الهوية الوطنية موجود بالفعل");
        }
        
        String sql = "INSERT INTO families (national_id, head_name, phone, address, family_members_count, "
                   + "vulnerability_level, registration_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, family.getNationalId());
            stmt.setString(2, family.getHeadName());
            stmt.setString(3, family.getPhone());
            stmt.setString(4, family.getAddress());
            stmt.setInt(5, family.getFamilyMembersCount());
            stmt.setString(6, family.getVulnerabilityLevel());
            stmt.setDate(7, Date.valueOf(family.getRegistrationDate()));
            stmt.executeUpdate();
            
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                family.setFamilyId(generatedKeys.getInt(1));
            }
        }
    }
    
    // ==================== READ ====================
    
    public Family getFamilyById(int id) throws SQLException {
        String sql = "SELECT * FROM families WHERE id = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractFamilyFromResultSet(rs);
            }
        }
        return null;
    }
    
    public Family getFamilyByNationalId(String nationalId) throws SQLException {
        String sql = "SELECT * FROM families WHERE national_id = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, nationalId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractFamilyFromResultSet(rs);
            }
        }
        return null;
    }
    
    public List<Family> getAllFamilies() throws SQLException {
        List<Family> families = new ArrayList<>();
        String sql = "SELECT * FROM families ORDER BY registration_date DESC";
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                families.add(extractFamilyFromResultSet(rs));
            }
        }
        return families;
    }
    
    public List<Family> getFamiliesByVulnerabilityLevel(String level) throws SQLException {
        List<Family> families = new ArrayList<>();
        String sql = "SELECT * FROM families WHERE vulnerability_level = ? ORDER BY head_name";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, level);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                families.add(extractFamilyFromResultSet(rs));
            }
        }
        return families;
    }
    
    public List<Family> getMostVulnerableFamilies() throws SQLException {
        List<Family> families = new ArrayList<>();
        String sql = "SELECT * FROM families ORDER BY "
                   + "CASE vulnerability_level "
                   + "WHEN 'HIGH' THEN 1 "
                   + "WHEN 'MEDIUM' THEN 2 "
                   + "WHEN 'LOW' THEN 3 END, head_name";
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                families.add(extractFamilyFromResultSet(rs));
            }
        }
        return families;
    }
    
    // ==================== UPDATE ====================
    
    public void updateFamily(Family family) throws SQLException {
        // Check if National ID exists for another family
        String checkSql = "SELECT COUNT(*) FROM families WHERE national_id = ? AND id != ?";
        try (PreparedStatement checkStmt = DBConnection.getConnection().prepareStatement(checkSql)) {
            checkStmt.setString(1, family.getNationalId());
            checkStmt.setInt(2, family.getFamilyId());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("رقم الهوية الوطنية موجود بالفعل لعائلة أخرى");
            }
        }
        
        String sql = "UPDATE families SET national_id = ?, head_name = ?, phone = ?, address = ?, "
                   + "family_members_count = ?, vulnerability_level = ? WHERE id = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, family.getNationalId());
            stmt.setString(2, family.getHeadName());
            stmt.setString(3, family.getPhone());
            stmt.setString(4, family.getAddress());
            stmt.setInt(5, family.getFamilyMembersCount());
            stmt.setString(6, family.getVulnerabilityLevel());
            stmt.setInt(7, family.getFamilyId());
            stmt.executeUpdate();
        }
    }
    
    // ==================== DELETE ====================
    
    public void deleteFamily(int id) throws SQLException {
        // Check if family has aid distributions
        String checkSql = "SELECT COUNT(*) FROM aid_distributions WHERE family_id = ?";
        try (PreparedStatement checkStmt = DBConnection.getConnection().prepareStatement(checkSql)) {
            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("لا يمكن حذف العائلة لأن لديها سجلات مساعدات");
            }
        }
        
        String sql = "DELETE FROM families WHERE id = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
    
    // ==================== VALIDATION ====================
    
    public boolean isNationalIdExists(String nationalId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM families WHERE national_id = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, nationalId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    
    // ==================== STATISTICS ====================
    
    public int getTotalFamiliesCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM families";
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    public int getServedFamiliesCount() throws SQLException {
        String sql = "SELECT COUNT(DISTINCT family_id) FROM aid_distributions";
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    public int getUnservedFamiliesCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM families f WHERE NOT EXISTS "
                   + "(SELECT 1 FROM aid_distributions ad WHERE ad.family_id = f.id)";
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    public List<Family> getUnservedFamilies() throws SQLException {
        List<Family> families = new ArrayList<>();
        String sql = "SELECT f.* FROM families f WHERE NOT EXISTS "
                   + "(SELECT 1 FROM aid_distributions ad WHERE ad.family_id = f.id) "
                   + "ORDER BY f.head_name";
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                families.add(extractFamilyFromResultSet(rs));
            }
        }
        return families;
    }
    
    public int getFamiliesServedByOrganization(int organizationId) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT family_id) FROM aid_distributions WHERE organization_id = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, organizationId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    // ==================== HELPER ====================
    
    private Family extractFamilyFromResultSet(ResultSet rs) throws SQLException {
        Family family = new Family();
        family.setFamilyId(rs.getInt("id"));
        family.setNationalId(rs.getString("national_id"));
        family.setHeadName(rs.getString("head_name"));
        family.setPhone(rs.getString("phone"));
        family.setAddress(rs.getString("address"));
        family.setFamilyMembersCount(rs.getInt("family_members_count"));
        family.setVulnerabilityLevel(rs.getString("vulnerability_level"));
        family.setRegistrationDate(rs.getDate("registration_date").toLocalDate());
        return family;
    }
}