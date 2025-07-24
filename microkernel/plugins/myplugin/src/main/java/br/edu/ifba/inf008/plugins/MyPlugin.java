package br.edu.ifba.inf008.plugins;

import br.edu.ifba.inf008.interfaces.IPlugin;
import br.edu.ifba.inf008.interfaces.ICore;
import br.edu.ifba.inf008.interfaces.IUIController;

import javafx.scene.control.MenuItem;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;

public class MyPlugin implements IPlugin {
    public boolean init() {
        IUIController uiController = ICore.getInstance().getUIController();

        MenuItem menuItem = uiController.createMenuItem("Menu", "Book");
        menuItem.setOnAction(e -> {
            uiController.createTab("Book", new Rectangle(200, 200, Color.LIGHTSTEELBLUE));
        });

        // uiController.addQuickAccessButton("Manage Books", () -> showUserManagementTab());

        return true;
    }
}
