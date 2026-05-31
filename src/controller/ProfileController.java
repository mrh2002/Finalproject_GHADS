package controller;

import dao.UserDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import models.SessionManager;
import models.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class ProfileController {

    @FXML
    private Label userIdLabel;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField usernameField;
    @FXML
    private TextField emailField;
    @FXML
    private Label roleLabel;
    @FXML
    private Label organizationLabel;
    @FXML
    private Label createdAtLabel;
    
    // عناصر الصورة
    @FXML
    private ImageView profileImageView;
    @FXML
    private Button changePhotoButton;
    @FXML
    private Button removePhotoButton;

    @FXML
    private Button editButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button cancelButton;

    private UserDAO userDAO = new UserDAO();
    private User currentUser;
    private boolean isEditing = false;
    private File selectedPhotoFile;
    private static final String PHOTO_DIRECTORY = "uploads/photos/";

    @FXML
    private void initialize() {
        createPhotoDirectory();
        currentUser = SessionManager.getInstance().getCurrentUser();
        loadUserProfile();
        setEditingMode(false);
        setupPhotoButtons();
    }

    private void createPhotoDirectory() {
        try {
            Path path = Paths.get(PHOTO_DIRECTORY);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setupPhotoButtons() {
        changePhotoButton.setOnAction(e -> changePhoto());
        removePhotoButton.setOnAction(e -> removePhoto());
    }

    private void changePhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("اختر صورة شخصية");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "jpeg", "*.gif", "*.bmp")
        );
        
        File selectedFile = fileChooser.showOpenDialog(changePhotoButton.getScene().getWindow());
        if (selectedFile != null) {
            selectedPhotoFile = selectedFile;
            try {
                Image image = new Image(selectedFile.toURI().toString(), 120, 120, true, true);
                profileImageView.setImage(image);
            } catch (Exception e) {
                showAlert("خطأ", "فشل تحميل الصورة: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void removePhoto() {
        selectedPhotoFile = null;
        profileImageView.setImage(null);
        profileImageView.setStyle("-fx-background-color: #ecf0f1;");
    }

    private String savePhoto() {
        if (selectedPhotoFile == null) return null;
        
        try {
            String uniqueFileName = UUID.randomUUID().toString() + "_" + selectedPhotoFile.getName();
            Path targetPath = Paths.get(PHOTO_DIRECTORY + uniqueFileName);
            Files.copy(selectedPhotoFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return targetPath.toAbsolutePath().toString();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("خطأ", "فشل حفظ الصورة: " + e.getMessage(), Alert.AlertType.ERROR);
            return null;
        }
    }

    private void loadUserProfile() {
        try {
            User freshUser = userDAO.getUserById(currentUser.getUserId());
            if (freshUser != null) {
                currentUser = freshUser;
                userIdLabel.setText(String.valueOf(currentUser.getUserId()));
                fullNameField.setText(currentUser.getFullName());
                usernameField.setText(currentUser.getUsername());
                emailField.setText(currentUser.getEmail());
                roleLabel.setText(currentUser.getRole());

                if (currentUser.getOrganizationName() != null && !currentUser.getOrganizationName().isEmpty()) {
                    organizationLabel.setText(currentUser.getOrganizationName());
                } else {
                    organizationLabel.setText("لا يوجد");
                }
                
                // Load profile photo
                if (currentUser.getPhotoPath() != null && !currentUser.getPhotoPath().isEmpty()) {
                    File photoFile = new File(currentUser.getPhotoPath());
                    if (photoFile.exists()) {
                        Image image = new Image(photoFile.toURI().toString(), 120, 120, true, true);
                        profileImageView.setImage(image);
                    } else {
                        profileImageView.setImage(null);
                    }
                } else {
                    profileImageView.setImage(null);
                }
            }
        } catch (Exception e) {
            showAlert("خطأ", "فشل تحميل الملف الشخصي: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEdit() {
        setEditingMode(true);
    }

    @FXML
    private void handleSave() {
        if (!validateFields()) {
            return;
        }

        try {
            // Check if email exists for another user
            if (userDAO.isEmailExistsExcept(emailField.getText().trim(), currentUser.getUserId())) {
                showAlert("خطأ", "البريد الإلكتروني موجود بالفعل", Alert.AlertType.ERROR);
                return;
            }

            currentUser.setFullName(fullNameField.getText().trim());
            currentUser.setEmail(emailField.getText().trim());
            
            // Update photo if new one selected
            if (selectedPhotoFile != null) {
                String newPhotoPath = savePhoto();
                if (newPhotoPath != null) {
                    // Delete old photo if exists
                    if (currentUser.getPhotoPath() != null && !currentUser.getPhotoPath().isEmpty()) {
                        try {
                            Files.deleteIfExists(Paths.get(currentUser.getPhotoPath()));
                        } catch (IOException e) {
                            // Ignore deletion error
                        }
                    }
                    currentUser.setPhotoPath(newPhotoPath);
                }
            }

            userDAO.updateUserProfile(currentUser);
            SessionManager.getInstance().setCurrentUser(currentUser);

            showAlert("نجاح", "تم تحديث الملف الشخصي بنجاح", Alert.AlertType.INFORMATION);
            setEditingMode(false);
            loadUserProfile();
        } catch (Exception e) {
            showAlert("خطأ", "فشل تحديث الملف الشخصي: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancel() {
        loadUserProfile();
        setEditingMode(false);
        selectedPhotoFile = null;
    }

    private void setEditingMode(boolean editing) {
        isEditing = editing;
        fullNameField.setEditable(editing);
        emailField.setEditable(editing);
        usernameField.setEditable(false);
        
        // Enable/disable photo buttons in edit mode
        changePhotoButton.setVisible(editing);
        removePhotoButton.setVisible(editing);

        editButton.setVisible(!editing);
        saveButton.setVisible(editing);
        cancelButton.setVisible(editing);
    }

    private boolean validateFields() {
        if (fullNameField.getText().trim().isEmpty()) {
            showAlert("تنبيه", "الرجاء إدخال الاسم الكامل", Alert.AlertType.WARNING);
            return false;
        }
        if (emailField.getText().trim().isEmpty()) {
            showAlert("تنبيه", "الرجاء إدخال البريد الإلكتروني", Alert.AlertType.WARNING);
            return false;
        }
        if (!emailField.getText().trim().contains("@")) {
            showAlert("تنبيه", "البريد الإلكتروني غير صحيح", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}