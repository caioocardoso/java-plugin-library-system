package br.edu.ifba.inf008.plugins;

import br.edu.ifba.inf008.shell.model.Book;
import br.edu.ifba.inf008.shell.model.Loan;
import br.edu.ifba.inf008.shell.model.User;
import br.edu.ifba.inf008.interfaces.ICore;
import br.edu.ifba.inf008.interfaces.IPlugin;
import br.edu.ifba.inf008.interfaces.IUIController;
import br.edu.ifba.inf008.plugins.data.ReportDAO;
import br.edu.ifba.inf008.plugins.data.ReportDAOImpl;

import java.sql.SQLException;
import java.time.LocalDate;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ReportPlugin implements IPlugin {

    private final ReportDAO reportDAO = new ReportDAOImpl();
    private TableView<Loan> reportTable = new TableView<>();
    private IUIController uiController;
    private CheckBox activeLoansSwitch;
    private Label statusLabel;

    @Override
    public boolean init() {
        this.uiController = ICore.getInstance().getUIController();

        Button reportsButton = uiController.addQuickAccessButton("", () -> {
            uiController.showTab("Report", () -> {
                VBox reportPane = createReportPane();

                reportPane.getStylesheets().add(
                        getClass().getResource("/br/edu/ifba/inf008/plugins/css/report-styles.css").toExternalForm());
                reportPane.getStyleClass().add("main-pane");

                loadReportData();

                return reportPane;
            });
        });
        
        Image reportsIconImage = new Image(getClass().getResourceAsStream("/br/edu/ifba/inf008/plugins/images/reportIcon.png"));
        ImageView reportsIconView = new ImageView(reportsIconImage);
        reportsIconView.setFitWidth(48);
        reportsIconView.setFitHeight(48);
        reportsButton.setGraphic(reportsIconView);

        return true;
    }

    private VBox createReportPane() {
        setupTableColumns();

        Label switchLabel = new Label("Active Loans:");
        switchLabel.getStyleClass().add("switch-label");
        
        activeLoansSwitch = new CheckBox();
        activeLoansSwitch.setSelected(true);
        activeLoansSwitch.setOnAction(e -> {
            updateSwitchStatus();
            loadReportData();
        });
        activeLoansSwitch.getStyleClass().add("switch");

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> loadReportData());
        refreshButton.getStyleClass().add("button");

        HBox topBar = new HBox(10, switchLabel, activeLoansSwitch, refreshButton);
        topBar.setAlignment(Pos.CENTER_LEFT);

        statusLabel = new Label("Showing: Active Loans");
        statusLabel.getStyleClass().add("status-label");

        VBox mainPane = new VBox(15, topBar, reportTable, statusLabel);
        mainPane.setPadding(new Insets(20));
        VBox.setVgrow(reportTable, Priority.ALWAYS);
        return mainPane;
    }

    private void updateSwitchStatus() {
        if (activeLoansSwitch.isSelected()) {
            statusLabel.setText("Showing: Active Loans");
        } else {
            statusLabel.setText("Showing: Returned Loans");
        }
    }

    private void setupTableColumns() {
        reportTable.getColumns().clear();

        TableColumn<Loan, Book> titleCol = new TableColumn<>("Book Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("book"));
        titleCol.setCellFactory(col -> new TableCell<Loan, Book>() {
            @Override
            protected void updateItem(Book book, boolean empty) {
                super.updateItem(book, empty);
                setText(empty || book == null ? null : book.getTitle());
            }
        });

        TableColumn<Loan, Book> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("book"));
        authorCol.setCellFactory(col -> new TableCell<Loan, Book>() {
            @Override
            protected void updateItem(Book book, boolean empty) {
                super.updateItem(book, empty);
                setText(empty || book == null ? null : book.getAuthor());
            }
        });

        TableColumn<Loan, User> userCol = new TableColumn<>("User");
        userCol.setCellValueFactory(new PropertyValueFactory<>("user"));
        userCol.setCellFactory(col -> new TableCell<Loan, User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? null : user.getName());
            }
        });

        TableColumn<Loan, LocalDate> loanDateCol = new TableColumn<>("Loan Date");
        loanDateCol.setCellValueFactory(new PropertyValueFactory<>("loanDate"));

        if (activeLoansSwitch != null && !activeLoansSwitch.isSelected()) {
            TableColumn<Loan, LocalDate> returnDateCol = new TableColumn<>("Return Date");
            returnDateCol.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
            reportTable.getColumns().addAll(titleCol, authorCol, userCol, loanDateCol, returnDateCol);
        } else {
            reportTable.getColumns().addAll(titleCol, authorCol, userCol, loanDateCol);
        }

        reportTable.getStyleClass().add("table-view");
        reportTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadReportData() {
        try {
            setupTableColumns();
            
            if (activeLoansSwitch != null && activeLoansSwitch.isSelected()) {
                reportTable.setItems(FXCollections.observableArrayList(reportDAO.getActiveLoans()));
            } else {
                reportTable.setItems(FXCollections.observableArrayList(reportDAO.getReturnedLoans()));
            }
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load the report: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        alert.showAndWait();
    }
}