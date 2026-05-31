package controller;

import dao.FamilyDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Family;
import models.SessionManager;
import models.VulnerabilityLevel;

import java.time.LocalDate;
import java.util.Optional;

public class ManageFamiliesController {

    @FXML
    private TableView<Family> familiesTable;
    @FXML
    private TableColumn<Family, Integer> idColumn;
    @FXML
    private TableColumn<Family, String> nationalIdColumn;
    @FXML
    private TableColumn<Family, String> headNameColumn;
    @FXML
    private TableColumn<Family, String> phoneColumn;
    @FXML
    private TableColumn<Family, String> addressColumn;
    @FXML
    private TableColumn<Family, Integer> membersCountColumn;
    @FXML
    private TableColumn<Family, String> vulnerabilityColumn;

    @FXML
    private TextField nationalIdField;
    @FXML
    private TextField headNameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField addressField;
    @FXML
    private Spinner<Integer> membersCountSpinner;
    @FXML
    private ComboBox<String> vulnerabilityComboBox;
    @FXML
    private DatePicker registrationDatePicker;
    @FXML
    private TextField searchField;

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

    private FamilyDAO familyDAO = new FamilyDAO();
    private ObservableList<Family> familiesList = FXCollections.observableArrayList();
    private Family selectedFamily;
    private boolean isAdmin = true;

    @FXML
    private void initialize() {
        isAdmin = SessionManager.getInstance().isAdmin();
        setupTableColumns();
        setupSpinner();
        setupComboBoxes();
        loadFamilies();
        setupSelectionListener();

        if (!isAdmin) {
            // Hide admin-only features if needed
        }
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("familyId"));
        nationalIdColumn.setCellValueFactory(new PropertyValueFactory<>("nationalId"));
        headNameColumn.setCellValueFactory(new PropertyValueFactory<>("headName"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        membersCountColumn.setCellValueFactory(new PropertyValueFactory<>("familyMembersCount"));
        vulnerabilityColumn.setCellValueFactory(new PropertyValueFactory<>("vulnerabilityLevel"));
    }

    private void setupSpinner() {
        SpinnerValueFactory<Integer> valueFactory
                = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 1);
        membersCountSpinner.setValueFactory(valueFactory);
    }

    private void setupComboBoxes() {
        vulnerabilityComboBox.setItems(FXCollections.observableArrayList(
                VulnerabilityLevel.HIGH.getArabicName(),
                VulnerabilityLevel.MEDIUM.getArabicName(),
                VulnerabilityLevel.LOW.getArabicName()
        ));
        vulnerabilityComboBox.setValue(VulnerabilityLevel.MEDIUM.getArabicName());
        registrationDatePicker.setValue(LocalDate.now());
    }

