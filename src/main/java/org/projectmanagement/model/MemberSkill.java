package org.projectmanagement.model;

public class MemberSkill {
    private Member member;
    private Skill skill;
    private int proficiencyLevel; // 1-5 scale

    public MemberSkill() {
    }

    public MemberSkill(Member member, Skill skill, int proficiencyLevel) {
        this.member = member;
        this.skill = skill;
        this.proficiencyLevel = proficiencyLevel;
    }

    // Getters and Setters
    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public int getProficiencyLevel() {
        return proficiencyLevel;
    }

    public void setProficiencyLevel(int proficiencyLevel) {
        this.proficiencyLevel = proficiencyLevel;
    }

    @Override
    public String toString() {
        return "MemberSkill{" +
                "memberName= " + member.getName() +
                ", skillId= " + skill.getId() +
                ", skillName= '" + skill.getName() + '\'' +
                ", proficiencyLevel= " + proficiencyLevel +
                '}';
    }
}
