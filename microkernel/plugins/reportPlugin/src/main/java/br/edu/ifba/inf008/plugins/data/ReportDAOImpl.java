package br.edu.ifba.inf008.plugins.data;

import br.edu.ifba.inf008.shell.data.DatabaseConnection;
import br.edu.ifba.inf008.shell.model.Book;
import br.edu.ifba.inf008.shell.model.Loan;
import br.edu.ifba.inf008.shell.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportDAOImpl implements ReportDAO {
    @Override
    public List<Loan> getActiveLoans() throws SQLException {
        List<Loan> activeLoans = new ArrayList<>();
        String sql = "SELECT l.loan_id, l.loan_date, " +
                     "u.user_id, u.name, " +
                     "b.book_id, b.title, b.author " +
                     "FROM loans l " +
                     "JOIN users u ON l.user_id = u.user_id " +
                     "JOIN books b ON l.book_id = b.book_id " +
                     "WHERE l.return_date IS NULL " +
                     "ORDER BY l.loan_date ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setName(rs.getString("name"));

                Book book = new Book();
                book.setBookId(rs.getInt("book_id"));
                book.setTitle(rs.getString("title"));
                book.setAuthor(rs.getString("author"));

                Loan loan = new Loan();
                loan.setLoanId(rs.getInt("loan_id"));
                loan.setUser(user);
                loan.setBook(book);
                loan.setLoanDate(rs.getDate("loan_date").toLocalDate());
                
                activeLoans.add(loan);
            }
        }
        return activeLoans;
    }
}