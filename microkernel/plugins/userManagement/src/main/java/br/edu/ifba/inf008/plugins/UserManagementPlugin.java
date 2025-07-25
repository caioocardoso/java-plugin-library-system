package br.edu.ifba.inf008.plugins;

import br.edu.ifba.inf008.shell.model.User;
import br.edu.ifba.inf008.interfaces.ICore;
import br.edu.ifba.inf008.interfaces.IPlugin;
import br.edu.ifba.inf008.interfaces.IUIController;
import br.edu.ifba.inf008.plugins.data.UserDAO;
import br.edu.ifba.inf008.plugins.data.UserDAOImpl;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;

public class UserManagementPlugin implements IPlugin {
    private final UserDAO userDAO = new UserDAOImpl();
    private TableView<User> userTable = new TableView<>();
    private ObservableList<User> masterData = FXCollections.observableArrayList();
    private TextField nameField = new TextField();
    private TextField emailField = new TextField();
    private Button saveButton = new Button("Add User");
    private User selectedUser = null;

    private IUIController uiController;

    @Override
    public boolean init() {
        this.uiController = ICore.getInstance().getUIController();
        MenuItem menuItem = uiController.createMenuItem("Menu", "Users");

        menuItem.setOnAction(e -> showUserManagementTab());

        Button usersButton = uiController.addQuickAccessButton("", () -> showUserManagementTab());
        Image userIconImage = new Image(getClass().getResourceAsStream("/br/edu/ifba/inf008/plugins/images/userIcon.png"));
        ImageView userIconView = new ImageView(userIconImage);
        userIconView.setFitWidth(48);
        userIconView.setFitHeight(48);
        usersButton.setGraphic(userIconView);

        return true;
    }

    private void showUserManagementTab() {
        VBox userPane = createManagementPane();
        userPane.getStylesheets().add(getClass().getResource("/br/edu/ifba/inf008/plugins/css/user-styles.css").toExternalForm());
        userPane.getStyleClass().add("main-pane");
        loadUserData();
        uiController.createTab("User Management", userPane);
    }

    private VBox createManagementPane() {
        TextField searchField = new TextField();
        searchField.setPromptText("Search by name or email...");

        FilteredList<User> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                if (user.getName().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (user.getEmail().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
        });
        userTable.setItems(filteredData);
        userTable.getStyleClass().add("table-view");

        setupTableColumns();

        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedUser = newSelection;
            if (selectedUser != null) {
                nameField.setText(selectedUser.getName());
                emailField.setText(selectedUser.getEmail());
                saveButton.setText("Update User");
            } else {
                clearForm();
            }
        });

        GridPane formPane = createFormPane();
        formPane.getStyleClass().add("form-pane");

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(e -> handleDelete());
        deleteButton.setDisable(true);
        userTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> deleteButton.setDisable(newVal == null));

        HBox topBar = new HBox(10, new Label("Search:"), searchField, deleteButton);
        topBar.setPadding(new Insets(0, 0, 10, 0));
        topBar.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        VBox mainPane = new VBox(15, topBar, userTable, formPane);
        mainPane.setPadding(new Insets(20));
        VBox.setVgrow(userTable, Priority.ALWAYS);

        return mainPane;
    }

    private void setupTableColumns() {
        userTable.getColumns().clear();
        TableColumn<User, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        TableColumn<User, LocalDateTime> registeredCol = new TableColumn<>("Registered At");
        registeredCol.setCellValueFactory(new PropertyValueFactory<>("registeredAt"));
        userTable.getColumns().addAll(nameCol, emailCol, registeredCol);
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private GridPane createFormPane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);

        nameField.setPromptText("Name");
        emailField.setPromptText("Email");

        Button clearButton = new Button("Clear");
        clearButton.setOnAction(e -> clearForm());

        saveButton.setOnAction(e -> handleSave());

        nameField.getStyleClass().add("text-field");
        emailField.getStyleClass().add("text-field");
        saveButton.getStyleClass().add("button");
        clearButton.getStyleClass().add("button");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        HBox buttonBox = new HBox(10, saveButton, clearButton);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        grid.add(buttonBox, 1, 2);

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        return grid;
    }

    private void loadUserData() {
        try {
            masterData.setAll(userDAO.getAllUsers());
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load users: " + ex.getMessage());
        }
    }

    private void handleSave() {
        String name = nameField.getText();
        String email = emailField.getText();

        if (name.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Name and Email cannot be empty.");
            return;
        }

        try {
            if (selectedUser == null) {
                User newUser = new User();
                newUser.setName(name);
                newUser.setEmail(email);
                userDAO.addUser(newUser);
                showAlert(Alert.AlertType.INFORMATION, "Success", "User added successfully.");
            } else {
                selectedUser.setName(name);
                selectedUser.setEmail(email);
                userDAO.updateUser(selectedUser);
                showAlert(Alert.AlertType.INFORMATION, "Success", "User updated successfully.");
            }
            loadUserData();
            clearForm();
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Operation failed: " + ex.getMessage());
        }
    }

    private void handleDelete() {
        if (selectedUser == null)
            return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete " + selectedUser.getName() + "?", ButtonType.YES, ButtonType.NO);
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    userDAO.deleteUser(selectedUser.getUserId());
                    showAlert(Alert.AlertType.INFORMATION, "Success", "User deleted successfully.");
                    loadUserData();
                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete user: " + ex.getMessage());
                }
            }
        });
    }

    private void clearForm() {
        selectedUser = null;
        userTable.getSelectionModel().clearSelection();
        nameField.clear();
        emailField.clear();
        saveButton.setText("Add User");
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
