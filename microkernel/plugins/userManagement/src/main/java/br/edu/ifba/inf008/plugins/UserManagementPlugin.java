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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import java.util.Optional;
import javafx.scene.shape.SVGPath;
import javafx.scene.control.Label;
import javafx.scene.Group;
import javafx.scene.Node;

public class UserManagementPlugin implements IPlugin {
    private final UserDAO userDAO = new UserDAOImpl();
    private TableView<User> userTable = new TableView<>();

    private IUIController uiController;

    @Override
    public boolean init() {
        this.uiController = ICore.getInstance().getUIController();
        MenuItem menuItem = uiController.createMenuItem("Menu", "Users");

        menuItem.setOnAction(e -> showUserManagementTab());

        Button usersButton = uiController.addQuickAccessButton("", () -> showUserManagementTab());
        usersButton.setGraphic(createUsersIcon());

        return true;
    }

    private Node createUsersIcon() {
        SVGPath path1 = new SVGPath();
        path1.setContent(
                "M458.159,404.216c-18.93-33.65-49.934-71.764-100.409-93.431c-28.868,20.196-63.938,32.087-101.745,32.087   c-37.828,0-72.898-11.89-101.767-32.087c-50.474,21.667-81.479,59.782-100.398,93.431C28.731,448.848,48.417,512,91.842,512   c43.426,0,164.164,0,164.164,0s120.726,0,164.153,0C463.583,512,483.269,448.848,458.159,404.216z");
        path1.getStyleClass().add("quick-button-icon");

        SVGPath path2 = new SVGPath();
        path2.setContent(
                "M256.005,300.641c74.144,0,134.231-60.108,134.231-134.242v-32.158C390.236,60.108,330.149,0,256.005,0   c-74.155,0-134.252,60.108-134.252,134.242V166.4C121.753,240.533,181.851,300.641,256.005,300.641z");
        path2.getStyleClass().add("quick-button-icon");

        return new Group(path1, path2);
    }

    private void showUserManagementTab() {
        VBox userPane = createManagementPane();
        loadUserData();
        uiController.createTab("User Management", userPane);
    }

    private VBox createManagementPane() {
        TableColumn<User, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<User, LocalDateTime> registeredCol = new TableColumn<>("Registered At");
        registeredCol.setCellValueFactory(new PropertyValueFactory<>("registeredAt"));

        userTable.getColumns().addAll(nameCol, emailCol, registeredCol);
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button addButton = new Button("Add...");
        addButton.setOnAction(e -> addUser());
        Button editButton = new Button("Edit...");
        Button deleteButton = new Button("Delete");

        editButton.setDisable(true);
        deleteButton.setDisable(true);
        userTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean isItemSelected = newSelection != null;
            editButton.setDisable(!isItemSelected);
            deleteButton.setDisable(!isItemSelected);
        });

        HBox buttonBox = new HBox(10, addButton, editButton, deleteButton);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        VBox mainPane = new VBox(10, userTable, buttonBox);
        mainPane.setPadding(new Insets(10));
        VBox.setVgrow(userTable, Priority.ALWAYS);

        return mainPane;
    }

    private void loadUserData() {
        try {
            List<User> userList = userDAO.getAllUsers();
            ObservableList<User> observableUserList = FXCollections.observableArrayList(userList);
            userTable.setItems(observableUserList);
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load users: " + ex.getMessage());
        }
    }

    private void addUser() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Add User");
        dialog.setHeaderText("Enter the informations of the new user.");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                User newUser = new User();
                newUser.setName(nameField.getText());
                newUser.setEmail(emailField.getText());
                return newUser;
            }
            return null;
        });

        Optional<User> result = dialog.showAndWait();

        result.ifPresent(user -> {
            if (user.getName().isEmpty() || user.getEmail().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Name and Email cannot be empty.");
                return;
            }
            try {
                userDAO.addUser(user);
                showAlert(Alert.AlertType.INFORMATION, "Success", "User added successfully.");
                loadUserData();
            } catch (SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add user: " + ex.getMessage());
            }
        });
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}