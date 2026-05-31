package controller;

import dao.UserDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import models.SessionManager;
import models.User;

public class LoginController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private Button loginButton;
    @FXML private Button exitButton;
    
    private UserDAO userDAO = new UserDAO();
    
    @FXML
    private void initialize() {
        // Add Enter key listener
        passwordField.setOnAction(event -> handleLogin());
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        
        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            showError("الرجاء إدخال اسم المستخدم وكلمة المرور");
            return;
        }
        
        try {
            User user = userDAO.login(username, password);
            
            if (user != null) {
                // Store user in session
                SessionManager.getInstance().setCurrentUser(user);
                
                // Redirect based on role
                if (user.getRole().equals("ADMIN")) {
                    loadAdminDashboard();
                } else {
                    loadCoordinatorDashboard();
                }
            } else {
                showError("اسم المستخدم أو كلمة المرور غير صحيحة");
            }
        } catch (Exception e) {
            showError("خطأ في الاتصال بقاعدة البيانات: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleExit() {
        System.exit(0);
    }
    
    private void loadAdminDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/AdminDashboard.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setTitle("GHADS - لوحة تحكم المسؤول");
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            showError("فشل تحميل لوحة التحكم: " + e.getMessage());
        }
    }
    
    private void loadCoordinatorDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/CoordinatorDashboard.fxml"));
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setTitle("GHADS - لوحة تحكم المنسق");
            stage.getScene().setRoot(root);
        } catch (Exception e) {
            e.printStackTrace();
            showError("فشل تحميل لوحة التحكم: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;");
    }
}