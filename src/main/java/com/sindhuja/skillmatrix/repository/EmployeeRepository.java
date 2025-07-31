package com.sindhuja.skillmatrix.repository;

import com.sindhuja.skillmatrix.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Find by email (case insensitive)
    @Query("SELECT e FROM Employee e WHERE LOWER(e.email) = LOWER(:email)")
    Optional<Employee> findByEmail(@Param("email") String email);

    // Search by name (case insensitive)
    List<Employee> findByNameContainingIgnoreCase(String name);

    // Search by email (case insensitive)
    List<Employee> findByEmailContainingIgnoreCase(String email);

    // Find employees by skill name
    @Query("SELECT DISTINCT e FROM Employee e JOIN e.skills es WHERE LOWER(es.skill.name) = LOWER(:skillName)")
    List<Employee> findBySkillName(@Param("skillName") String skillName);

    // Count total employees
    @Query("SELECT COUNT(e) FROM Employee e")
    long countAllEmployees();

    // Check if email exists (case insensitive)
    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Employee e WHERE LOWER(e.email) = LOWER(:email)")
    boolean existsByEmailIgnoreCase(@Param("email") String email);
}