    private void loadFamilies() {
        try {
            familiesList.clear();
            familiesList.addAll(familyDAO.getAllFamilies());
            familiesTable.setItems(familiesList);
        } catch (Exception e) {
            showAlert("خطأ", "فشل تحميل العائلات: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupSelectionListener() {
        familiesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedFamily = newSelection;
                nationalIdField.setText(newSelection.getNationalId());
                headNameField.setText(newSelection.getHeadName());
                phoneField.setText(newSelection.getPhone());
                addressField.setText(newSelection.getAddress());
                membersCountSpinner.getValueFactory().setValue(newSelection.getFamilyMembersCount());

                // Convert vulnerability level to Arabic for display
                String arabicLevel = VulnerabilityLevel.fromString(newSelection.getVulnerabilityLevel()).getArabicName();
                vulnerabilityComboBox.setValue(arabicLevel);

                registrationDatePicker.setValue(newSelection.getRegistrationDate());
                editButton.setDisable(false);
                deleteButton.setDisable(false);
                addButton.setDisable(true);
            } else {
                selectedFamily = null;
                editButton.setDisable(true);
                deleteButton.setDisable(true);
                addButton.setDisable(false);
            }
        });
    }

    @FXML
    private void handleAdd() {
        if (!validateFields()) {
            return;
        }

        try {
            // Check if national ID exists
            if (familyDAO.isNationalIdExists(nationalIdField.getText().trim())) {
                showAlert("خطأ", "رقم الهوية الوطنية موجود بالفعل", Alert.AlertType.ERROR);
                return;
            }

            Family family = new Family();
            family.setNationalId(nationalIdField.getText().trim());
            family.setHeadName(headNameField.getText().trim());
            family.setPhone(phoneField.getText().trim());
            family.setAddress(addressField.getText().trim());
            family.setFamilyMembersCount(membersCountSpinner.getValue());

            // Convert Arabic vulnerability level to English
            String arabicLevel = vulnerabilityComboBox.getValue();
            String englishLevel = VulnerabilityLevel.fromString(arabicLevel).name();
            family.setVulnerabilityLevel(englishLevel);

            family.setRegistrationDate(registrationDatePicker.getValue());

            familyDAO.createFamily(family);
            showAlert("نجاح", "تم إضافة العائلة بنجاح", Alert.AlertType.INFORMATION);
            resetForm();
            loadFamilies();
        } catch (Exception e) {
            showAlert("خطأ", "فشل إضافة العائلة: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEdit() {
        if (selectedFamily == null) {
            showAlert("تنبيه", "الرجاء اختيار عائلة للتعديل", Alert.AlertType.WARNING);
            return;
        }

        if (!validateFields()) {
            return;
        }

        try {
            selectedFamily.setNationalId(nationalIdField.getText().trim());
            selectedFamily.setHeadName(headNameField.getText().trim());
            selectedFamily.setPhone(phoneField.getText().trim());
            selectedFamily.setAddress(addressField.getText().trim());
            selectedFamily.setFamilyMembersCount(membersCountSpinner.getValue());

            // Convert Arabic vulnerability level to English
            String arabicLevel = vulnerabilityComboBox.getValue();
            String englishLevel = VulnerabilityLevel.fromString(arabicLevel).name();
            selectedFamily.setVulnerabilityLevel(englishLevel);

            familyDAO.updateFamily(selectedFamily);
            showAlert("نجاح", "تم تعديل العائلة بنجاح", Alert.AlertType.INFORMATION);
            resetForm();
            loadFamilies();
        } catch (Exception e) {
            showAlert("خطأ", "فشل تعديل العائلة: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedFamily == null) {
            showAlert("تنبيه", "الرجاء اختيار عائلة للحذف", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("تأكيد الحذف");
        confirm.setHeaderText(null);
        confirm.setContentText("هل أنت متأكد من حذف العائلة: " + selectedFamily.getHeadName() + "؟");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                familyDAO.deleteFamily(selectedFamily.getFamilyId());
                showAlert("نجاح", "تم حذف العائلة بنجاح", Alert.AlertType.INFORMATION);
                resetForm();
                loadFamilies();
            } catch (Exception e) {
                showAlert("خطأ", "فشل حذف العائلة: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void resetForm() {
        nationalIdField.clear();
        headNameField.clear();
        phoneField.clear();
        addressField.clear();
        membersCountSpinner.getValueFactory().setValue(1);
        vulnerabilityComboBox.setValue(VulnerabilityLevel.MEDIUM.getArabicName());
        registrationDatePicker.setValue(LocalDate.now());
        familiesTable.getSelectionModel().clearSelection();
        selectedFamily = null;
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        addButton.setDisable(false);
    }

    @FXML
    private void handleRefresh() {
        loadFamilies();
        resetForm();
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        if (searchTerm.isEmpty()) {
            loadFamilies();
            return;
        }

        ObservableList<Family> filtered = FXCollections.observableArrayList();
        for (Family family : familiesList) {
            if (family.getHeadName().toLowerCase().contains(searchTerm)
                    || family.getNationalId().toLowerCase().contains(searchTerm)
                    || family.getPhone().toLowerCase().contains(searchTerm)) {
                filtered.add(family);
            }
        }
        familiesTable.setItems(filtered);
    }

    private boolean validateFields() {
        if (nationalIdField.getText().trim().isEmpty()) {
            showAlert("تنبيه", "الرجاء إدخال رقم الهوية الوطنية", Alert.AlertType.WARNING);
            return false;
        }
        if (headNameField.getText().trim().isEmpty()) {
            showAlert("تنبيه", "الرجاء إدخال اسم رب الأسرة", Alert.AlertType.WARNING);
            return false;
        }
        if (nationalIdField.getText().trim().length() < 9) {
            showAlert("تنبيه", "رقم الهوية الوطنية يجب أن يكون 9 أرقام على الأقل", Alert.AlertType.WARNING);
            return false;
        }
        if (registrationDatePicker.getValue() == null) {
            showAlert("تنبيه", "الرجاء اختيار تاريخ التسجيل", Alert.AlertType.WARNING);
            return false;
        }
        if (registrationDatePicker.getValue().isAfter(LocalDate.now())) {
            showAlert("تنبيه", "تاريخ التسجيل لا يمكن أن يكون في المستقبل", Alert.AlertType.WARNING);
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
