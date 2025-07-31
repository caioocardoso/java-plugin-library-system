package br.edu.ifba.inf008.shell;

import br.edu.ifba.inf008.interfaces.IUIController;
import br.edu.ifba.inf008.interfaces.ICore;
import br.edu.ifba.inf008.shell.PluginController;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Pos;
import java.util.function.Supplier;

public class UIController extends Application implements IUIController {

    private ICore core;
    private MenuBar menuBar;
    private TabPane tabPane;
    private HBox quickAccessBox;
    private static UIController uiController;

    public UIController() {
    }

    @Override
    public void init() {
        uiController = this;
    }

    public static UIController getInstance() {
        return uiController;
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Library Management System");

        menuBar = new MenuBar();

        VBox vBox = new VBox(menuBar);

        tabPane = new TabPane();
        tabPane.setSide(Side.BOTTOM);

        Image logoImage = new Image(UIController.class.getResourceAsStream("/images/logo.png"));
        ImageView logoView = new ImageView(logoImage);
        logoView.setFitWidth(200);
        logoView.setPreserveRatio(true);

        Label welcomeLabel = new Label("Library Management System");
        welcomeLabel.getStyleClass().add("welcome-label");

        Label subtitleLabel = new Label("Your central hub for managing users, books, and loans.");
        subtitleLabel.getStyleClass().add("home-subtitle");

        Button manageUsersBtn = new Button("Manage Users");
        manageUsersBtn.getStyleClass().add("quick-button");

        Button manageBooksBtn = new Button("Manage Books");
        manageBooksBtn.getStyleClass().add("quick-button");

        quickAccessBox = new HBox(15);
        quickAccessBox.setAlignment(Pos.CENTER);

        VBox homePane = new VBox(15, logoView, welcomeLabel, subtitleLabel, quickAccessBox);
        homePane.setAlignment(Pos.CENTER);
        homePane.setId("home-pane");

        VBox.setVgrow(tabPane, Priority.ALWAYS);

        Tab homeTab = new Tab("Home");
        homeTab.setContent(homePane);
        homeTab.setClosable(false);

        tabPane.getTabs().add(homeTab);

        vBox.getChildren().addAll(tabPane);

        Scene scene = new Scene(vBox, 960, 600);

        scene.getStylesheets().add(UIController.class.getResource("/css/app-styles.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();

        Core.getInstance().getPluginController().init();
    }

    public MenuItem createMenuItem(String menuText, String menuItemText) {
        // Criar o menu caso ele nao exista
        Menu newMenu = null;
        for (Menu menu : menuBar.getMenus()) {
            if (menuText.equals(menu.getText())) {
                newMenu = menu;
                break;
            }
        }
        if (newMenu == null) {
            newMenu = new Menu(menuText);
            menuBar.getMenus().add(newMenu);
        }

        // Criar o menu item neste menu
        MenuItem menuItem = new MenuItem(menuItemText);
        newMenu.getItems().add(menuItem);

        return menuItem;
    }

    public boolean showTab(String tabText, Supplier<Node> contentSupplier) {
        for (Tab tab : tabPane.getTabs()) {
            if (tabText.equals(tab.getText())) {
                tabPane.getSelectionModel().select(tab);
                return true;
            }
        }

        Node contents = contentSupplier.get();

        Tab newTab = new Tab(tabText);
        newTab.setContent(contents);
        tabPane.getTabs().add(newTab);

        tabPane.getSelectionModel().select(newTab);

        return true;
    }

    @Override
    public Button addQuickAccessButton(String text, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add("quick-button");
        button.setOnAction(e -> action.run());
        quickAccessBox.getChildren().add(button);
        return button;
    }
}
