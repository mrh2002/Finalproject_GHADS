package dao;

import models.User;
import utils.DBConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    // ==================== AUTHENTICATION ====================
    public User login(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        }
        return null;
    }

    public boolean authenticate(String username, String password) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    // ==================== READ ====================
    public User getUserById(int id) throws SQLException {
        String sql = "SELECT u.*, o.name as organization_name FROM users u "
                + "LEFT JOIN organizations o ON u.organization_id = o.id "
                + "WHERE u.id = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                User user = extractUserFromResultSet(rs);
                user.setOrganizationName(rs.getString("organization_name"));
                return user;
            }
        }
        return null;
    }

    public User getUserByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        }
        return null;
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.*, o.name as organization_name FROM users u "
                + "LEFT JOIN organizations o ON u.organization_id = o.id "
                + "ORDER BY u.id";
        try (Statement stmt = DBConnection.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User user = extractUserFromResultSet(rs);
                user.setOrganizationName(rs.getString("organization_name"));
                users.add(user);
            }
        }
        return users;
    }

    public List<User> getUsersByRole(String role) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.*, o.name as organization_name FROM users u "
                + "LEFT JOIN organizations o ON u.organization_id = o.id "
                + "WHERE u.role = ? ORDER BY u.full_name";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, role);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                User user = extractUserFromResultSet(rs);
                user.setOrganizationName(rs.getString("organization_name"));
                users.add(user);
            }
        }
        return users;
    }

    public List<User> getUsersByOrganization(int organizationId) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.*, o.name as organization_name FROM users u "
                + "LEFT JOIN organizations o ON u.organization_id = o.id "
                + "WHERE u.organization_id = ? ORDER BY u.full_name";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, organizationId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                User user = extractUserFromResultSet(rs);
                user.setOrganizationName(rs.getString("organization_name"));
                users.add(user);
            }
        }
        return users;
    }

    // ==================== CREATE ====================
    public void createUser(User user) throws SQLException {
        String sql = "INSERT INTO users (full_name, username, password, email, role, organization_id) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getEmail());
            stmt.setString(5, user.getRole());
            stmt.setObject(6, user.getOrganizationId() > 0 ? user.getOrganizationId() : null);
            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                user.setUserId(generatedKeys.getInt(1));
            }
        }
    }

    // ==================== UPDATE ====================
    public void updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, email = ?, organization_id = ? WHERE id = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getEmail());
            stmt.setObject(3, user.getOrganizationId() > 0 ? user.getOrganizationId() : null);
            stmt.setInt(4, user.getUserId());
            stmt.executeUpdate();
        }
    }

    public void updateUserProfile(User user) throws SQLException {
        String sql = "UPDATE users SET full_name = ?, email = ? WHERE id = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getEmail());
            stmt.setInt(3, user.getUserId());
            stmt.executeUpdate();
        }
    }

    // ==================== DELETE ====================
    public void deleteUser(int id) throws SQLException {
        // Check if user has aid distributions
        String checkSql = "SELECT COUNT(*) FROM aid_distributions WHERE coordinator_id = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(checkSql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("لا يمكن حذف المستخدم لأنه قام بتسجيل مساعدات");
            }
        }

        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // ==================== PASSWORD MANAGEMENT ====================
    public void changePassword(int userId, String currentPassword, String newPassword) throws SQLException {
        // First verify current password
        String checkSql = "SELECT password FROM users WHERE id = ?";
        try (PreparedStatement checkStmt = DBConnection.getConnection().prepareStatement(checkSql)) {
            checkStmt.setInt(1, userId);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                if (!rs.getString("password").equals(currentPassword)) {
                    throw new SQLException("كلمة المرور الحالية غير صحيحة");
                }
            }
        }

        // Update password
        String updateSql = "UPDATE users SET password = ? WHERE id = ?";
        try (PreparedStatement updateStmt = DBConnection.getConnection().prepareStatement(updateSql)) {
            updateStmt.setString(1, newPassword);
            updateStmt.setInt(2, userId);
            updateStmt.executeUpdate();
        }
    }

    public void updatePasswordOnly(int userId, String newPassword) throws SQLException {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, newPassword);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }

    // ==================== VALIDATION METHODS ====================
    public boolean isUsernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public boolean isUsernameExistsExcept(String username, int excludeUserId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ? AND id != ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setInt(2, excludeUserId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public boolean isEmailExists(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    public boolean isEmailExistsExcept(String email, int excludeUserId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ? AND id != ?";
        try (PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.setInt(2, excludeUserId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    // ==================== STATISTICS METHODS ====================
    /**
     * Get total number of coordinators (users with role 'COORDINATOR')
     */
    public int getTotalCoordinatorsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'COORDINATOR'";
        try (Statement stmt = DBConnection.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Get total number of all users
     */
    public int getTotalUsersCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        try (Statement stmt = DBConnection.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Get total number of admins
     */
    public int getTotalAdminsCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'";
        try (Statement stmt = DBConnection.getConnection().createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    // ==================== HELPER METHODS ====================
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("id"));
        user.setFullName(rs.getString("full_name"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setRole(rs.getString("role"));
        user.setOrganizationId(rs.getObject("organization_id") != null ? rs.getInt("organization_id") : 0);
        return user;
    }
}
