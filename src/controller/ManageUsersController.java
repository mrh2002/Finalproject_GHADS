package controller;

import dao.OrganizationDAO;
import dao.UserDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import models.Organization;
import models.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

public class ManageUsersController {

    @FXML
    private TableView<User> usersTable;
    @FXML
    private TableColumn<User, Integer> idColumn;
    @FXML
    private TableColumn<User, String> nameColumn;
    @FXML
    private TableColumn<User, String> usernameColumn;
    @FXML
    private TableColumn<User, String> emailColumn;
    @FXML
    private TableColumn<User, String> roleColumn;
    @FXML
    private TableColumn<User, String> organizationColumn;
    @FXML
    private TableColumn<User, String> photoColumn;  // عمود الصورة الجديد

    @FXML
    private TextField fullNameField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField emailField;
    @FXML
    private ComboBox<String> roleComboBox;
    @FXML
    private ComboBox<Organization> organizationComboBox;
    @FXML
    private TextField searchField;
    
    // عناصر رفع الصورة
    @FXML
    private Button selectPhotoButton;
    @FXML
    private Button clearPhotoButton;
    @FXML
    private ImageView photoPreviewImageView;
    @FXML
    private Label photoPathLabel;

    @FXML
    private Button addButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button resetButton;
    @FXML
    private Button refreshButton;

    private UserDAO userDAO = new UserDAO();
    private OrganizationDAO orgDAO = new OrganizationDAO();
    private ObservableList<User> usersList = FXCollections.observableArrayList();
    private ObservableList<Organization> organizationsList = FXCollections.observableArrayList();
    private User selectedUser;
    private File selectedPhotoFile;
    private static final String PHOTO_DIRECTORY = "uploads/photos/";

