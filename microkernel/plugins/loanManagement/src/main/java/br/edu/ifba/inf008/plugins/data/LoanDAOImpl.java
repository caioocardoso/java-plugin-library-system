package br.edu.ifba.inf008.plugins.data;

import br.edu.ifba.inf008.shell.data.DatabaseConnection;
import br.edu.ifba.inf008.shell.model.Book;
import br.edu.ifba.inf008.shell.model.Loan;
import br.edu.ifba.inf008.shell.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LoanDAOImpl implements LoanDAO {
    @Override
    public List<Loan> getAllLoans() throws SQLException {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT l.loan_id, l.loan_date, l.return_date, " +
                     "u.user_id, u.name, u.email, " +
                     "b.book_id, b.title, b.author " +
                     "FROM loans l " +
                     "JOIN users u ON l.user_id = u.user_id " +
                     "JOIN books b ON l.book_id = b.book_id " +
                     "ORDER BY l.loan_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));

                Book book = new Book();
                book.setBookId(rs.getInt("book_id"));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));

                Loan loan = new Loan();
                loan.setLoanId(rs.getInt("loan_id"));
                loan.setUser(user);
                loan.setBook(book);
                loan.setLoanDate(rs.getDate("loan_date").toLocalDate());
                if (rs.getDate("return_date") != null) {
                    loan.setReturnDate(rs.getDate("return_date").toLocalDate());
                }
                loans.add(loan);
            }
        }
        return loans;
    }

    @Override
    public void addLoan(Loan loan) throws SQLException, IllegalStateException {
        String checkSql = "SELECT copies_available FROM books WHERE book_id = ?";
        String insertLoanSql = "INSERT INTO loans (user_id, book_id, loan_date) VALUES (?, ?, ?)";
        String updateBookSql = "UPDATE books SET copies_available = copies_available - 1 WHERE book_id = ?";
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, loan.getBook().getBookId());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    if (rs.getInt("copies_available") <= 0) {
                        throw new IllegalStateException("No copies available for this book.");
                    }
                } else {
                    throw new SQLException("Book not found.");
                }
            }

            try (PreparedStatement insertStmt = conn.prepareStatement(insertLoanSql)) {
                insertStmt.setInt(1, loan.getUser().getUserId());
                insertStmt.setInt(2, loan.getBook().getBookId());
                insertStmt.setDate(3, Date.valueOf(loan.getLoanDate()));
                insertStmt.executeUpdate();
            }

            try (PreparedStatement updateStmt = conn.prepareStatement(updateBookSql)) {
                updateStmt.setInt(1, loan.getBook().getBookId());
                updateStmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException | IllegalStateException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    @Override
    public void returnLoan(int loanId) throws SQLException {
        String selectBookIdSql = "SELECT book_id FROM loans WHERE loan_id = ?";
        String updateLoanSql = "UPDATE loans SET return_date = ? WHERE loan_id = ?";
        String updateBookSql = "UPDATE books SET copies_available = copies_available + 1 WHERE book_id = ?";
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            int bookId = -1;
            try (PreparedStatement selectStmt = conn.prepareStatement(selectBookIdSql)) {
                selectStmt.setInt(1, loanId);
                ResultSet rs = selectStmt.executeQuery();
                if (rs.next()) {
                    bookId = rs.getInt("book_id");
                } else {
                    throw new SQLException("Loan not found.");
                }
            }

            try (PreparedStatement updateLoanStmt = conn.prepareStatement(updateLoanSql)) {
                updateLoanStmt.setDate(1, Date.valueOf(java.time.LocalDate.now()));
                updateLoanStmt.setInt(2, loanId);
                updateLoanStmt.executeUpdate();
            }

            try (PreparedStatement updateBookStmt = conn.prepareStatement(updateBookSql)) {
                updateBookStmt.setInt(1, bookId);
                updateBookStmt.executeUpdate();
            }
            
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) conn.rollback();
            throw e;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    @Override
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, name, email FROM users ORDER BY name";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                users.add(user);
            }
        }
        return users;
    }

    @Override
    public List<Book> getAvailableBooks() throws SQLException {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT book_id, title, author FROM books WHERE copies_available > 0 ORDER BY title";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Book book = new Book();
                book.setBookId(rs.getInt("book_id"));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));
                books.add(book);
            }
        }
        return books;
    }
}