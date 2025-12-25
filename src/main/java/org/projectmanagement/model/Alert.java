package org.projectmanagement.model;

import java.sql.Timestamp;
import java.util.Objects;

public class Alert {
    private int id;
    private AlertType type;
    private Severity severity;
    private String title;
    private String message;
    private Member member;
    private Project project;
    private Task task;
    private boolean isRead;
    private Timestamp createdAt;

    public enum AlertType {
        OVERLOAD, CONFLICT, DELAY, DEADLINE, INFO
    }

    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public Alert() {
        this.isRead = false;
        this.severity = Severity.MEDIUM;
    }

    public Alert(AlertType type, String title, String message) {
        this();
        this.type = type;
        this.title = title;
        this.message = message;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public AlertType getType() {
        return type;
    }

    public void setType(AlertType type) {
        this.type = type;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }


    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Alert alert = (Alert) o;
        return id == alert.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Alert{" +
                "id=" + id +
                ", type=" + type +
                ", severity=" + severity +
                ", title='" + title + '\'' +
                ", isRead=" + isRead +
                '}';
    }
}
