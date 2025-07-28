package br.edu.ifba.inf008.plugins.data;

import br.edu.ifba.inf008.shell.data.DatabaseConnection;
import br.edu.ifba.inf008.plugins.data.UserDAO;
import br.edu.ifba.inf008.shell.model.User;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.ArrayList;

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

    @Override
    public void updateUser(User user) throws SQLException {
        String sql = "UPDATE users SET name = ?, email = ? WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getEmail());
            pstmt.setInt(3, user.getUserId());
            pstmt.executeUpdate();
        }
    }

    @Override
    public void deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.executeUpdate();
        }
    }

    @Override
    public List<User> getAllUsers() throws SQLException {
        String sql = "SELECT user_id, name, email, registered_at FROM users";
        List<User> users = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                Timestamp registeredAt = rs.getTimestamp("registered_at");
                if (registeredAt != null) {
                    user.setRegisteredAt(registeredAt.toLocalDateTime());
                } else {
                    user.setRegisteredAt(null);
                }
                users.add(user);
            }
        }
        return users;
    }

    @Override
    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT user_id, name, email, registered_at FROM users WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = new User();
                    user.setUserId(rs.getInt("user_id"));
                    user.setName(rs.getString("name"));
                    user.setEmail(rs.getString("email"));
                    Timestamp registeredAt = rs.getTimestamp("registered_at");
                    if (registeredAt != null) {
                        user.setRegisteredAt(registeredAt.toLocalDateTime());
                    } else {
                        user.setRegisteredAt(null);
                    }
                    return user;
                }
            }
        }
        return null;
    }
}