    @FXML
    private void initialize() {
        // إنشاء مجلد الصور إذا لم يكن موجوداً
        createPhotoDirectory();
        
        setupTableColumns();
        setupComboBoxes();
        loadUsers();
        loadOrganizations();
        setupSelectionListener();
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

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        organizationColumn.setCellValueFactory(new PropertyValueFactory<>("organizationName"));
        
        // عمود الصورة - عرض أيقونة
        photoColumn.setCellValueFactory(new PropertyValueFactory<>("photoPath"));
        photoColumn.setCellFactory(col -> new TableCell<User, String>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitWidth(32);
                imageView.setFitHeight(32);
                imageView.setPreserveRatio(true);
            }
            
            @Override
            protected void updateItem(String photoPath, boolean empty) {
                super.updateItem(photoPath, empty);
                if (empty || photoPath == null || photoPath.isEmpty()) {
                    setGraphic(null);
                    setText("");
                } else {
                    try {
                        File file = new File(photoPath);
                        if (file.exists()) {
                            Image image = new Image(file.toURI().toString(), 32, 32, true, true);
                            imageView.setImage(image);
                            setGraphic(imageView);
                            setText("");
                        } else {
                            setGraphic(null);
                            setText("❌");
                        }
                    } catch (Exception e) {
                        setGraphic(null);
                        setText("");
                    }
                }
            }
        });
    }

    private void setupPhotoButtons() {
        selectPhotoButton.setOnAction(e -> selectPhoto());
        clearPhotoButton.setOnAction(e -> clearPhoto());
    }

    private void selectPhoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("اختر صورة المستخدم");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp")
        );
        
        File selectedFile = fileChooser.showOpenDialog(selectPhotoButton.getScene().getWindow());
        if (selectedFile != null) {
            selectedPhotoFile = selectedFile;
            photoPathLabel.setText(selectedFile.getName());
            try {
                Image image = new Image(selectedFile.toURI().toString(), 100, 100, true, true);
                photoPreviewImageView.setImage(image);
            } catch (Exception e) {
                showAlert("خطأ", "فشل تحميل الصورة: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void clearPhoto() {
        selectedPhotoFile = null;
        photoPathLabel.setText("لم يتم اختيار صورة");
        photoPreviewImageView.setImage(null);
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

    private void setupComboBoxes() {
        roleComboBox.setItems(FXCollections.observableArrayList("ADMIN", "COORDINATOR"));
        roleComboBox.setValue("COORDINATOR");
        
        // عند تغيير الدور، تمكين/تعطيل اختيار المنظمة
        roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("COORDINATOR".equals(newVal)) {
                organizationComboBox.setDisable(false);
            } else {
                organizationComboBox.setDisable(true);
                organizationComboBox.setValue(null);
            }
        });
    }

    private void loadOrganizations() {
        try {
            organizationsList.clear();
            organizationsList.addAll(orgDAO.getAllOrganizations());
            organizationComboBox.setItems(organizationsList);
        } catch (Exception e) {
            showAlert("خطأ", "فشل تحميل المنظمات: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadUsers() {
        try {
            usersList.clear();
            usersList.addAll(userDAO.getAllUsers());
            usersTable.setItems(usersList);
        } catch (Exception e) {
            showAlert("خطأ", "فشل تحميل المستخدمين: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupSelectionListener() {
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedUser = newSelection;
                fullNameField.setText(newSelection.getFullName());
                usernameField.setText(newSelection.getUsername());
                emailField.setText(newSelection.getEmail());
                roleComboBox.setValue(newSelection.getRole());

                // Set organization in combo box
                for (Organization org : organizationsList) {
                    if (org.getOrganizationId() == newSelection.getOrganizationId()) {
                        organizationComboBox.setValue(org);
                        break;
                    }
                }

                passwordField.clear();
                
                // Load photo preview if exists
                if (newSelection.getPhotoPath() != null && !newSelection.getPhotoPath().isEmpty()) {
                    try {
                        File photoFile = new File(newSelection.getPhotoPath());
                        if (photoFile.exists()) {
                            Image image = new Image(photoFile.toURI().toString(), 100, 100, true, true);
                            photoPreviewImageView.setImage(image);
                            photoPathLabel.setText(photoFile.getName());
                        } else {
                            clearPhoto();
                        }
                    } catch (Exception e) {
                        clearPhoto();
                    }
                } else {
                    clearPhoto();
                }
                
                editButton.setDisable(false);
                deleteButton.setDisable(false);
                addButton.setDisable(true);
            } else {
                selectedUser = null;
                editButton.setDisable(true);
                deleteButton.setDisable(true);
                addButton.setDisable(false);
                resetForm();
            }
        });
    }

    @FXML
    private void handleAdd() {
        if (!validateFields()) {
            return;
        }

        try {
            // Check if username exists
            if (userDAO.isUsernameExists(usernameField.getText().trim())) {
                showAlert("خطأ", "اسم المستخدم موجود بالفعل", Alert.AlertType.ERROR);
                return;
            }

            // Check if email exists
            if (userDAO.isEmailExists(emailField.getText().trim())) {
                showAlert("خطأ", "البريد الإلكتروني موجود بالفعل", Alert.AlertType.ERROR);
                return;
            }

            User user = new User();
            user.setFullName(fullNameField.getText().trim());
            user.setUsername(usernameField.getText().trim());
            user.setPassword(passwordField.getText().trim());
            user.setEmail(emailField.getText().trim());
            user.setRole(roleComboBox.getValue());
            
            // Save photo
            String photoPath = savePhoto();
            user.setPhotoPath(photoPath);

            Organization selectedOrg = organizationComboBox.getValue();
            if (selectedOrg != null && "COORDINATOR".equals(user.getRole())) {
                user.setOrganizationId(selectedOrg.getOrganizationId());
            } else {
                user.setOrganizationId(0);
            }

            userDAO.createUser(user);
            showAlert("نجاح", "تم إضافة المستخدم بنجاح", Alert.AlertType.INFORMATION);
            resetForm();
            loadUsers();
        } catch (Exception e) {
            showAlert("خطأ", "فشل إضافة المستخدم: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEdit() {
        if (selectedUser == null) {
            showAlert("تنبيه", "الرجاء اختيار مستخدم للتعديل", Alert.AlertType.WARNING);
            return;
        }

        if (!validateFieldsForEdit()) {
            return;
        }

        try {
            // Check if username exists for another user
            if (userDAO.isUsernameExistsExcept(usernameField.getText().trim(), selectedUser.getUserId())) {
                showAlert("خطأ", "اسم المستخدم موجود بالفعل", Alert.AlertType.ERROR);
                return;
            }

            // Check if email exists for another user
            if (userDAO.isEmailExistsExcept(emailField.getText().trim(), selectedUser.getUserId())) {
                showAlert("خطأ", "البريد الإلكتروني موجود بالفعل", Alert.AlertType.ERROR);
                return;
            }

            selectedUser.setFullName(fullNameField.getText().trim());
            selectedUser.setUsername(usernameField.getText().trim());
            selectedUser.setEmail(emailField.getText().trim());
            selectedUser.setRole(roleComboBox.getValue());

            // Update photo if new one selected
            if (selectedPhotoFile != null) {
                String newPhotoPath = savePhoto();
                if (newPhotoPath != null) {
                    // Delete old photo if exists
                    if (selectedUser.getPhotoPath() != null && !selectedUser.getPhotoPath().isEmpty()) {
                        try {
                            Files.deleteIfExists(Paths.get(selectedUser.getPhotoPath()));
                        } catch (IOException e) {
                            // Ignore deletion error
                        }
                    }
                    selectedUser.setPhotoPath(newPhotoPath);
                }
            }

            Organization selectedOrg = organizationComboBox.getValue();
            if (selectedOrg != null && "COORDINATOR".equals(selectedUser.getRole())) {
                selectedUser.setOrganizationId(selectedOrg.getOrganizationId());
            } else {
                selectedUser.setOrganizationId(0);
            }

            // Update password if provided
            if (!passwordField.getText().trim().isEmpty()) {
                if (passwordField.getText().trim().length() < 8) {
                    showAlert("خطأ", "كلمة المرور يجب أن تكون 8 أحرف على الأقل", Alert.AlertType.ERROR);
                    return;
                }
                userDAO.updatePasswordOnly(selectedUser.getUserId(), passwordField.getText().trim());
            }

            userDAO.updateUser(selectedUser);
            showAlert("نجاح", "تم تعديل المستخدم بنجاح", Alert.AlertType.INFORMATION);
            resetForm();
            loadUsers();
        } catch (Exception e) {
            showAlert("خطأ", "فشل تعديل المستخدم: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedUser == null) {
            showAlert("تنبيه", "الرجاء اختيار مستخدم للحذف", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("تأكيد الحذف");
        confirm.setHeaderText(null);
        confirm.setContentText("هل أنت متأكد من حذف المستخدم: " + selectedUser.getFullName() + "؟");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Delete photo file if exists
                if (selectedUser.getPhotoPath() != null && !selectedUser.getPhotoPath().isEmpty()) {
                    try {
                        Files.deleteIfExists(Paths.get(selectedUser.getPhotoPath()));
                    } catch (IOException e) {
                        // Ignore deletion error
                    }
                }
                
                userDAO.deleteUser(selectedUser.getUserId());
                showAlert("نجاح", "تم حذف المستخدم بنجاح", Alert.AlertType.INFORMATION);
                resetForm();
                loadUsers();
            } catch (Exception e) {
                showAlert("خطأ", "فشل حذف المستخدم: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void resetForm() {
        fullNameField.clear();
        usernameField.clear();
        passwordField.clear();
        emailField.clear();
        roleComboBox.setValue("COORDINATOR");
        organizationComboBox.setValue(null);
        organizationComboBox.setDisable(false);
        clearPhoto();
        usersTable.getSelectionModel().clearSelection();
        selectedUser = null;
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        addButton.setDisable(false);
        selectedPhotoFile = null;
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
        loadOrganizations();
        resetForm();
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        if (searchTerm.isEmpty()) {
            loadUsers();
            return;
        }

        ObservableList<User> filtered = FXCollections.observableArrayList();
        for (User user : usersList) {
            if (user.getFullName().toLowerCase().contains(searchTerm)
                    || user.getUsername().toLowerCase().contains(searchTerm)
                    || user.getEmail().toLowerCase().contains(searchTerm)) {
                filtered.add(user);
            }
        }
        usersTable.setItems(filtered);
    }

    private boolean validateFields() {
        if (fullNameField.getText().trim().isEmpty()) {
            showAlert("تنبيه", "الرجاء إدخال الاسم الكامل", Alert.AlertType.WARNING);
            return false;
        }
        if (usernameField.getText().trim().isEmpty()) {
            showAlert("تنبيه", "الرجاء إدخال اسم المستخدم", Alert.AlertType.WARNING);
            return false;
        }
        if (passwordField.getText().trim().isEmpty()) {
            showAlert("تنبيه", "الرجاء إدخال كلمة المرور", Alert.AlertType.WARNING);
            return false;
        }
        if (passwordField.getText().trim().length() < 8) {
            showAlert("تنبيه", "كلمة المرور يجب أن تكون 8 أحرف على الأقل", Alert.AlertType.WARNING);
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
        if ("COORDINATOR".equals(roleComboBox.getValue()) && organizationComboBox.getValue() == null) {
            showAlert("تنبيه", "الرجاء اختيار المنظمة للمنسق", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private boolean validateFieldsForEdit() {
        if (fullNameField.getText().trim().isEmpty()) {
            showAlert("تنبيه", "الرجاء إدخال الاسم الكامل", Alert.AlertType.WARNING);
            return false;
        }
        if (usernameField.getText().trim().isEmpty()) {
            showAlert("تنبيه", "الرجاء إدخال اسم المستخدم", Alert.AlertType.WARNING);
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
        if ("COORDINATOR".equals(roleComboBox.getValue()) && organizationComboBox.getValue() == null) {
            showAlert("تنبيه", "الرجاء اختيار المنظمة للمنسق", Alert.AlertType.WARNING);
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