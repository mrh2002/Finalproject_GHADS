package controller;

import dao.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.*;

import java.util.Optional;

public class CoordinatorDashboardController {

    @FXML
    private Label totalFamiliesLabel;
    @FXML
    private Label servedByMyOrgLabel;
    @FXML
    private Label unservedFamiliesLabel;
    @FXML
    private Label welcomeLabel;
    @FXML
    private Label orgNameLabel;

    @FXML
    private VBox contentArea;

    private FamilyDAO familyDAO = new FamilyDAO();
    private UserDAO userDAO = new UserDAO();
    private AidDistributionDAO aidDAO = new AidDistributionDAO();
    private User currentUser;

    // متغير لتتبع حالة الثيم
    private boolean isDarkTheme = false;

    @FXML
    private void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        welcomeLabel.setText("مرحباً, " + currentUser.getFullName());

        // Load organization name
        try {
            OrganizationDAO orgDAO = new OrganizationDAO();
            Organization org = orgDAO.getOrganizationById(currentUser.getOrganizationId());
            if (org != null) {
                orgNameLabel.setText("المنظمة: " + org.getName());
                SessionManager.getInstance().setCurrentOrganization(org);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        loadDashboardStats();
    }

    private void loadDashboardStats() {
        try {
            int familiesCount = familyDAO.getTotalFamiliesCount();
            int servedCount = familyDAO.getFamiliesServedByOrganization(currentUser.getOrganizationId());
            int unservedCount = familyDAO.getUnservedFamiliesCount();

            totalFamiliesLabel.setText(String.valueOf(familiesCount));
            servedByMyOrgLabel.setText(String.valueOf(servedCount));
            unservedFamiliesLabel.setText(String.valueOf(unservedCount));
        } catch (Exception e) {
            showAlert("خطأ", "فشل تحميل الإحصائيات: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void loadDashboard() {
        loadDashboardStats();
        contentArea.getChildren().clear();
    }

    @FXML
    private void showFamilies() {
        loadContent("/view/ManageFamilies.fxml");
    }

    @FXML
    private void showAidDistribution() {
        loadContent("/view/ManageAidDistributions.fxml");
    }

    @FXML
    private void showProfile() {
        loadContent("/view/Profile.fxml");
    }

    @FXML
    private void showChangePassword() {
        loadContent("/view/ChangePassword.fxml");
    }

    @FXML
    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("تسجيل الخروج");
        alert.setHeaderText(null);
        alert.setContentText("هل أنت متأكد من رغبتك في تسجيل الخروج؟");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            SessionManager.getInstance().clearSession();
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/view/Login.fxml"));
                Stage stage = (Stage) welcomeLabel.getScene().getWindow();
                stage.setTitle("GHADS - تسجيل الدخول");
                stage.getScene().setRoot(root);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleExit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("إغلاق التطبيق");
        alert.setHeaderText(null);
        alert.setContentText("هل أنت متأكد من رغبتك في إغلاق التطبيق؟");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.exit(0);
        }
    }

    @FXML
    private void changeFontSize(javafx.event.ActionEvent event) {
        MenuItem menuItem = (MenuItem) event.getSource();
        String size = menuItem.getText();
        Scene scene = welcomeLabel.getScene();

        // التحقق من وجود stylesheet
        if (scene.getStylesheets().isEmpty()) {
            scene.getStylesheets().add(getClass().getResource("/view/styles.css").toExternalForm());
        }

        String currentCss = scene.getStylesheets().get(0);
        scene.getStylesheets().clear();
        scene.getStylesheets().add(currentCss);

        int fontSize;
        switch (size) {
            case "Small":
                fontSize = 12;
                break;
            case "Large":
                fontSize = 18;
                break;
            default:
                fontSize = 14;
                break;
        }

        scene.getRoot().setStyle("-fx-font-size: " + fontSize + "px;");
        showInfo("تغيير حجم الخط", "تم تغيير حجم الخط إلى: " + size);
    }

    @FXML
    private void changeFontFamily(javafx.event.ActionEvent event) {
        MenuItem menuItem = (MenuItem) event.getSource();
        String fontFamily = menuItem.getText();
        Scene scene = welcomeLabel.getScene();

        // التحقق من وجود stylesheet
        if (scene.getStylesheets().isEmpty()) {
            scene.getStylesheets().add(getClass().getResource("/view/styles.css").toExternalForm());
        }

        String currentCss = scene.getStylesheets().get(0);
        scene.getStylesheets().clear();
        scene.getStylesheets().add(currentCss);

        scene.getRoot().setStyle("-fx-font-family: '" + fontFamily + "';");
        showInfo("تغيير الخط", "تم تغيير الخط إلى: " + fontFamily);
    }

    @FXML
    private void changeTheme() {
        Scene scene = welcomeLabel.getScene();

        // إذا لم يكن هناك stylesheet، أضف الوضع العادي
        if (scene.getStylesheets().isEmpty()) {
            scene.getStylesheets().add(getClass().getResource("/view/styles.css").toExternalForm());
            isDarkTheme = false;
            showInfo("تغيير الثيم", "تم تغيير الثيم إلى الوضع العادي");
            return;
        }

        if (isDarkTheme) {
            // التبديل إلى الوضع العادي
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/view/styles.css").toExternalForm());
            scene.getRoot().setStyle("");
            isDarkTheme = false;
            showInfo("تغيير الثيم", "تم تغيير الثيم إلى الوضع العادي");
        } else {
            // التبديل إلى الوضع الداكن
            scene.getStylesheets().clear();
            // التحقق من وجود ملف dark-theme.css
            if (getClass().getResource("/view/dark-theme.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/view/dark-theme.css").toExternalForm());
            } else {
                // تطبيق ألوان داكنة مباشرة إذا لم يكن الملف موجوداً
                scene.getRoot().setStyle("-fx-base: #333333; -fx-background-color: #2c3e50;");
            }
            isDarkTheme = true;
            showInfo("تغيير الثيم", "تم تغيير الثيم إلى الوضع الداكن");
        }
    }

    @FXML
    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("عن النظام");
        alert.setHeaderText("GHADS - Gaza Humanitarian Aid Distribution System");
        alert.setContentText("الإصدار: 1.0\nالمطور: GHADS Team\n\nنظام لتنسيق توزيع المساعدات الإنسانية في غزة");
        alert.showAndWait();
    }

    private void loadContent(String fxmlPath) {
        try {
            Parent content = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().clear();
            contentArea.getChildren().add(content);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("خطأ", "فشل تحميل المحتوى: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        showAlert(title, content, Alert.AlertType.INFORMATION);
    }
}
