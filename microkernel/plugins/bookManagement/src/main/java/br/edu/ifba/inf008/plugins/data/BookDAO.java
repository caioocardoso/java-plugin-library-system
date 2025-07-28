package br.edu.ifba.inf008.plugins.data;

import br.edu.ifba.inf008.shell.model.Book;
import java.sql.SQLException;
import java.util.List;

public interface BookDAO {
    void addBook(Book book) throws SQLException;
    void updateBook(Book book) throws SQLException;
    void deleteBook(int bookId) throws SQLException;
    List<Book> getAllBooks() throws SQLException;
}