package br.edu.ifba.inf008.interfaces;

import javafx.scene.control.MenuItem;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import java.util.function.Supplier;

public interface IUIController {
    public abstract MenuItem createMenuItem(String menuText, String menuItemText);

    public abstract boolean showTab(String tabText, Supplier<Node> contentSupplier);

    public abstract Button addQuickAccessButton(String text, Runnable action);
}
