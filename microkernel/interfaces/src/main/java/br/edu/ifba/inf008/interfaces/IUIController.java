package br.edu.ifba.inf008.interfaces;

import javafx.scene.control.MenuItem;
import javafx.scene.Node;
import javafx.scene.control.Button;

public interface IUIController
{
    public abstract MenuItem createMenuItem(String menuText, String menuItemText);
    public abstract boolean createTab(String tabText, Node contents);
    public abstract Button addQuickAccessButton(String text, Runnable action);
}
