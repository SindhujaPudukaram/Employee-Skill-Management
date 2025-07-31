
package com.sindhuja.skillmatrix.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "skills", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name")
})
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // Constructors
    public Skill() {}

    public Skill(String name) {
        this.name = name;
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

    // equals and hashCode (important for Set operations or comparisons)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Skill)) return false;
        Skill skill = (Skill) o;
        return Objects.equals(name, skill.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    // Optional toString
    @Override
    public String toString() {
        return "Skill{name='" + name + "'}";
    }
}
