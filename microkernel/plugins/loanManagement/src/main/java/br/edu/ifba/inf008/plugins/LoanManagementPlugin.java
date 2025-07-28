package br.edu.ifba.inf008.plugins;

import br.edu.ifba.inf008.shell.model.Book;
import br.edu.ifba.inf008.shell.model.Loan;
import br.edu.ifba.inf008.shell.model.User;
import br.edu.ifba.inf008.interfaces.ICore;
import br.edu.ifba.inf008.interfaces.IPlugin;
import br.edu.ifba.inf008.interfaces.IUIController;
import br.edu.ifba.inf008.plugins.data.LoanDAO;
import br.edu.ifba.inf008.plugins.data.LoanDAOImpl;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javafx.scene.layout.ColumnConstraints;

public class LoanManagementPlugin implements IPlugin {
    private final LoanDAO loanDAO = new LoanDAOImpl();
    private ObservableList<Loan> masterData = FXCollections.observableArrayList();
    private ObservableList<User> allUsersMasterData = FXCollections.observableArrayList();
    private ObservableList<Book> allBooksMasterData = FXCollections.observableArrayList();

    private TableView<Loan> loanTable = new TableView<>();
    private ComboBox<User> userComboBox = new ComboBox<>();
    private ComboBox<Book> bookComboBox = new ComboBox<>();

    private Button returnLoanButton = new Button("Return Selected Loan");

    private IUIController uiController;

    @Override
    public boolean init() {
        this.uiController = ICore.getInstance().getUIController();
        MenuItem menuItem = uiController.createMenuItem("Menu", "Loans");
        menuItem.setOnAction(e -> showLoanManagementTab());

        Button loanButton = uiController.addQuickAccessButton("", () -> showLoanManagementTab());
        Image loanIconImage = new Image(
                getClass().getResourceAsStream("/br/edu/ifba/inf008/plugins/images/loanIcon.png"));
        ImageView loanIconView = new ImageView(loanIconImage);
        loanIconView.setFitWidth(68);
        loanIconView.setFitHeight(68);
        loanButton.setGraphic(loanIconView);

        return true;
    }

    private void showLoanManagementTab() {
        VBox loanPane = createManagementPane();
        loanPane.getStylesheets()
                .add(getClass().getResource("/br/edu/ifba/inf008/plugins/css/loan-styles.css").toExternalForm());
        loanPane.getStyleClass().add("main-pane");
        loadData();
        uiController.createTab("Loan Management", loanPane);
    }

    private VBox createManagementPane() {
        TextField searchField = new TextField();
        searchField.setPromptText("Search by user or book...");
        searchField.getStyleClass().add("search-field");
        CheckBox activeOnlyCheckBox = new CheckBox("Show only active loans");
        HBox topBar = new HBox(10, new Label("Search:"), searchField, activeOnlyCheckBox);
        topBar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        setupTableAndFilters(searchField, activeOnlyCheckBox);

        GridPane formPane = createFormPane();
        formPane.getStyleClass().add("form-pane");

        VBox mainPane = new VBox(20, topBar, loanTable, formPane);
        mainPane.setPadding(new Insets(20));
        VBox.setVgrow(loanTable, Priority.ALWAYS);

        return mainPane;
    }

