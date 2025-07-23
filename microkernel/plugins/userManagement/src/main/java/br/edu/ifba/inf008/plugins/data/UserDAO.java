package br.edu.ifba.inf008.plugins.data;

import br.edu.ifba.inf008.shell.model.User;
import java.sql.SQLException;
import java.util.List;

public interface UserDAO {
    void addUser(User user) throws SQLException;
    // void updateUser(User user) throws SQLException;
    // void deleteUser(int userId) throws SQLException;
    List<User> getAllUsers() throws SQLException;
    // User getUserById(int userId) throws SQLException;
}