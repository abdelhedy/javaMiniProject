package org.projectmanagement.model;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Project {
    private int id;
    private String name;
    private String description;
    private Date startDate;
    private Date deadline;
    private ProjectStatus status;
    private List<Task> tasks;


    public enum ProjectStatus {
        PLANNING, IN_PROGRESS, COMPLETED, CANCELLED
    }

    public Project() {
        this.tasks = new ArrayList<>();
        this.status = ProjectStatus.PLANNING;
    }

    public Project(String name, String description, Date startDate, Date deadline) {
        this();
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.deadline = deadline;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    public ProjectStatus getStatus() {
        return status;
    }

    public void setStatus(ProjectStatus status) {
        this.status = status;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    // Business methods
    public int getTotalTasks() {
        return tasks.size();
    }

    public int getCompletedTasks() {
        return (int) tasks.stream().filter(t -> t.getStatus() == Task.TaskStatus.COMPLETED).count();
    }

    public double getCompletionPercentage() {
        if (tasks.isEmpty()) return 0;
        return (getCompletedTasks() * 100.0) / getTotalTasks();
    }

    public double getTotalEstimatedHours() {
        return tasks.stream().mapToDouble(Task::getEstimatedHours).sum();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return id == project.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", startDate=" + startDate +
                ", deadline=" + deadline +
                ", completion=" + String.format("%.2f", getCompletionPercentage()) + "%" +
                '}';
    }
}
