package org.projectmanagement.model;

public class TaskSkill {
    private Task task;
    private Skill skill;
    private int requiredLevel; // minimum proficiency required

    public TaskSkill() {
    }

    public TaskSkill(Task task, Skill skill, int requiredLevel) {
        this.task = task;
        this.skill = skill;
        this.requiredLevel = requiredLevel;
    }



    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }


    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public void setRequiredLevel(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    @Override
    public String toString() {
        return "TaskSkill{" +
                "taskId=" + task.getId() +
                ", skillId=" + skill.getId() +
                ", skillName='" + skill.getName() + '\'' +
                ", requiredLevel=" + requiredLevel +
                '}';
    }
}
