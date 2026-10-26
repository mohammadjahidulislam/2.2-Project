package com.attendance.controller;

import com.attendance.dao.CourseDAO;
import com.attendance.dao.SessionDAO;
import com.attendance.model.ClassSession;
import com.attendance.model.Course;
import com.attendance.model.User;
import com.attendance.util.SceneManager;
import com.attendance.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.ResourceBundle;

public class TeacherDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private Label messageLabel;
    @FXML private TextField topicField;
    @FXML private TableView<Course> courseTable;
    @FXML private TableColumn<Course, String> colCode;
    @FXML private TableColumn<Course, String> colName;

    private final CourseDAO courseDAO = new CourseDAO();
    private final SessionDAO sessionDAO = new SessionDAO();
    public static final int QR_VALID_SECONDS = 25;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User user = SessionManager.getCurrentUser();
        welcomeLabel.setText("Welcome, " + user.getName());

        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));

        loadCourses();
    }

    private void loadCourses() {
        List<Course> courses = courseDAO.findByTeacher(SessionManager.getCurrentUser().getId());
        courseTable.setItems(FXCollections.observableArrayList(courses));
    }

    @FXML
    private void goToNewCourse() {
        SceneManager.switchTo("new_course.fxml", "New Course");
    }

    @FXML
    private void startSession() {
        Course selected = courseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Please select a course from the table first.");
            return;
        }
        String token = generateToken();
        String topic = topicField.getText() == null ? "" : topicField.getText().trim();
        ClassSession session = sessionDAO.createSession(selected.getId(), topic, token, QR_VALID_SECONDS);
        if (session == null) {
            messageLabel.setText("Could not start session. Check database connection.");
            return;
        }
        QRDisplayController.setActiveCourse(selected, session);
        SceneManager.switchTo("qr_display.fxml", "Class QR Code");
    }

    @FXML
    private void viewAttendanceSheet() {
        Course selected = courseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Please select a course from the table first.");
            return;
        }
        AttendanceSheetController.setCourse(selected);
        SceneManager.switchTo("attendance_sheet.fxml", "Attendance Sheet - " + selected.getCode());
    }

    @FXML
    private void exportCSV() {
        Course selected = courseTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Please select a course from the table first.");
            return;
        }
        AttendanceSheetController.setCourse(selected);
        SceneManager.switchTo("attendance_sheet.fxml", "Attendance Sheet - " + selected.getCode());
        messageLabel.setText("Use the 'Export CSV' button on the attendance sheet screen.");
    }

    @FXML
    private void goToSettings() {
        SceneManager.switchTo("settings.fxml", "Settings");
    }

    @FXML
    private void handleLogout() {
        SessionManager.logout();
        SceneManager.switchTo("login.fxml", "QR Attendance System - Login");
    }

    private String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
