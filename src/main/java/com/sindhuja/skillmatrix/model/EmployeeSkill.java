package com.sindhuja.skillmatrix.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "employee_skills")
public class EmployeeSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    @JsonBackReference
    private Employee employee;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProficiencyLevel proficiency;

    // Constructors
    public EmployeeSkill() {}

    public EmployeeSkill(Employee employee, Skill skill, ProficiencyLevel proficiency) {
        this.employee = employee;
        this.skill = skill;
        this.proficiency = proficiency;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public ProficiencyLevel getProficiency() {
        return proficiency;
    }

    public void setProficiency(ProficiencyLevel proficiency) {
        this.proficiency = proficiency;
    }

    @Override
    public String toString() {
        return "EmployeeSkill{" +
                "id=" + id +
                ", skill=" + (skill != null ? skill.getName() : "null") +
                ", proficiency=" + proficiency +
                '}';
    }
}