    private void setupTableAndFilters(TextField searchField, CheckBox activeOnlyCheckBox) {
        loanTable.getColumns().clear();

        TableColumn<Loan, User> userCol = new TableColumn<>("User");
        userCol.setCellValueFactory(new PropertyValueFactory<>("user"));
        userCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? null : user.getName());
            }
        });

        TableColumn<Loan, Book> bookCol = new TableColumn<>("Book Title");
        bookCol.setCellValueFactory(new PropertyValueFactory<>("book"));
        bookCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Book book, boolean empty) {
                super.updateItem(book, empty);
                setText(empty || book == null ? null : book.getTitle());
            }
        });

        TableColumn<Loan, LocalDate> loanDateCol = new TableColumn<>("Loan Date");
        loanDateCol.setCellValueFactory(new PropertyValueFactory<>("loanDate"));
        TableColumn<Loan, LocalDate> returnDateCol = new TableColumn<>("Return Date");
        returnDateCol.setCellValueFactory(new PropertyValueFactory<>("returnDate"));
        loanTable.getColumns().addAll(bookCol, userCol, loanDateCol, returnDateCol);
        loanTable.getStyleClass().add("table-view");

        FilteredList<Loan> filteredData = new FilteredList<>(masterData, p -> true);
        loanTable.setItems(filteredData);
        loanTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Runnable filterUpdater = () -> {
            String filterText = searchField.getText();
            boolean activeOnly = activeOnlyCheckBox.isSelected();
            filteredData.setPredicate(loan -> {
                boolean activeFilter = !activeOnly || loan.getReturnDate() == null;
                if (!activeFilter)
                    return false;

                if (filterText == null || filterText.isEmpty())
                    return true;

                String lowerCaseFilter = filterText.toLowerCase();
                return loan.getUser().getName().toLowerCase().contains(lowerCaseFilter) ||
                        loan.getBook().getTitle().toLowerCase().contains(lowerCaseFilter);
            });
        };

        searchField.textProperty().addListener((obs, oldV, newV) -> filterUpdater.run());
        activeOnlyCheckBox.selectedProperty().addListener((obs, oldV, newV) -> filterUpdater.run());

        loanTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean isReturnable = (newSelection != null && newSelection.getReturnDate() == null);
            returnLoanButton.setDisable(!isReturnable);
        });
    }

    private GridPane createFormPane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);

        setupComboBoxFiltering();

        Button registerButton = new Button("Register New Loan");
        registerButton.setOnAction(e -> handleRegisterLoan());

        returnLoanButton.setOnAction(e -> handleReturnLoan());
        returnLoanButton.setDisable(true);

        userComboBox.setMaxWidth(Double.MAX_VALUE);
        bookComboBox.setMaxWidth(Double.MAX_VALUE);

        grid.add(new Label("Search User:"), 0, 0);
        grid.add(userComboBox, 1, 0);
        grid.add(new Label("Search Available Book:"), 0, 1);
        grid.add(bookComboBox, 1, 1);

        HBox buttonBox = new HBox(10, registerButton, returnLoanButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        grid.add(buttonBox, 1, 2);

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        return grid;
    }

    private void setupComboBoxFiltering() {
        userComboBox.setEditable(true);
        bookComboBox.setEditable(true);

        FilteredList<User> filteredUsers = new FilteredList<>(allUsersMasterData, p -> true);
        userComboBox.setItems(filteredUsers);
        userComboBox.getEditor().textProperty().addListener((obs, oldVal, newVal) -> Platform.runLater(() -> {
            if (userComboBox.getSelectionModel().getSelectedItem() == null ||
                    !userComboBox.getSelectionModel().getSelectedItem().getName().equals(newVal)) {
                filteredUsers.setPredicate(user -> user.getName().toLowerCase().contains(newVal.toLowerCase().trim()));
            }
        }));
        
        userComboBox.getEditor().setOnMouseClicked(e -> {
            if (!userComboBox.isShowing()) {
                userComboBox.show();
            }
        });

        FilteredList<Book> filteredBooks = new FilteredList<>(allBooksMasterData, p -> true);
        bookComboBox.setItems(filteredBooks);
        bookComboBox.getEditor().textProperty().addListener((obs, oldVal, newVal) -> Platform.runLater(() -> {
            if (bookComboBox.getSelectionModel().getSelectedItem() == null ||
                    !bookComboBox.getSelectionModel().getSelectedItem().getTitle().equals(newVal)) {
                filteredBooks.setPredicate(book -> book.getTitle().toLowerCase().contains(newVal.toLowerCase().trim()));
            }
        }));

        bookComboBox.getEditor().setOnMouseClicked(e -> {
            if (!bookComboBox.isShowing()) {
                bookComboBox.show();
            }
        });

        userComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(User user) {
                return user == null ? null : user.getName();
            }

            @Override
            public User fromString(String string) {
                return allUsersMasterData.stream().filter(u -> u.getName().equals(string)).findFirst().orElse(null);
            }
        });
        bookComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Book book) {
                return book == null ? null : book.getTitle();
            }

            @Override
            public Book fromString(String string) {
                return allBooksMasterData.stream().filter(b -> b.getTitle().equals(string)).findFirst().orElse(null);
            }
        });
    }

    private void loadData() {
        try {
            masterData.setAll(loanDAO.getAllLoans());
            allUsersMasterData.setAll(loanDAO.getAllUsers());
            allBooksMasterData.setAll(loanDAO.getAvailableBooks());
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load initial data: " + ex.getMessage());
        }
    }

    private void handleRegisterLoan() {
        User selectedUser = userComboBox.getValue();
        Book selectedBook = bookComboBox.getValue();

        if (selectedUser == null || selectedBook == null) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "You must select a user and a book.");
            return;
        }

        Loan newLoan = new Loan();
        newLoan.setUser(selectedUser);
        newLoan.setBook(selectedBook);
        newLoan.setLoanDate(LocalDate.now());

        try {
            loanDAO.addLoan(newLoan);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Loan registered successfully.");
            loadData();
            userComboBox.getSelectionModel().clearSelection();
            userComboBox.getEditor().clear();
            bookComboBox.getSelectionModel().clearSelection();
            bookComboBox.getEditor().clear();
        } catch (IllegalStateException | SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Operation Failed", ex.getMessage());
        }
    }

    private void handleReturnLoan() {
        Loan selectedLoan = loanTable.getSelectionModel().getSelectedItem();
        if (selectedLoan == null)
            return;

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Return the book '" + selectedLoan.getBook().getTitle() + "'?", ButtonType.YES, ButtonType.NO);
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    loanDAO.returnLoan(selectedLoan.getLoanId());
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Book returned successfully.");
                    loadData();
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
        alert.getDialogPane().getStylesheets()
                .add(getClass().getResource("/br/edu/ifba/inf008/plugins/css/loan-styles.css").toExternalForm());
        alert.showAndWait();
    }
}