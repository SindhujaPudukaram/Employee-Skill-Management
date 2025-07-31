package com.sindhuja.skillmatrix.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<EmployeeSkill> skills = new ArrayList<>();

    // Constructors
    public Employee() {}

    public Employee(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<EmployeeSkill> getSkills() {
        return skills;
    }

    public void setSkills(List<EmployeeSkill> skills) {
        this.skills = skills != null ? skills : new ArrayList<>();

        // Make sure each EmployeeSkill references this employee
        for (EmployeeSkill skill : this.skills) {
            skill.setEmployee(this);
        }
    }

    // Helper method to add a skill
    public void addSkill(EmployeeSkill skill) {
        skills.add(skill);
        skill.setEmployee(this);
    }

    // Helper method to remove a skill
    public void removeSkill(EmployeeSkill skill) {
        skills.remove(skill);
        skill.setEmployee(null);
    }

    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", skillsCount=" + (skills != null ? skills.size() : 0) +
                '}';
    }
}