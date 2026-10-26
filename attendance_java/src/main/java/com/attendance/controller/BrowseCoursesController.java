package com.attendance.controller;

import com.attendance.dao.CourseDAO;
import com.attendance.model.Course;
import com.attendance.model.User;
import com.attendance.util.SceneManager;
import com.attendance.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class BrowseCoursesController implements Initializable {

    @FXML private TableView<Course> courseTable;
    @FXML private TableColumn<Course, String> colCode;
    @FXML private TableColumn<Course, String> colName;
    @FXML private TableColumn<Course, Void> colAction;

    private final CourseDAO courseDAO = new CourseDAO();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        setupActionColumn();
        loadCourses();
    }

    private void loadCourses() {
        List<Course> courses = courseDAO.findAll();
        courseTable.setItems(FXCollections.observableArrayList(courses));
    }

    private void setupActionColumn() {
        colAction.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Course, Void> call(TableColumn<Course, Void> param) {
                return new TableCell<>() {
                    private final Button btn = new Button("Enroll");
                    {
                        btn.setOnAction(e -> {
                            Course course = getTableView().getItems().get(getIndex());
                            User user = SessionManager.getCurrentUser();
                            if (courseDAO.isEnrolled(user.getId(), course.getId())) {
                                SceneManager.showInfo("Already Enrolled", "You are already enrolled in this course.");
                            } else {
                                courseDAO.enroll(user.getId(), course.getId());
                                SceneManager.showInfo("Enrolled", "You have enrolled in " + course.getCode());
                            }
                        });
                    }
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : btn);
                    }
                };
            }
        });
    }

    @FXML
    private void goBack() {
        SceneManager.switchTo("student_dashboard.fxml", "Student Dashboard");
    }
}
