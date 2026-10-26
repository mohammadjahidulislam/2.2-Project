package com.attendance.controller;

import com.attendance.dao.UserDAO;
import com.attendance.util.SceneManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {

    @FXML private ComboBox<String> roleBox;
    @FXML private TextField nameField;
    @FXML private TextField studentIdField;
    @FXML private TextField departmentField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private final UserDAO userDAO = new UserDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        roleBox.getItems().addAll("STUDENT", "TEACHER");
        roleBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleRegister() {
        String role = roleBox.getValue();
        String name = nameField.getText().trim();
        String studentId = studentIdField.getText().trim();
        String department = departmentField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Name, email and password are required.");
            return;
        }
        if (password.length() < 6) {
            messageLabel.setText("Password must be at least 6 characters.");
            return;
        }
        if ("STUDENT".equals(role) && studentId.isEmpty()) {
            messageLabel.setText("Student ID is required for students.");
            return;
        }

        boolean success = userDAO.register(name, email, password, role, studentId, department);
        if (!success) {
            messageLabel.setText("An account with this email already exists.");
            return;
        }

        SceneManager.showInfo("Registration Successful", "Your account has been created. Please login.");
        SceneManager.switchTo("login.fxml", "QR Attendance System - Login");
    }

    @FXML
    private void goToLogin() {
        SceneManager.switchTo("login.fxml", "QR Attendance System - Login");
    }
}
