package com.attendance.controller;

import com.attendance.dao.SessionDAO;
import com.attendance.model.ClassSession;
import com.attendance.model.Course;
import com.attendance.util.QRUtil;
import com.attendance.util.SceneManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.net.URL;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.ResourceBundle;

public class QRDisplayController implements Initializable {

    @FXML private Label courseLabel;
    @FXML private Label topicLabel;
    @FXML private ImageView qrImageView;
    @FXML private ProgressBar progressBar;

    private final SessionDAO sessionDAO = new SessionDAO();
    private static final int VALID_SECONDS = TeacherDashboardController.QR_VALID_SECONDS;

    // Simple static hand-off from TeacherDashboardController -> this screen
    private static Course activeCourse;
    private static ClassSession activeSession;

    public static void setActiveCourse(Course course, ClassSession session) {
        activeCourse = course;
        activeSession = session;
    }

    private Timeline refreshTimeline;
    private int secondsElapsed = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (activeCourse == null || activeSession == null) {
            SceneManager.showError("Error", "No active session found.");
            SceneManager.switchTo("teacher_dashboard.fxml", "Teacher Dashboard");
            return;
        }
        courseLabel.setText(activeCourse.getCode() + " - " + activeCourse.getName());
        topicLabel.setText(activeSession.getTopic() == null || activeSession.getTopic().isEmpty()
                ? "Class Session" : activeSession.getTopic());

        renderQR();
        startAutoRefresh();
    }

    private void renderQR() {
        try {
            String payload = activeSession.getId() + ":" + activeSession.getQrToken();
            qrImageView.setImage(QRUtil.generateQRImage(payload, 300));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startAutoRefresh() {
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            secondsElapsed++;
            double remainingFraction = Math.max(0, 1.0 - (secondsElapsed / (double) VALID_SECONDS));
            progressBar.setProgress(remainingFraction);

            if (secondsElapsed >= VALID_SECONDS) {
                String newToken = generateToken();
                sessionDAO.refreshToken(activeSession.getId(), newToken, VALID_SECONDS);
                activeSession.setQrToken(newToken);
                renderQR();
                secondsElapsed = 0;
            }
        }));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    private String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @FXML
    private void handleCloseSession() {
        if (refreshTimeline != null) refreshTimeline.stop();
        sessionDAO.closeSession(activeSession.getId());
        SceneManager.showInfo("Session Closed", "The class session has been closed.");
        SceneManager.switchTo("teacher_dashboard.fxml", "Teacher Dashboard");
    }

    @FXML
    private void goBack() {
        if (refreshTimeline != null) refreshTimeline.stop();
        SceneManager.switchTo("teacher_dashboard.fxml", "Teacher Dashboard");
    }
}
