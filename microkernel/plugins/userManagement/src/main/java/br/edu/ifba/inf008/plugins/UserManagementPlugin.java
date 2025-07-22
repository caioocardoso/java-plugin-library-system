package br.edu.ifba.inf008.plugins;

import br.edu.ifba.inf008.interfaces.IPlugin;
import br.edu.ifba.inf008.interfaces.ICore;
import br.edu.ifba.inf008.interfaces.IUIController;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;

public class UserManagementPlugin implements IPlugin {
    @Override
    public boolean init() {
        IUIController uiController = ICore.getInstance().getUIController();
        MenuItem menuItem = uiController.createMenuItem("Users", "Manage users");

        menuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                VBox userPane = new VBox();
                Label title = new Label("TEST");
                Button btnAddUser = new Button("Add new user");

                userPane.getChildren().addAll(title, btnAddUser);

                uiController.createTab("Usuários", userPane);
            }
        });
        return true;
    }
}