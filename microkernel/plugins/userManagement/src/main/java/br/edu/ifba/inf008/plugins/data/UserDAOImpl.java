package br.edu.ifba.inf008.plugins.data;

import br.edu.ifba.inf008.domain.data.DatabaseConnection;
import br.edu.ifba.inf008.plugins.data.UserDAO;
import br.edu.ifba.inf008.domain.model.User;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class UserDAOImpl implements UserDAO {
    @Override
    public void addUser(User user) throws SQLException {
        String sql = "INSERT INTO users (name, email) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.executeUpdate();
        }
    }
}
