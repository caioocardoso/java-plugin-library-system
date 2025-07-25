package br.edu.ifba.inf008.plugins;

import br.edu.ifba.inf008.shell.model.Book;
import br.edu.ifba.inf008.shell.model.Loan;
import br.edu.ifba.inf008.shell.model.User;
import br.edu.ifba.inf008.interfaces.ICore;
import br.edu.ifba.inf008.interfaces.IPlugin;
import br.edu.ifba.inf008.interfaces.IUIController;
import br.edu.ifba.inf008.plugins.data.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Pos;
import javafx.event.ActionEvent;

public class LoanManagementPlugin implements IPlugin {

    private final LoanDAO loanDAO = new LoanDAOImpl();
    private final LoanDAO userDAO = new LoanDAOImpl();
    private final LoanDAO bookDAO = new LoanDAOImpl();

    private TableView<Loan> loanTable = new TableView<>();
    private ObservableList<Loan> masterData = FXCollections.observableArrayList();

    private IUIController uiController;

    @Override
    public boolean init() {
        this.uiController = ICore.getInstance().getUIController();
        MenuItem menuItem = uiController.createMenuItem("Menu", "Loans");
        menuItem.setOnAction(e -> showLoanManagementTab());

        Button loanButton = uiController.addQuickAccessButton("", () -> showLoanManagementTab());
        Image loanIconImage = new Image(getClass().getResourceAsStream("/br/edu/ifba/inf008/plugins/images/loanIcon.png"));
        ImageView loanIconView = new ImageView(loanIconImage);
        loanIconView.setFitWidth(68);
        loanIconView.setFitHeight(68);
        loanButton.setGraphic(loanIconView);
        return true;
    }

    private void showLoanManagementTab() {
        VBox loanPane = createManagementPane();
        loanPane.getStylesheets().add(getClass().getResource("/br/edu/ifba/inf008/plugins/css/loan-styles.css").toExternalForm());
        loanPane.getStyleClass().add("main-pane");
        loadLoanData();
        uiController.createTab("Loan Management", loanPane);
    }

