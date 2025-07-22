package br.edu.ifba.inf008.plugins.data;

import br.edu.ifba.inf008.domain.model.User;
import java.util.List;
import java.sql.SQLException;

public interface UserDAO {
    void addUser(User user) throws SQLException;
    // void updateUser(User user) throws SQLException;
    // void deleteUser(int userId) throws SQLException;
    // List<User> getAllUsers() throws SQLException;
    // User getUserById(int userId) throws SQLException;
}