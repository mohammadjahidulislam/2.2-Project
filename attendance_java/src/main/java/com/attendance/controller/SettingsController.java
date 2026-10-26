package com.attendance.controller;

import com.attendance.dao.UserDAO;
import com.attendance.model.User;
import com.attendance.util.SceneManager;
import com.attendance.util.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {

    @FXML private TextField nameField;
    @FXML private TextField departmentField;
    @FXML private TextField emailField;
    @FXML private Label profileMessage;

    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private Label passwordMessage;

    private final UserDAO userDAO = new UserDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User user = SessionManager.getCurrentUser();
        nameField.setText(user.getName());
        departmentField.setText(user.getDepartment());
        emailField.setText(user.getEmail());
    }

    @FXML
    private void saveProfile() {
        User user = SessionManager.getCurrentUser();
        String name = nameField.getText().trim();
        String department = departmentField.getText().trim();

        if (name.isEmpty()) {
            profileMessage.setText("Name cannot be empty.");
            return;
        }

        userDAO.updateProfile(user.getId(), name, department);
        user.setName(name);
        user.setDepartment(department);
        profileMessage.setText("Profile updated successfully.");
    }

    @FXML
    private void changePassword() {
        User user = SessionManager.getCurrentUser();
        String current = currentPasswordField.getText();
        String newPass = newPasswordField.getText();

        if (current.isEmpty() || newPass.isEmpty()) {
            passwordMessage.setText("Please fill in both password fields.");
            return;
        }
        if (newPass.length() < 6) {
            passwordMessage.setText("New password must be at least 6 characters.");
            return;
        }

        boolean success = userDAO.changePassword(user.getId(), current, newPass);
        if (success) {
            passwordMessage.setText("Password changed successfully.");
            currentPasswordField.clear();
            newPasswordField.clear();
        } else {
            passwordMessage.setText("Current password is incorrect.");
        }
    }

    @FXML
    private void goBack() {
        User user = SessionManager.getCurrentUser();
        if (user.isTeacher()) {
            SceneManager.switchTo("teacher_dashboard.fxml", "Teacher Dashboard");
        } else {
            SceneManager.switchTo("student_dashboard.fxml", "Student Dashboard");
        }
    }
}
