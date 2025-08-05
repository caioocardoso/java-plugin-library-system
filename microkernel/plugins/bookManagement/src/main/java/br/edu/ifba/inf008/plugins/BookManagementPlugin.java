package br.edu.ifba.inf008.plugins;

import br.edu.ifba.inf008.shell.model.Book;
import br.edu.ifba.inf008.interfaces.ICore;
import br.edu.ifba.inf008.interfaces.IPlugin;
import br.edu.ifba.inf008.interfaces.IUIController;
import br.edu.ifba.inf008.plugins.data.BookDAO;
import br.edu.ifba.inf008.plugins.data.BookDAOImpl;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Pos;
import javafx.scene.layout.ColumnConstraints;

public class BookManagementPlugin implements IPlugin {

    private final BookDAO bookDAO = new BookDAOImpl();
    private TableView<Book> bookTable = new TableView<>();
    private ObservableList<Book> masterData = FXCollections.observableArrayList();
    private TextField titleField = new TextField();
    private TextField authorField = new TextField();
    private TextField isbnField = new TextField();
    private TextField yearField = new TextField();
    private TextField copiesField = new TextField();
    private Button saveButton = new Button("Add Book");
    private Book selectedBook = null;

    private IUIController uiController;

    @Override
    public boolean init() {
        this.uiController = ICore.getInstance().getUIController();

        Button booksButton = uiController.addQuickAccessButton("", () -> {
            uiController.showTab("Book Management", () -> {
                VBox bookPane = createManagementPane();

                bookPane.getStylesheets().add(
                        getClass().getResource("/br/edu/ifba/inf008/plugins/css/book-styles.css").toExternalForm());
                bookPane.getStyleClass().add("main-pane");

                loadBookData();

                return bookPane;
            });
        });

        Image bookIconImage = new Image(
                getClass().getResourceAsStream("/br/edu/ifba/inf008/plugins/images/bookIcon.png"));
        ImageView bookIconView = new ImageView(bookIconImage);
        bookIconView.setFitWidth(48);
        bookIconView.setFitHeight(48);
        booksButton.setGraphic(bookIconView);

        return true;
    }

    private VBox createManagementPane() {
        TextField searchField = new TextField();
        searchField.setPromptText("Search by title or author...");
        searchField.getStyleClass().add("search-field");

        FilteredList<Book> filteredData = new FilteredList<>(masterData, p -> true);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredData.setPredicate(book -> {
                if (newVal == null || newVal.isEmpty()) {
                    return true;
                }
                String filter = newVal.toLowerCase();
                if (book.getTitle().toLowerCase().contains(filter)) {
                    return true;
                }
                return book.getAuthor().toLowerCase().contains(filter);
            });
        });
        bookTable.setItems(filteredData);
        bookTable.getStyleClass().add("table-view");

        setupTableColumns();
        bookTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            selectedBook = newSelection;
            if (selectedBook != null) {
                titleField.setText(selectedBook.getTitle());
                authorField.setText(selectedBook.getAuthor());
                isbnField.setText(selectedBook.getIsbn());
                yearField.setText(String.valueOf(selectedBook.getYear()));
                copiesField.setText(String.valueOf(selectedBook.getCopies()));
                saveButton.setText("Update Book");
            } else {
                clearForm();
            }
        });

        GridPane formPane = createFormPane();
        formPane.getStyleClass().add("form-pane");

        HBox topBar = new HBox(10, new Label("Search:"), searchField);
        topBar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        VBox mainPane = new VBox(15, topBar, bookTable, formPane);
        mainPane.setPadding(new Insets(20));
        VBox.setVgrow(bookTable, Priority.ALWAYS);

        return mainPane;
    }

    private void setupTableColumns() {
        bookTable.getColumns().clear();
        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        TableColumn<Book, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
        isbnCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));
        TableColumn<Book, Integer> yearCol = new TableColumn<>("Year");
        yearCol.setCellValueFactory(new PropertyValueFactory<>("year"));
        TableColumn<Book, Integer> copiesCol = new TableColumn<>("Copies");
        copiesCol.setCellValueFactory(new PropertyValueFactory<>("copies"));
        bookTable.getColumns().addAll(titleCol, authorCol, isbnCol, yearCol, copiesCol);
        bookTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private GridPane createFormPane() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);

        titleField.getStyleClass().add("text-field");
        authorField.getStyleClass().add("text-field");
        isbnField.getStyleClass().add("text-field");
        yearField.getStyleClass().add("text-field");
        copiesField.getStyleClass().add("text-field");

        Button clearButton = new Button("Clear");
        clearButton.getStyleClass().add("button");
        clearButton.setOnAction(e -> clearForm());

        saveButton.getStyleClass().add("button");
        saveButton.setOnAction(e -> handleSave());

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("delete-button");
        deleteButton.setOnAction(e -> handleDelete());
        deleteButton.setDisable(true);
        bookTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> deleteButton.setDisable(newVal == null));

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Author:"), 0, 1);
        grid.add(authorField, 1, 1);
        grid.add(new Label("ISBN:"), 0, 2);
        grid.add(isbnField, 1, 2);
        grid.add(new Label("Year:"), 0, 3);
        grid.add(yearField, 1, 3);
        grid.add(new Label("Copies:"), 0, 4);
        grid.add(copiesField, 1, 4);

        HBox buttonBox = new HBox(10, saveButton, clearButton, deleteButton);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        grid.add(buttonBox, 1, 5);

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS);
        grid.getColumnConstraints().addAll(col1, col2);

        return grid;
    }

    private void loadBookData() {
        try {
            masterData.setAll(bookDAO.getAllBooks());
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load books: " + ex.getMessage());
        }
    }

    private void handleSave() {
        try {
            String title = titleField.getText();
            String author = authorField.getText();
            String isbn = isbnField.getText();
            int year = Integer.parseInt(yearField.getText());
            int copies = Integer.parseInt(copiesField.getText());

            if (title.isEmpty() || author.isEmpty() || isbn.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", "Title, Author, and ISBN cannot be empty.");
                return;
            }

            if (selectedBook == null) {
                Book newBook = new Book();
                newBook.setTitle(title);
                newBook.setAuthor(author);
                newBook.setIsbn(isbn);
                newBook.setYear(year);
                newBook.setCopies(copies);
                bookDAO.addBook(newBook);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Book added successfully.");
            } else {
                selectedBook.setTitle(title);
                selectedBook.setAuthor(author);
                selectedBook.setIsbn(isbn);
                selectedBook.setYear(year);
                selectedBook.setCopies(copies);
                bookDAO.updateBook(selectedBook);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Book updated successfully.");
            }
            loadBookData();
            clearForm();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Validation Error", "Year and Copies must be valid numbers.");
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Operation failed: " + ex.getMessage());
        }
    }

    private void handleDelete() {
        if (selectedBook == null) {
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete '" + selectedBook.getTitle() + "'?", ButtonType.YES, ButtonType.NO);
        confirmation.getDialogPane().getStylesheets()
                .add(getClass().getResource("/br/edu/ifba/inf008/plugins/css/loan-styles.css").toExternalForm());

        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    bookDAO.deleteBook(selectedBook.getBookId());
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Book deleted successfully.");
                    loadBookData();
                } catch (SQLException ex) {
                    showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete book: " + ex.getMessage());
                }
            }
        });
    }

    private void clearForm() {
        selectedBook = null;
        bookTable.getSelectionModel().clearSelection();
        titleField.clear();
        authorField.clear();
        isbnField.clear();
        yearField.clear();
        copiesField.clear();
        saveButton.setText("Add Book");
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
