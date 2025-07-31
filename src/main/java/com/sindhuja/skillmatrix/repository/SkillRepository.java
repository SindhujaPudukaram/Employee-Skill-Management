package com.sindhuja.skillmatrix.repository;

import com.sindhuja.skillmatrix.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    // Find by exact name
    Optional<Skill> findByName(String name);

    // Find by name (case insensitive)
    @Query("SELECT s FROM Skill s WHERE LOWER(s.name) = LOWER(:name)")
    Optional<Skill> findByNameIgnoreCase(@Param("name") String name);

    // Search skills by partial name (case insensitive)
    List<Skill> findByNameContainingIgnoreCase(String namePattern);

    // Check if skill exists by name
    boolean existsByName(String name);

    // Check if skill exists by name (case insensitive)
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Skill s WHERE LOWER(s.name) = LOWER(:name)")
    boolean existsByNameIgnoreCase(@Param("name") String name);

    // Count total skills
    @Query("SELECT COUNT(s) FROM Skill s")
    long countAllSkills();

    // Find skills ordered by name
    List<Skill> findAllByOrderByNameAsc();
}