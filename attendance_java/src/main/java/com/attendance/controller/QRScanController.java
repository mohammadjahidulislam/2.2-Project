package com.attendance.controller;

import com.attendance.dao.AttendanceDAO;
import com.attendance.dao.CourseDAO;
import com.attendance.dao.SessionDAO;
import com.attendance.model.ClassSession;
import com.attendance.model.Course;
import com.attendance.model.User;
import com.attendance.util.QRUtil;
import com.attendance.util.SceneManager;
import com.attendance.util.SessionManager;
import com.github.sarxos.webcam.Webcam;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.sql.Timestamp;
import java.util.List;
import java.util.ResourceBundle;

public class QRScanController implements Initializable {

    @FXML private ComboBox<Course> courseBox;
    @FXML private ImageView cameraView;
    @FXML private Label statusLabel;

    private final CourseDAO courseDAO = new CourseDAO();
    private final SessionDAO sessionDAO = new SessionDAO();
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    private Webcam webcam;
    private Timeline captureTimeline;
    private boolean attendanceMarkedThisSession = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User user = SessionManager.getCurrentUser();
        List<Course> courses = courseDAO.findEnrolledCourses(user.getId());
        courseBox.getItems().addAll(courses);
        if (!courses.isEmpty()) courseBox.getSelectionModel().selectFirst();

        statusLabel.setText("Select your course, then click 'Start Camera' and point it at the projector QR code.");
    }

    @FXML
    private void startCamera() {
        if (courseBox.getValue() == null) {
            statusLabel.setText("Please select a course first.");
            return;
        }

        try {
            webcam = Webcam.getDefault();
            if (webcam == null) {
                statusLabel.setText("No webcam found on this computer.");
                return;
            }
            if (!webcam.isOpen()) {
                webcam.open();
            }
        } catch (Exception e) {
            statusLabel.setText("Could not access webcam: " + e.getMessage());
            return;
        }

        attendanceMarkedThisSession = false;
        statusLabel.setText("Camera started. Point it at the QR code on the projector screen...");

        captureTimeline = new Timeline(new KeyFrame(Duration.millis(400), event -> captureAndDecode()));
        captureTimeline.setCycleCount(Timeline.INDEFINITE);
        captureTimeline.play();
    }

    private void captureAndDecode() {
        if (webcam == null || !webcam.isOpen() || attendanceMarkedThisSession) return;

        BufferedImage image = webcam.getImage();
        if (image == null) return;

        cameraView.setImage(SwingFXUtils.toFXImage(image, null));

        String decodedText = QRUtil.decodeQRFromImage(image);
        if (decodedText != null) {
            handleDecodedText(decodedText);
        }
    }

    private void handleDecodedText(String text) {
        try {
            String[] parts = text.split(":", 2);
            int sessionId = Integer.parseInt(parts[0]);
            String token = parts[1];

            ClassSession session = sessionDAO.findById(sessionId);
            if (session == null || !session.isActive()) {
                statusLabel.setText("This class session is not active anymore.");
                return;
            }
            if (!session.getQrToken().equals(token)) {
                statusLabel.setText("QR code expired. Please scan the current QR code again.");
                return;
            }
            if (session.getQrExpiresAt().before(new Timestamp(System.currentTimeMillis()))) {
                statusLabel.setText("QR code expired. Please scan the current QR code again.");
                return;
            }

            Course selectedCourse = courseBox.getValue();
            if (session.getCourseId() != selectedCourse.getId()) {
                statusLabel.setText("This QR code belongs to a different course than the one you selected.");
                return;
            }

            User user = SessionManager.getCurrentUser();
            if (!courseDAO.isEnrolled(user.getId(), session.getCourseId())) {
                statusLabel.setText("You are not enrolled in this course.");
                return;
            }

            AttendanceDAO.MarkResult result = attendanceDAO.markAttendance(user.getId(), session.getCourseId(), session.getId());
            attendanceMarkedThisSession = true;
            stopCamera();

            switch (result) {
                case SUCCESS -> statusLabel.setText("Attendance marked successfully!");
                case ALREADY_MARKED -> statusLabel.setText("Your attendance was already marked for this class.");
                case ERROR -> statusLabel.setText("Something went wrong. Please try again.");
            }
        } catch (Exception e) {
            statusLabel.setText("Invalid QR code scanned.");
        }
    }

    private void stopCamera() {
        if (captureTimeline != null) captureTimeline.stop();
        if (webcam != null && webcam.isOpen()) webcam.close();
    }

    @FXML
    private void goBack() {
        stopCamera();
        SceneManager.switchTo("student_dashboard.fxml", "Student Dashboard");
    }
}
