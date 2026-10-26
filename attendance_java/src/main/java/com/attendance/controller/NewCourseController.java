package com.attendance.controller;

import com.attendance.dao.CourseDAO;
import com.attendance.util.SceneManager;
import com.attendance.util.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class NewCourseController {

    @FXML private TextField codeField;
    @FXML private TextField nameField;
    @FXML private Label messageLabel;

    private final CourseDAO courseDAO = new CourseDAO();

    @FXML
    private void handleCreate() {
        String code = codeField.getText().trim().toUpperCase();
        String name = nameField.getText().trim();

        if (code.isEmpty() || name.isEmpty()) {
            messageLabel.setText("Please fill in both fields.");
            return;
        }

        boolean success = courseDAO.createCourse(code, name, SessionManager.getCurrentUser().getId());
        if (success) {
            SceneManager.showInfo("Success", "Course created successfully.");
            SceneManager.switchTo("teacher_dashboard.fxml", "Teacher Dashboard");
        } else {
            messageLabel.setText("Could not create course. The course code may already exist.");
        }
    }

    @FXML
    private void goBack() {
        SceneManager.switchTo("teacher_dashboard.fxml", "Teacher Dashboard");
    }
}
