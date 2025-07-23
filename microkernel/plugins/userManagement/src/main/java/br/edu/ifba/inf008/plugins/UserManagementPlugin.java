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

public class UserManagementPlugin implements IPlugin {
    private final UserDAO userDAO = new UserDAOImpl();
    private TableView<User> userTable = new TableView<>();

    @Override
    public boolean init() {
        IUIController uiController = ICore.getInstance().getUIController();
        MenuItem menuItem = uiController.createMenuItem("Users", "Manage Users");

        menuItem.setOnAction(e -> {
            VBox userPane = createManagementPane();
            loadUserData();
            uiController.createTab("User Management", userPane);
        });

        return true;
    }

    private VBox createManagementPane() {
        // --- 1. Configuração da Tabela ---
        TableColumn<User, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<User, LocalDateTime> registeredCol = new TableColumn<>("Registered At");
        registeredCol.setCellValueFactory(new PropertyValueFactory<>("registeredAt"));

        userTable.getColumns().addAll(nameCol, emailCol, registeredCol);
        userTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button addButton = new Button("Add...");
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

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}   