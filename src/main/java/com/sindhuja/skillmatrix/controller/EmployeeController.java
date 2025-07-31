package com.sindhuja.skillmatrix.controller;

import com.sindhuja.skillmatrix.model.Employee;
import com.sindhuja.skillmatrix.model.EmployeeSkill;
import com.sindhuja.skillmatrix.model.Skill;
import com.sindhuja.skillmatrix.model.ProficiencyLevel;
import com.sindhuja.skillmatrix.repository.EmployeeRepository;
import com.sindhuja.skillmatrix.repository.SkillRepository;
import com.sindhuja.skillmatrix.repository.EmployeeSkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
public class EmployeeController {

    @Autowired
    private EmployeeRepository employeeRepo;

    @Autowired
    private SkillRepository skillRepo;

    @Autowired
    private EmployeeSkillRepository employeeSkillRepo;

    // DTO Classes
    public static class EmployeeDTO {
        public String name;
        public String email;
        public List<SkillDTO> skills;

        public static class SkillDTO {
            public String name;
            public String proficiency;
        }
    }

    public static class EmployeeUpdateDTO {
        public String name;
        public String email;
    }

    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        try {
            System.out.println("=== GET ALL EMPLOYEES ===");
            List<Employee> employees = employeeRepo.findAll();
            System.out.println("Found " + employees.size() + " employees in database");

            for (Employee emp : employees) {
                System.out.println("Employee: " + emp.getName() + " (" + emp.getEmail() + ") - Skills: " + emp.getSkills().size());
            }

            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            System.err.println("Error getting all employees: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable Long id) {
        try {
            System.out.println("Getting employee by ID: " + id);
            Optional<Employee> employee = employeeRepo.findById(id);

            if (employee.isPresent()) {
                System.out.println("Employee found: " + employee.get().getName());
                return ResponseEntity.ok(employee.get());
            } else {
                System.out.println("Employee not found with ID: " + id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            System.err.println("Error getting employee by ID: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Employee>> searchEmployeesByName(@RequestParam String name) {
        try {
            System.out.println("Searching employees by name: " + name);
            List<Employee> employees = employeeRepo.findByNameContainingIgnoreCase(name);
            System.out.println("Found " + employees.size() + " employees matching: " + name);
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            System.err.println("Error searching employees: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> addEmployee(@RequestBody EmployeeDTO dto) {
        try {
            System.out.println("=== ADDING EMPLOYEE ===");
            System.out.println("Received data: " + dto.name + " (" + dto.email + ")");
            System.out.println("Skills to add: " + (dto.skills != null ? dto.skills.size() : 0));

            // Input validation
            if (dto.name == null || dto.name.trim().isEmpty()) {
                System.out.println("Validation failed: Empty name");
                return ResponseEntity.badRequest().body("Employee name is required and cannot be empty");
            }

            if (dto.email == null || dto.email.trim().isEmpty()) {
                System.out.println("Validation failed: Empty email");
                return ResponseEntity.badRequest().body("Employee email is required and cannot be empty");
            }

            // Check for duplicate email
            String trimmedEmail = dto.email.trim().toLowerCase();
            if (employeeRepo.findByEmail(trimmedEmail).isPresent()) {
                System.out.println("Validation failed: Duplicate email - " + trimmedEmail);
                return ResponseEntity.badRequest().body("Employee with email '" + trimmedEmail + "' already exists");
            }

            // Create employee
            Employee employee = new Employee();
            employee.setName(dto.name.trim());
            employee.setEmail(trimmedEmail);

            System.out.println("Saving employee to database...");
            Employee savedEmployee = employeeRepo.save(employee);
            System.out.println("Employee saved with ID: " + savedEmployee.getId());

            // Process skills
            List<EmployeeSkill> employeeSkills = new ArrayList<>();

            if (dto.skills != null && !dto.skills.isEmpty()) {
                System.out.println("Processing " + dto.skills.size() + " skills...");

                for (int i = 0; i < dto.skills.size(); i++) {
                    EmployeeDTO.SkillDTO skillDto = dto.skills.get(i);
                    System.out.println("Processing skill " + (i + 1) + ": " + skillDto.name + " (" + skillDto.proficiency + ")");

                    // Validate skill data
                    if (skillDto.name == null || skillDto.name.trim().isEmpty()) {
                        System.out.println("Skipping skill with empty name");
                        continue;
                    }

                    if (skillDto.proficiency == null || skillDto.proficiency.trim().isEmpty()) {
                        System.out.println("Skipping skill with empty proficiency");
                        continue;
                    }

                    String skillName = skillDto.name.trim();

                    // Check for duplicate skills for this employee
                    boolean skillExists = employeeSkills.stream()
                            .anyMatch(es -> es.getSkill().getName().equalsIgnoreCase(skillName));

                    if (skillExists) {
                        System.out.println("Skipping duplicate skill: " + skillName);
                        continue;
                    }

                    // Find or create skill
                    Optional<Skill> existingSkill = skillRepo.findByNameIgnoreCase(skillName);
                    Skill skill;

                    if (existingSkill.isPresent()) {
                        skill = existingSkill.get();
                        System.out.println("Using existing skill: " + skill.getName() + " (ID: " + skill.getId() + ")");
                    } else {
                        skill = new Skill(skillName);
                        skill = skillRepo.save(skill);
                        System.out.println("Created new skill: " + skill.getName() + " (ID: " + skill.getId() + ")");
                    }

                    // Validate proficiency level
                    ProficiencyLevel proficiencyLevel;
                    try {
                        proficiencyLevel = ProficiencyLevel.valueOf(skillDto.proficiency.trim().toUpperCase());
                        System.out.println("Proficiency level: " + proficiencyLevel);
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid proficiency level: " + skillDto.proficiency);
                        return ResponseEntity.badRequest().body(
                                "Invalid proficiency level: '" + skillDto.proficiency +
                                        "'. Must be one of: BEGINNER, INTERMEDIATE, EXPERT"
                        );
                    }

                    // Create EmployeeSkill association
                    EmployeeSkill employeeSkill = new EmployeeSkill();
                    employeeSkill.setEmployee(savedEmployee);
                    employeeSkill.setSkill(skill);
                    employeeSkill.setProficiency(proficiencyLevel);

                    System.out.println("Saving EmployeeSkill association...");
                    EmployeeSkill savedEmployeeSkill = employeeSkillRepo.save(employeeSkill);
                    System.out.println("EmployeeSkill saved with ID: " + savedEmployeeSkill.getId());

                    employeeSkills.add(savedEmployeeSkill);
                }
            } else {
                System.out.println("No skills provided");
            }

            // Update employee with skills
            savedEmployee.setSkills(employeeSkills);
            System.out.println("Employee created successfully with " + employeeSkills.size() + " skills");
            System.out.println("=== EMPLOYEE CREATION COMPLETED ===");

            return ResponseEntity.ok(savedEmployee);

        } catch (Exception e) {
            System.err.println("ERROR: Failed to save employee");
            System.err.println("Error message: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to save employee: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateEmployee(@PathVariable Long id, @RequestBody EmployeeUpdateDTO dto) {
        try {
            System.out.println("=== UPDATING EMPLOYEE " + id + " ===");

            Optional<Employee> employeeOpt = employeeRepo.findById(id);
            if (employeeOpt.isEmpty()) {
                System.out.println("Employee not found with ID: " + id);
                return ResponseEntity.notFound().build();
            }

            Employee employee = employeeOpt.get();
            System.out.println("Updating employee: " + employee.getName());

            // Validate input
            if (dto.name == null || dto.name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Employee name is required");
            }
            if (dto.email == null || dto.email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Employee email is required");
            }

            String newEmail = dto.email.trim().toLowerCase();

            // Check for email conflicts
            if (!employee.getEmail().equalsIgnoreCase(newEmail)) {
                if (employeeRepo.findByEmail(newEmail).isPresent()) {
                    return ResponseEntity.badRequest().body("Email '" + newEmail + "' is already used by another employee");
                }
            }

            employee.setName(dto.name.trim());
            employee.setEmail(newEmail);

            Employee updatedEmployee = employeeRepo.save(employee);
            System.out.println("Employee updated successfully");

            return ResponseEntity.ok(updatedEmployee);

        } catch (Exception e) {
            System.err.println("Error updating employee: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error updating employee: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteEmployee(@PathVariable Long id) {
        try {
            System.out.println("=== DELETING EMPLOYEE " + id + " ===");

            Optional<Employee> employee = employeeRepo.findById(id);
            if (employee.isEmpty()) {
                System.out.println("Employee not found with ID: " + id);
                return ResponseEntity.notFound().build();
            }

            String employeeName = employee.get().getName();
            System.out.println("Deleting employee: " + employeeName);

            employeeRepo.deleteById(id);
            System.out.println("Employee deleted successfully (cascade should handle skills)");

            return ResponseEntity.ok("Employee '" + employeeName + "' deleted successfully");

        } catch (Exception e) {
            System.err.println("Error deleting employee: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error deleting employee: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/skills")
    public ResponseEntity<List<EmployeeSkill>> getEmployeeSkills(@PathVariable Long id) {
        try {
            System.out.println("Getting skills for employee ID: " + id);

            if (!employeeRepo.existsById(id)) {
                System.out.println("Employee not found with ID: " + id);
                return ResponseEntity.notFound().build();
            }

            List<EmployeeSkill> skills = employeeSkillRepo.findByEmployee_Id(id);
            System.out.println("Found " + skills.size() + " skills for employee " + id);

            return ResponseEntity.ok(skills);

        } catch (Exception e) {
            System.err.println("Error getting employee skills: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // Debug endpoint for testing
    @GetMapping("/debug/stats")
    public ResponseEntity<?> getDebugStats() {
        try {
            System.out.println("=== DEBUG STATS REQUEST ===");

            long employeeCount = employeeRepo.count();
            long skillCount = skillRepo.count();
            long employeeSkillCount = employeeSkillRepo.count();

            Map<String, Object> stats = new HashMap<>();
            stats.put("employees", employeeCount);
            stats.put("skills", skillCount);
            stats.put("employeeSkills", employeeSkillCount);
            stats.put("timestamp", new Date());
            stats.put("status", "OK");

            System.out.println("Debug stats: " + stats);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("Error getting debug stats: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Database error: " + e.getMessage());
        }
    }
}