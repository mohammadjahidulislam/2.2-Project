package com.attendance.model;

public class Course {
    private int id;
    private String code;
    private String name;
    private int teacherId;

    public Course() {}

    public Course(int id, String code, String name, int teacherId) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.teacherId = teacherId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getTeacherId() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }

    @Override
    public String toString() {
        return code + " - " + name;
    }
}
