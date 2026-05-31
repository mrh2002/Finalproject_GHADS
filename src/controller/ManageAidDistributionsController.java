package controller;

import dao.AidDistributionDAO;
import dao.FamilyDAO;
import dao.OrganizationDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.*;
import java.time.LocalDate;
import java.util.Optional;

public class ManageAidDistributionsController {

    @FXML
    private TableView<AidDistribution> distributionsTable;
    @FXML
    private TableColumn<AidDistribution, Integer> idColumn;
    @FXML
    private TableColumn<AidDistribution, String> familyNameColumn;
    @FXML
    private TableColumn<AidDistribution, String> organizationColumn;
    @FXML
    private TableColumn<AidDistribution, String> aidTypeColumn;
    @FXML
    private TableColumn<AidDistribution, String> dateColumn;
    @FXML
    private TableColumn<AidDistribution, String> coordinatorColumn;

    @FXML
    private ComboBox<Family> familyComboBox;
    @FXML
    private ComboBox<String> aidTypeComboBox;
    @FXML
    private DatePicker distributionDatePicker;
    @FXML
    private TextArea notesArea;
    @FXML
    private ComboBox<String> filterOrganizationComboBox;
    @FXML
    private TextField searchField;
    @FXML
    private RadioButton mostVulnerableRadio;
    @FXML
    private RadioButton unservedRadio;
    @FXML
    private ToggleGroup searchToggleGroup;

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

    private AidDistributionDAO aidDAO = new AidDistributionDAO();
    private FamilyDAO familyDAO = new FamilyDAO();
    private OrganizationDAO orgDAO = new OrganizationDAO();
    private ObservableList<AidDistribution> distributionsList = FXCollections.observableArrayList();
    private ObservableList<Family> familiesList = FXCollections.observableArrayList();
    private AidDistribution selectedDistribution;
    private User currentUser;
    private boolean isAdmin;

    @FXML
    private void initialize() {
        currentUser = SessionManager.getInstance().getCurrentUser();
        isAdmin = SessionManager.getInstance().isAdmin();

        setupTableColumns();
        setupComboBoxes();
        loadDistributions();
        loadFamilies();
        loadOrganizationsFilter();
        setupSelectionListener();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("distributionId"));
        familyNameColumn.setCellValueFactory(new PropertyValueFactory<>("familyName"));
        organizationColumn.setCellValueFactory(new PropertyValueFactory<>("organizationName"));
        aidTypeColumn.setCellValueFactory(new PropertyValueFactory<>("aidType"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("distributionDate"));
        coordinatorColumn.setCellValueFactory(new PropertyValueFactory<>("coordinatorName"));
    }

    private void setupComboBoxes() {
        // Aid types
        aidTypeComboBox.setItems(FXCollections.observableArrayList(
                "طعام", "دواء", "إيواء", "ملابس", "مياه", "نظافة", "تعليم", "مساعدات نقدية", "أخرى"
        ));
        aidTypeComboBox.setValue("طعام");

        distributionDatePicker.setValue(LocalDate.now());

        // Search toggle group
        searchToggleGroup = new ToggleGroup();
        mostVulnerableRadio.setToggleGroup(searchToggleGroup);
        unservedRadio.setToggleGroup(searchToggleGroup);
    }

