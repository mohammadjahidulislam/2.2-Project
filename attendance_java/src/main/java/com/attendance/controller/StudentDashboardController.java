package com.attendance.controller;

import com.attendance.dao.AttendanceDAO;
import com.attendance.dao.CourseDAO;
import com.attendance.dao.SessionDAO;
import com.attendance.model.Course;
import com.attendance.model.User;
import com.attendance.util.SceneManager;
import com.attendance.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class StudentDashboardController implements Initializable {

    @FXML private Label welcomeLabel;
    @FXML private TableView<CourseRow> courseTable;
    @FXML private TableColumn<CourseRow, String> colCode;
    @FXML private TableColumn<CourseRow, String> colName;
    @FXML private TableColumn<CourseRow, Integer> colTotal;
    @FXML private TableColumn<CourseRow, Integer> colAttended;
    @FXML private TableColumn<CourseRow, String> colPercent;

    private final CourseDAO courseDAO = new CourseDAO();
    private final SessionDAO sessionDAO = new SessionDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User user = SessionManager.getCurrentUser();
        welcomeLabel.setText("Welcome, " + user.getName());

        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colAttended.setCellValueFactory(new PropertyValueFactory<>("attended"));
        colPercent.setCellValueFactory(new PropertyValueFactory<>("percent"));

        loadCourses();
    }

    private void loadCourses() {
        User user = SessionManager.getCurrentUser();
        List<Course> courses = courseDAO.findEnrolledCourses(user.getId());
        ObservableList<CourseRow> rows = FXCollections.observableArrayList();
        for (Course c : courses) {
            int total = sessionDAO.findByCourse(c.getId()).size();
            int attended = attendanceDAO.countAttendedSessions(user.getId(), c.getId());
            double pct = total == 0 ? 0 : (attended * 100.0 / total);
            rows.add(new CourseRow(c.getCode(), c.getName(), total, attended, String.format("%.1f%%", pct)));
        }
        courseTable.setItems(rows);
    }

    @FXML
    private void goToScan() {
        SceneManager.switchTo("qr_scan.fxml", "Scan QR - Attendance");
    }

    @FXML
    private void goToBrowseCourses() {
        SceneManager.switchTo("browse_courses.fxml", "Browse Courses");
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

    /** Simple row object for the TableView (needs public getters for PropertyValueFactory). */
    public static class CourseRow {
        private final String code;
        private final String name;
        private final int total;
        private final int attended;
        private final String percent;

        public CourseRow(String code, String name, int total, int attended, String percent) {
            this.code = code;
            this.name = name;
            this.total = total;
            this.attended = attended;
            this.percent = percent;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public int getTotal() { return total; }
        public int getAttended() { return attended; }
        public String getPercent() { return percent; }
    }
}
