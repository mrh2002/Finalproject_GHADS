package controller;

import dao.OrganizationDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Organization;

import java.util.Optional;

public class ManageOrganizationsController {

    @FXML
    private TableView<Organization> organizationsTable;
    @FXML
    private TableColumn<Organization, Integer> idColumn;
    @FXML
    private TableColumn<Organization, String> nameColumn;
    @FXML
    private TableColumn<Organization, String> typeColumn;
    @FXML
    private TableColumn<Organization, String> contactColumn;

    @FXML
    private TextField nameField;
    @FXML
    private TextField typeField;
    @FXML
    private TextField contactField;
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

    private OrganizationDAO orgDAO = new OrganizationDAO();
    private ObservableList<Organization> organizationsList = FXCollections.observableArrayList();
    private Organization selectedOrganization;

    @FXML
    private void initialize() {
        setupTableColumns();
        loadOrganizations();
        setupSelectionListener();
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("organizationId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contactInfo"));
    }

    private void loadOrganizations() {
        try {
            organizationsList.clear();
            organizationsList.addAll(orgDAO.getAllOrganizations());
            organizationsTable.setItems(organizationsList);
        } catch (Exception e) {
            showAlert("خطأ", "فشل تحميل المنظمات: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupSelectionListener() {
        organizationsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedOrganization = newSelection;
                nameField.setText(newSelection.getName());
                typeField.setText(newSelection.getType());
                contactField.setText(newSelection.getContactInfo());
                editButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                selectedOrganization = null;
                editButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });
    }

    @FXML
    private void handleAdd() {
        if (!validateFields()) {
            return;
        }

        try {
            Organization org = new Organization();
            org.setName(nameField.getText().trim());
            org.setType(typeField.getText().trim());
            org.setContactInfo(contactField.getText().trim());

            orgDAO.createOrganization(org);
            showAlert("نجاح", "تم إضافة المنظمة بنجاح", Alert.AlertType.INFORMATION);
            resetForm();
            loadOrganizations();
        } catch (Exception e) {
            showAlert("خطأ", "فشل إضافة المنظمة: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleEdit() {
        if (selectedOrganization == null) {
            showAlert("تنبيه", "الرجاء اختيار منظمة للتعديل", Alert.AlertType.WARNING);
            return;
        }

        if (!validateFields()) {
            return;
        }

        try {
            selectedOrganization.setName(nameField.getText().trim());
            selectedOrganization.setType(typeField.getText().trim());
            selectedOrganization.setContactInfo(contactField.getText().trim());

            orgDAO.updateOrganization(selectedOrganization);
            showAlert("نجاح", "تم تعديل المنظمة بنجاح", Alert.AlertType.INFORMATION);
            resetForm();
            loadOrganizations();
        } catch (Exception e) {
            showAlert("خطأ", "فشل تعديل المنظمة: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedOrganization == null) {
            showAlert("تنبيه", "الرجاء اختيار منظمة للحذف", Alert.AlertType.WARNING);
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("تأكيد الحذف");
        confirm.setHeaderText(null);
        confirm.setContentText("هل أنت متأكد من حذف المنظمة: " + selectedOrganization.getName() + "؟");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                orgDAO.deleteOrganization(selectedOrganization.getOrganizationId());
                showAlert("نجاح", "تم حذف المنظمة بنجاح", Alert.AlertType.INFORMATION);
                resetForm();
                loadOrganizations();
            } catch (Exception e) {
                showAlert("خطأ", "فشل حذف المنظمة: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void resetForm() {
        nameField.clear();
        typeField.clear();
        contactField.clear();
        organizationsTable.getSelectionModel().clearSelection();
        selectedOrganization = null;
        editButton.setDisable(true);
        deleteButton.setDisable(true);
    }

    @FXML
    private void handleRefresh() {
        loadOrganizations();
        resetForm();
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        if (searchTerm.isEmpty()) {
            loadOrganizations();
            return;
        }

        ObservableList<Organization> filtered = FXCollections.observableArrayList();
        for (Organization org : organizationsList) {
            if (org.getName().toLowerCase().contains(searchTerm)
                    || org.getType().toLowerCase().contains(searchTerm)) {
                filtered.add(org);
            }
        }
        organizationsTable.setItems(filtered);
    }

    private boolean validateFields() {
        if (nameField.getText().trim().isEmpty()) {
            showAlert("تنبيه", "الرجاء إدخال اسم المنظمة", Alert.AlertType.WARNING);
            return false;
        }
        if (typeField.getText().trim().isEmpty()) {
            showAlert("تنبيه", "الرجاء إدخال نوع المنظمة", Alert.AlertType.WARNING);
            return false;
        }
        if (contactField.getText().trim().isEmpty()) {
            showAlert("تنبيه", "الرجاء إدخال معلومات الاتصال", Alert.AlertType.WARNING);
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
