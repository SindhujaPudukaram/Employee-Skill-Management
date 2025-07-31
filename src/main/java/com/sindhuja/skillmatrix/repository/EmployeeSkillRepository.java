package com.sindhuja.skillmatrix.repository;

import com.sindhuja.skillmatrix.model.EmployeeSkill;
import com.sindhuja.skillmatrix.model.ProficiencyLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeSkillRepository extends JpaRepository<EmployeeSkill, Long> {

    // Find by skill name (exact match)
    List<EmployeeSkill> findBySkill_Name(String skillName);

    // Find by skill name (case insensitive)
    @Query("SELECT es FROM EmployeeSkill es WHERE LOWER(es.skill.name) = LOWER(:skillName)")
    List<EmployeeSkill> findBySkill_NameIgnoreCase(@Param("skillName") String skillName);

    // Find by employee ID
    List<EmployeeSkill> findByEmployee_Id(Long employeeId);

    // Find by proficiency level
    List<EmployeeSkill> findByProficiency(ProficiencyLevel proficiency);

    // Find by skill name and proficiency level
    @Query("SELECT es FROM EmployeeSkill es WHERE LOWER(es.skill.name) = LOWER(:skillName) AND es.proficiency = :proficiency")
    List<EmployeeSkill> findBySkillNameAndProficiency(@Param("skillName") String skillName,
                                                      @Param("proficiency") ProficiencyLevel proficiency);

    // Find by employee ID and skill name (case insensitive)
    @Query("SELECT es FROM EmployeeSkill es WHERE es.employee.id = :employeeId AND LOWER(es.skill.name) = LOWER(:skillName)")
    Optional<EmployeeSkill> findByEmployeeIdAndSkillName(@Param("employeeId") Long employeeId,
                                                         @Param("skillName") String skillName);

    // Check if employee has a specific skill
    @Query("SELECT CASE WHEN COUNT(es) > 0 THEN true ELSE false END FROM EmployeeSkill es WHERE es.employee.id = :employeeId AND LOWER(es.skill.name) = LOWER(:skillName)")
    boolean existsByEmployee_IdAndSkill_Name(@Param("employeeId") Long employeeId, @Param("skillName") String skillName);

    // Find skills by employee ID ordered by skill name
    @Query("SELECT es FROM EmployeeSkill es WHERE es.employee.id = :employeeId ORDER BY es.skill.name ASC")
    List<EmployeeSkill> findByEmployee_IdOrderBySkillName(@Param("employeeId") Long employeeId);

    // Count skills by proficiency level
    @Query("SELECT COUNT(es) FROM EmployeeSkill es WHERE es.proficiency = :proficiency")
    long countByProficiency(@Param("proficiency") ProficiencyLevel proficiency);

    // Get all unique skill names
    @Query("SELECT DISTINCT es.skill.name FROM EmployeeSkill es ORDER BY es.skill.name")
    List<String> findAllUniqueSkillNames();

    // Count total employee skill associations
    @Query("SELECT COUNT(es) FROM EmployeeSkill es")
    long countAllEmployeeSkills();

    // Find employees with multiple skills (having more than specified count)
    @Query("SELECT es FROM EmployeeSkill es WHERE es.employee.id IN " +
            "(SELECT es2.employee.id FROM EmployeeSkill es2 GROUP BY es2.employee.id HAVING COUNT(es2) > :minSkillCount)")
    List<EmployeeSkill> findEmployeesWithMultipleSkills(@Param("minSkillCount") long minSkillCount);
}