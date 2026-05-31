package dao;

import models.Organization;
import utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrganizationDAO {
    
    // ==================== CREATE ====================
    
    public void createOrganization(Organization org) throws SQLException {
        String sql = "INSERT INTO organizations (name, type, contact_info) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, org.getName());
            stmt.setString(2, org.getType());
            stmt.setString(3, org.getContactInfo());
            stmt.executeUpdate();
            
            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                org.setOrganizationId(generatedKeys.getInt(1));
            }
        }
    }
    
    // ==================== READ ====================
    
    public Organization getOrganizationById(int id) throws SQLException {
        String sql = "SELECT * FROM organizations WHERE id = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractOrganizationFromResultSet(rs);
            }
        }
        return null;
    }
    
    public Organization getOrganizationByName(String name) throws SQLException {
        String sql = "SELECT * FROM organizations WHERE name = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractOrganizationFromResultSet(rs);
            }
        }
        return null;
    }
    
    public List<Organization> getAllOrganizations() throws SQLException {
        List<Organization> organizations = new ArrayList<>();
        String sql = "SELECT * FROM organizations ORDER BY name";
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                organizations.add(extractOrganizationFromResultSet(rs));
            }
        }
        return organizations;
    }
    
    // ==================== UPDATE ====================
    
    public void updateOrganization(Organization org) throws SQLException {
        String sql = "UPDATE organizations SET name = ?, type = ?, contact_info = ? WHERE id = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, org.getName());
            stmt.setString(2, org.getType());
            stmt.setString(3, org.getContactInfo());
            stmt.setInt(4, org.getOrganizationId());
            stmt.executeUpdate();
        }
    }
    
    // ==================== DELETE ====================
    
    public void deleteOrganization(int id) throws SQLException {
        // First check if organization has users
        String checkUsersSql = "SELECT COUNT(*) FROM users WHERE organization_id = ?";
        try (PreparedStatement checkStmt = DBConnection.getConnection().prepareStatement(checkUsersSql)) {
            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("لا يمكن حذف المنظمة لأنها تحتوي على مستخدمين");
            }
        }
        
        String sql = "DELETE FROM organizations WHERE id = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
    
    // ==================== VALIDATION ====================
    
    public boolean isOrganizationNameExists(String name) throws SQLException {
        String sql = "SELECT COUNT(*) FROM organizations WHERE name = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    
    public boolean isOrganizationNameExistsExcept(String name, int excludeId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM organizations WHERE name = ? AND id != ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setInt(2, excludeId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }
    
    // ==================== STATISTICS ====================
    
    public int getTotalOrganizationsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM organizations";
        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    // ==================== HELPER ====================
    
    private Organization extractOrganizationFromResultSet(ResultSet rs) throws SQLException {
        Organization org = new Organization();
        org.setOrganizationId(rs.getInt("id"));
        org.setName(rs.getString("name"));
        org.setType(rs.getString("type"));
        org.setContactInfo(rs.getString("contact_info"));
        return org;
    }
}