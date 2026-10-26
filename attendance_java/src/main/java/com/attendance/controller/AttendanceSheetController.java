package com.attendance.controller;

import com.attendance.dao.AttendanceDAO;
import com.attendance.dao.CourseDAO;
import com.attendance.dao.SessionDAO;
import com.attendance.model.ClassSession;
import com.attendance.model.Course;
import com.attendance.model.User;
import com.attendance.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AttendanceSheetController implements Initializable {

    @FXML private Label courseLabel;
    @FXML private TableView<Map<String, Object>> sheetTable;

    private final SessionDAO sessionDAO = new SessionDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    private static Course selectedCourse;
    private List<ClassSession> sessions;
    private List<User> enrolledStudents;

    public static void setCourse(Course course) {
        selectedCourse = course;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (selectedCourse == null) {
            SceneManager.showError("Error", "No course selected.");
            SceneManager.switchTo("teacher_dashboard.fxml", "Teacher Dashboard");
            return;
        }
        courseLabel.setText("Attendance Sheet: " + selectedCourse.getCode() + " - " + selectedCourse.getName());
        buildTable();
    }

    @SuppressWarnings("unchecked")
    private void buildTable() {
        sessions = sessionDAO.findByCourse(selectedCourse.getId());
        enrolledStudents = fetchEnrolledStudents(selectedCourse.getId());

        sheetTable.getColumns().clear();

        TableColumn<Map<String, Object>, String> idCol = new TableColumn<>("Student ID");
        idCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                (String) data.getValue().get("studentId")));
        idCol.setPrefWidth(110);

        TableColumn<Map<String, Object>, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                (String) data.getValue().get("name")));
        nameCol.setPrefWidth(160);

        sheetTable.getColumns().addAll(idCol, nameCol);

        SimpleDateFormat fmt = new SimpleDateFormat("dd-MMM");
        for (ClassSession s : sessions) {
            String header = fmt.format(s.getSessionDate());
            TableColumn<Map<String, Object>, String> col = new TableColumn<>(header);
            final int sessionId = s.getId();
            col.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                    (String) data.getValue().get("session_" + sessionId)));
            col.setPrefWidth(80);
            sheetTable.getColumns().add(col);
        }

        TableColumn<Map<String, Object>, String> pctCol = new TableColumn<>("Percentage");
        pctCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                (String) data.getValue().get("percentage")));
        pctCol.setPrefWidth(90);
        sheetTable.getColumns().add(pctCol);

        ObservableList<Map<String, Object>> rows = FXCollections.observableArrayList();
        for (User student : enrolledStudents) {
            Map<String, Object> row = new HashMap<>();
            row.put("studentId", student.getStudentId());
            row.put("name", student.getName());
            Set<Integer> attended = attendanceDAO.getAttendedSessionIds(student.getId(), selectedCourse.getId());
            int presentCount = 0;
            for (ClassSession s : sessions) {
                boolean present = attended.contains(s.getId());
                row.put("session_" + s.getId(), present ? "P" : "A");
                if (present) presentCount++;
            }
            double pct = sessions.isEmpty() ? 0 : (presentCount * 100.0 / sessions.size());
            row.put("percentage", String.format("%.1f%%", pct));
            rows.add(row);
        }
        sheetTable.setItems(rows);
    }

    private List<User> fetchEnrolledStudents(int courseId) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT u.* FROM users u JOIN enrollments e ON u.id = e.student_id WHERE e.course_id = ?";
        try (Connection conn = com.attendance.db.DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User u = new User();
                    u.setId(rs.getInt("id"));
                    u.setName(rs.getString("name"));
                    u.setStudentId(rs.getString("student_id"));
                    list.add(u);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @FXML
    private void exportCSV() {
        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName(selectedCourse.getCode() + "_attendance.csv");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        java.io.File file = chooser.showSaveDialog(sheetTable.getScene().getWindow());
        if (file == null) return;

        try (FileWriter writer = new FileWriter(file)) {
            // Header
            StringBuilder header = new StringBuilder("Student ID,Name");
            SimpleDateFormat fmt = new SimpleDateFormat("dd-MMM");
            for (ClassSession s : sessions) {
                header.append(",").append(fmt.format(s.getSessionDate()));
            }
            header.append(",Percentage\n");
            writer.write(header.toString());

            for (Map<String, Object> row : sheetTable.getItems()) {
                StringBuilder line = new StringBuilder();
                line.append(row.get("studentId")).append(",").append(row.get("name"));
                for (ClassSession s : sessions) {
                    line.append(",").append(row.get("session_" + s.getId()));
                }
                line.append(",").append(row.get("percentage")).append("\n");
                writer.write(line.toString());
            }

            SceneManager.showInfo("Export Successful", "Attendance sheet exported to:\n" + file.getAbsolutePath());
        } catch (IOException e) {
            SceneManager.showError("Export Failed", "Could not write CSV file: " + e.getMessage());
        }
    }

    @FXML
    private void goBack() {
        SceneManager.switchTo("teacher_dashboard.fxml", "Teacher Dashboard");
    }
}