    private VBox createManagementPane() {
        TextField searchField = new TextField();
        searchField.setPromptText("Search by user or book...");
        searchField.getStyleClass().add("search-field");

        CheckBox activeOnlyCheckBox = new CheckBox("Show only active loans");

        FilteredList<Loan> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(loan -> {
                if (newVal == null || newVal.isEmpty()) return !activeOnlyCheckBox.isSelected() || loan.getReturnDate() == null;
                String filter = newVal.toLowerCase();
                boolean matchesUser = loan.getUser() != null && loan.getUser().getName().toLowerCase().contains(filter);
                boolean matchesBook = loan.getBook() != null && loan.getBook().getTitle().toLowerCase().contains(filter);
                boolean active = !activeOnlyCheckBox.isSelected() || loan.getReturnDate() == null;
                return (matchesUser || matchesBook) && active;
            });
        });
        activeOnlyCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            searchField.fireEvent(new ActionEvent());
            filteredData.setPredicate(loan -> {
                String filter = searchField.getText();
                if (filter == null || filter.isEmpty()) return !newVal || loan.getReturnDate() == null;
                String lower = filter.toLowerCase();
                boolean matchesUser = loan.getUser() != null && loan.getUser().getName().toLowerCase().contains(lower);
                boolean matchesBook = loan.getBook() != null && loan.getBook().getTitle().toLowerCase().contains(lower);
                boolean active = !newVal || loan.getReturnDate() == null;
                return (matchesUser || matchesBook) && active;
            });
        });
        loanTable.setItems(filteredData);
        loanTable.getStyleClass().add("table-view");

        setupTableColumns();

        Button newLoanButton = new Button("New Loan...");
        newLoanButton.setOnAction(e -> handleNewLoan());

        Button returnLoanButton = new Button("Return Loan");
        returnLoanButton.setOnAction(e -> handleReturnLoan());
        returnLoanButton.setDisable(true);

        loanTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean isReturnable = (newSelection != null && newSelection.getReturnDate() == null);
            returnLoanButton.setDisable(!isReturnable);
        });

        HBox topBar = new HBox(10, new Label("Search:"), searchField);
        topBar.setPadding(new Insets(0, 0, 10, 0));
        topBar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        HBox bottomBar = new HBox(15, activeOnlyCheckBox, newLoanButton, returnLoanButton);
        bottomBar.setPadding(new Insets(10, 0, 0, 0));
        bottomBar.setAlignment(Pos.CENTER_RIGHT);

        VBox mainPane = new VBox(15, topBar, loanTable, bottomBar);
        mainPane.setPadding(new Insets(20));
        VBox.setVgrow(loanTable, Priority.ALWAYS);

        return mainPane;
    }

    private void setupTableColumns() {
        loanTable.getColumns().clear();
        TableColumn<Loan, String> userCol = new TableColumn<>("User");
        userCol.setCellValueFactory(new PropertyValueFactory<>("user"));
        TableColumn<Loan, String> bookCol = new TableColumn<>("Book Title");
        bookCol.setCellValueFactory(new PropertyValueFactory<>("book"));
        TableColumn<Loan, LocalDate> loanDateCol = new TableColumn<>("Loan Date");
        loanDateCol.setCellValueFactory(new PropertyValueFactory<>("loanDate"));
        TableColumn<Loan, LocalDate> returnDateCol = new TableColumn<>("Return Date");
        returnDateCol.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        loanTable.getColumns().addAll(bookCol, userCol, loanDateCol, returnDateCol);
        loanTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadLoanData() {
        try {
            masterData.setAll(loanDAO.getAllLoans());
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load loans: " + ex.getMessage());
        }
    }

    private void handleNewLoan() {
        Dialog<Loan> dialog = new Dialog<>();
        dialog.setTitle("Register New Loan");

        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().getStylesheets().add(getClass().getResource("/br/edu/ifba/inf008/plugins/css/loan-styles.css").toExternalForm());

        ComboBox<User> userComboBox = new ComboBox<>();
        ComboBox<Book> bookComboBox = new ComboBox<>();

        try {
            userComboBox.setItems(FXCollections.observableArrayList(userDAO.getAllUsers()));
            bookComboBox.setItems(FXCollections.observableArrayList(bookDAO.getAvailableBooks()));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load users or books.");
            return;
        }

        userComboBox.setConverter(new StringConverter<User>() {
            @Override
            public String toString(User user) {
                return user == null ? "" : user.getName();
            }

            @Override
            public User fromString(String s) {
                return null;
            }
        });

        bookComboBox.setConverter(new StringConverter<Book>() {
            @Override
            public String toString(Book book) {
                return book == null ? "" : book.getTitle();
            }

            @Override
            public Book fromString(String s) {
                return null;
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("User:"), 0, 0);
        grid.add(userComboBox, 1, 0);
        grid.add(new Label("Book:"), 0, 1);
        grid.add(bookComboBox, 1, 1);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                Loan newLoan = new Loan();
                newLoan.setUser(userComboBox.getValue());
                newLoan.setBook(bookComboBox.getValue());
                newLoan.setLoanDate(LocalDate.now());
                return newLoan;
            }
            return null;
        });

        Optional<Loan> result = dialog.showAndWait();
        result.ifPresent(loan -> {
            if (loan.getUser() == null || loan.getBook() == null) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "You must select a user and a book.");
                return;
            }
            try {
                loanDAO.addLoan(loan);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Loan registered successfully.");
                loadLoanData();
            } catch (IllegalStateException | SQLException ex) {
                showAlert(Alert.AlertType.ERROR, "Operation Failed", ex.getMessage());
            }
        });
    }

    private void handleReturnLoan() {
        Loan selectedLoan = loanTable.getSelectionModel().getSelectedItem();
        if (selectedLoan == null || selectedLoan.getReturnDate() != null) {
            showAlert(Alert.AlertType.WARNING, "Invalid Action", "Please select an active loan to return.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Return the book '" + selectedLoan.getBook().getTitle() + "'?", ButtonType.YES, ButtonType.NO);
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    loanDAO.returnLoan(selectedLoan.getLoanId());
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Book returned successfully.");
                    loadLoanData();
                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to return the book: " + ex.getMessage());
                }
            }
        });
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(getClass().getResource("/br/edu/ifba/inf008/plugins/css/loan-styles.css").toExternalForm());
        alert.showAndWait();
    }
}
