package controller;

import dao.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.SessionManager;
import models.User;

public class ChangePasswordController {

    @FXML
    private PasswordField currentPasswordField;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label errorLabel;

    @FXML
    private Button changeButton;
    @FXML
    private Button clearButton;

    private UserDAO userDAO = new UserDAO();
    private User currentUser;

    @FXML
    private void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
    }

    @FXML
    private void handleChangePassword() {
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validation
        if (currentPassword.isEmpty()) {
            showError("الرجاء إدخال كلمة المرور الحالية");
            return;
        }

        if (newPassword.isEmpty()) {
            showError("الرجاء إدخال كلمة المرور الجديدة");
            return;
        }

        if (newPassword.length() < 8) {
            showError("كلمة المرور الجديدة يجب أن تكون 8 أحرف على الأقل");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("كلمة المرور الجديدة وتأكيدها غير متطابقين");
            return;
        }

        if (currentPassword.equals(newPassword)) {
            showError("كلمة المرور الجديدة يجب أن تكون مختلفة عن القديمة");
            return;
        }

        try {
            userDAO.changePassword(currentUser.getUserId(), currentPassword, newPassword);
            showAlert("نجاح", "تم تغيير كلمة المرور بنجاح", Alert.AlertType.INFORMATION);
            clearForm();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void clearForm() {
        currentPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();
        errorLabel.setText("");
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setStyle("-fx-text-fill: red;");
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