    private void loadFamilies() {
        try {
            familiesList.clear();
            familiesList.addAll(familyDAO.getAllFamilies());
            familyComboBox.setItems(familiesList);
        } catch (Exception e) {
            showAlert("خطأ", "فشل تحميل العائلات: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadOrganizationsFilter() {
        try {
            filterOrganizationComboBox.setItems(FXCollections.observableArrayList("الكل"));
            orgDAO.getAllOrganizations().forEach(org
                    -> filterOrganizationComboBox.getItems().add(org.getName())
            );
            filterOrganizationComboBox.setValue("الكل");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadDistributions() {
        try {
            distributionsList.clear();
            if (isAdmin) {
                distributionsList.addAll(aidDAO.getAllAidDistributions());
            } else {
                distributionsList.addAll(aidDAO.getDistributionsByOrganization(currentUser.getOrganizationId()));
            }
            distributionsTable.setItems(distributionsList);
        } catch (Exception e) {
            showAlert("خطأ", "فشل تحميل المساعدات: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupSelectionListener() {
        distributionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedDistribution = newSelection;

                // Select family in combo box
                for (Family family : familiesList) {
                    if (family.getFamilyId() == newSelection.getFamilyId()) {
                        familyComboBox.setValue(family);
                        break;
                    }
                }

                aidTypeComboBox.setValue(newSelection.getAidType());
                distributionDatePicker.setValue(newSelection.getDistributionDate());
                notesArea.setText(newSelection.getNotes());

                editButton.setDisable(false);
                deleteButton.setDisable(false);
                addButton.setDisable(true);
            } else {
                selectedDistribution = null;
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

        Family selectedFamily = familyComboBox.getValue();
        if (selectedFamily == null) {
            showAlert("تنبيه", "الرجاء اختيار عائلة", Alert.AlertType.WARNING);
            return;
        }

        try {
            // Check duplicate before saving
            AidDistributionDAO.DuplicateCheckResult check = aidDAO.canDistributeAid(
                    selectedFamily.getFamilyId(),
                    aidTypeComboBox.getValue()
            );

            if (!check.isAllowed()) {
                showAlert("منع التوزيع", check.getMessage(), Alert.AlertType.ERROR);
                return;
            }

            AidDistribution distribution = new AidDistribution();
            distribution.setFamilyId(selectedFamily.getFamilyId());
            distribution.setOrganizationId(isAdmin ? 1 : currentUser.getOrganizationId());
            distribution.setCoordinatorId(currentUser.getUserId());
            distribution.setAidType(aidTypeComboBox.getValue());
            distribution.setDistributionDate(distributionDatePicker.getValue());
            distribution.setNotes(notesArea.getText());

            aidDAO.createAidDistribution(distribution);
            showAlert("نجاح", "تم تسجيل المساعدة بنجاح\n" + check.getMessage(), Alert.AlertType.INFORMATION);
            resetForm();
            loadDistributions();
            loadFamilies(); // Refresh families to update served status
        } catch (Exception e) {
            showAlert("خطأ", "فشل تسجيل المساعدة: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEdit() {
        if (selectedDistribution == null) {
            showAlert("تنبيه", "الرجاء اختيار مساعدة للتعديل", Alert.AlertType.WARNING);
            return;
        }

        if (!validateFields()) {
            return;
        }

        try {
            selectedDistribution.setAidType(aidTypeComboBox.getValue());
            selectedDistribution.setDistributionDate(distributionDatePicker.getValue());
            selectedDistribution.setNotes(notesArea.getText());

            aidDAO.updateAidDistribution(selectedDistribution);
            showAlert("نجاح", "تم تعديل المساعدة بنجاح", Alert.AlertType.INFORMATION);
            resetForm();
            loadDistributions();
        } catch (Exception e) {
            showAlert("خطأ", "فشل تعديل المساعدة: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedDistribution == null) {
            showAlert("تنبيه", "الرجاء اختيار مساعدة للحذف", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("تأكيد الحذف");
        confirm.setHeaderText(null);
        confirm.setContentText("هل أنت متأكد من حذف سجل المساعدة؟");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                aidDAO.deleteAidDistribution(selectedDistribution.getDistributionId());
                showAlert("نجاح", "تم حذف المساعدة بنجاح", Alert.AlertType.INFORMATION);
                resetForm();
                loadDistributions();
            } catch (Exception e) {
                showAlert("خطأ", "فشل حذف المساعدة: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void resetForm() {
        familyComboBox.setValue(null);
        aidTypeComboBox.setValue("طعام");
        distributionDatePicker.setValue(LocalDate.now());
        notesArea.clear();
        distributionsTable.getSelectionModel().clearSelection();
        selectedDistribution = null;
        editButton.setDisable(true);
        deleteButton.setDisable(true);
        addButton.setDisable(false);
    }

    @FXML
    private void handleRefresh() {
        loadDistributions();
        loadFamilies();
        resetForm();
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        String filterOrg = filterOrganizationComboBox.getValue();

        ObservableList<AidDistribution> filtered = FXCollections.observableArrayList();

        for (AidDistribution dist : distributionsList) {
            boolean matchesSearch = searchTerm.isEmpty()
                    || dist.getFamilyName().toLowerCase().contains(searchTerm)
                    || dist.getAidType().toLowerCase().contains(searchTerm);

            boolean matchesOrg = filterOrg.equals("الكل")
                    || dist.getOrganizationName().equals(filterOrg);

            if (matchesSearch && matchesOrg) {
                filtered.add(dist);
            }
        }
        distributionsTable.setItems(filtered);
    }

    @FXML
    private void searchVulnerableFamilies() {
        try {
            familiesList.clear();
            if (mostVulnerableRadio.isSelected()) {
                familiesList.addAll(familyDAO.getMostVulnerableFamilies());
            } else if (unservedRadio.isSelected()) {
                familiesList.addAll(familyDAO.getUnservedFamilies());
            } else {
                familiesList.addAll(familyDAO.getAllFamilies());
            }
            familyComboBox.setItems(familiesList);

            if (!familiesList.isEmpty()) {
                showAlert("نتائج البحث", "تم العثور على " + familiesList.size() + " عائلة", Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            showAlert("خطأ", "فشل البحث: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private boolean validateFields() {
        if (familyComboBox.getValue() == null) {
            showAlert("تنبيه", "الرجاء اختيار عائلة", Alert.AlertType.WARNING);
            return false;
        }
        if (aidTypeComboBox.getValue() == null || aidTypeComboBox.getValue().isEmpty()) {
            showAlert("تنبيه", "الرجاء إدخال نوع المساعدة", Alert.AlertType.WARNING);
            return false;
        }
        if (distributionDatePicker.getValue() == null) {
            showAlert("تنبيه", "الرجاء اختيار تاريخ التوزيع", Alert.AlertType.WARNING);
            return false;
        }
        if (distributionDatePicker.getValue().isAfter(LocalDate.now())) {
            showAlert("تنبيه", "تاريخ التوزيع لا يمكن أن يكون في المستقبل", Alert.AlertType.WARNING);
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
