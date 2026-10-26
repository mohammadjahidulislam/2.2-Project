package com.attendance.controller;

import com.attendance.dao.UserDAO;
import com.attendance.model.User;
import com.attendance.util.SceneManager;
import com.attendance.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private final UserDAO userDAO = new UserDAO();

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both email and password.");
            return;
        }

        User user = userDAO.login(email, password);
        if (user == null) {
            messageLabel.setText("Invalid email or password.");
            return;
        }

        SessionManager.setCurrentUser(user);
        if (user.isTeacher()) {
            SceneManager.switchTo("teacher_dashboard.fxml", "Teacher Dashboard");
        } else {
            SceneManager.switchTo("student_dashboard.fxml", "Student Dashboard");
        }
    }

    @FXML
    private void goToRegister() {
        SceneManager.switchTo("register.fxml", "Register");
    }
}
