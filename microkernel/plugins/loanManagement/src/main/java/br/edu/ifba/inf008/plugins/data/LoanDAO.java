package br.edu.ifba.inf008.plugins.data;

import br.edu.ifba.inf008.shell.model.Loan;
import br.edu.ifba.inf008.shell.model.User;
import br.edu.ifba.inf008.shell.model.Book;
import java.sql.SQLException;
import java.util.List;

public interface LoanDAO {

    List<Loan> getAllLoans() throws SQLException;

    void addLoan(Loan loan) throws SQLException, IllegalStateException;

    void returnLoan(int loanId) throws SQLException;
    List<User> getAllUsers() throws SQLException;
    List<Book> getAvailableBooks() throws SQLException;
}
