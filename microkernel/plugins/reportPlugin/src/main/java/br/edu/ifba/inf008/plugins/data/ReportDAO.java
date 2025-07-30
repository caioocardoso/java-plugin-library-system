package br.edu.ifba.inf008.plugins.data;

import br.edu.ifba.inf008.shell.model.Loan;
import java.sql.SQLException;
import java.util.List;

public interface ReportDAO {
    List<Loan> getActiveLoans() throws SQLException;
} 