package com.sindhuja.skillmatrix.controller;

import com.sindhuja.skillmatrix.model.Employee;
import com.sindhuja.skillmatrix.model.EmployeeSkill;
import com.sindhuja.skillmatrix.model.Skill;
import com.sindhuja.skillmatrix.model.ProficiencyLevel;
import com.sindhuja.skillmatrix.repository.EmployeeRepository;
import com.sindhuja.skillmatrix.repository.EmployeeSkillRepository;
import com.sindhuja.skillmatrix.repository.SkillRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employee-skills")
@CrossOrigin(origins = "*")
public class EmployeeSkillController {

    @Autowired
    private EmployeeSkillRepository employeeSkillRepo;

    @Autowired
    private EmployeeRepository employeeRepo;

    @Autowired
    private SkillRepository skillRepo;

    // DTO Classes
    public static class EmployeeSkillDTO {
        public Long employeeId;
        public String skillName;
        public String proficiency;
    }

    public static class EmployeeSkillUpdateDTO {
        public String proficiency;
    }

    @GetMapping
    public ResponseEntity<List<EmployeeSkill>> getAllEmployeeSkills() {
        try {
            System.out.println("=== GET ALL EMPLOYEE SKILLS ===");
            List<EmployeeSkill> employeeSkills = employeeSkillRepo.findAll();
            System.out.println("Found " + employeeSkills.size() + " employee skill associations");
            return ResponseEntity.ok(employeeSkills);
        } catch (Exception e) {
            System.err.println("Error getting all employee skills: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/by-skill")
    public ResponseEntity<List<EmployeeSkill>> getBySkill(
            @RequestParam String skill,
            @RequestParam(required = false) String proficiency) {

        try {
            System.out.println("=== SEARCH BY SKILL ===");
            System.out.println("Searching for skill: '" + skill + "'");
            System.out.println("Proficiency filter: " + (proficiency != null ? "'" + proficiency + "'" : "none"));

            // First try exact match (case insensitive)
            List<EmployeeSkill> results = employeeSkillRepo.findBySkill_Name(skill);
            System.out.println("Exact match results: " + results.size());

            // If no exact match, try case-insensitive search
            if (results.isEmpty()) {
                System.out.println("No exact match found, trying case-insensitive search...");
                List<Skill> allSkills = skillRepo.findAll();

                Optional<Skill> matchingSkill = allSkills.stream()
                        .filter(s -> s.getName().equalsIgnoreCase(skill))
                        .findFirst();

                if (matchingSkill.isPresent()) {
                    results = employeeSkillRepo.findBySkill_Name(matchingSkill.get().getName());
                    System.out.println("Case-insensitive match results: " + results.size());
                }
            }

            // Filter by proficiency if provided
            if (proficiency != null && !proficiency.trim().isEmpty()) {
                System.out.println("Applying proficiency filter: " + proficiency);

                try {
                    ProficiencyLevel profLevel = ProficiencyLevel.valueOf(proficiency.toUpperCase());
                    results = results.stream()
                            .filter(es -> es.getProficiency() == profLevel)
                            .collect(Collectors.toList());
                    System.out.println("After proficiency filter: " + results.size() + " results");
                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid proficiency level: " + proficiency);
                    return ResponseEntity.badRequest().build();
                }
            }

            System.out.println("Final results: " + results.size() + " employee skills found");

            // Log the results for debugging
            for (EmployeeSkill es : results) {
                System.out.println("Result: Employee " + es.getEmployee().getName() +
                        " has skill " + es.getSkill().getName() +
                        " at " + es.getProficiency() + " level");
            }

            return ResponseEntity.ok(results);

        } catch (Exception e) {
            System.err.println("Error searching by skill: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/by-employee")
    public ResponseEntity<List<EmployeeSkill>> getByEmployee(@RequestParam Long employeeId) {
        try {
            System.out.println("=== GET SKILLS BY EMPLOYEE ===");
            System.out.println("Getting skills for employee ID: " + employeeId);

            if (!employeeRepo.existsById(employeeId)) {
                System.out.println("Employee not found with ID: " + employeeId);
                return ResponseEntity.notFound().build();
            }

            List<EmployeeSkill> skills = employeeSkillRepo.findByEmployee_Id(employeeId);
            System.out.println("Found " + skills.size() + " skills for employee " + employeeId);

            return ResponseEntity.ok(skills);

        } catch (Exception e) {
            System.err.println("Error getting skills by employee: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> addEmployeeSkill(@RequestBody EmployeeSkillDTO dto) {
        try {
            System.out.println("=== ADDING EMPLOYEE SKILL ===");
            System.out.println("Employee ID: " + dto.employeeId);
            System.out.println("Skill name: " + dto.skillName);
            System.out.println("Proficiency: " + dto.proficiency);

            // Validate input
            if (dto.employeeId == null) {
                return ResponseEntity.badRequest().body("Employee ID is required");
            }
            if (dto.skillName == null || dto.skillName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Skill name is required");
            }
            if (dto.proficiency == null || dto.proficiency.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Proficiency level is required");
            }

            // Find employee
            Optional<Employee> employeeOpt = employeeRepo.findById(dto.employeeId);
            if (employeeOpt.isEmpty()) {
                System.out.println("Employee not found with ID: " + dto.employeeId);
                return ResponseEntity.badRequest().body("Employee not found with ID: " + dto.employeeId);
            }
            Employee employee = employeeOpt.get();
            System.out.println("Found employee: " + employee.getName());

            // Check if employee already has this skill
            String skillName = dto.skillName.trim();
            Optional<EmployeeSkill> existingSkill = employeeSkillRepo.findByEmployeeIdAndSkillName(dto.employeeId, skillName);

            if (existingSkill.isPresent()) {
                System.out.println("Employee already has this skill: " + skillName);
                return ResponseEntity.badRequest().body("Employee already has skill: " + skillName);
            }

            // Find or create skill
            Optional<Skill> skillOpt = skillRepo.findByNameIgnoreCase(skillName);
            Skill skill;

            if (skillOpt.isPresent()) {
                skill = skillOpt.get();
                System.out.println("Using existing skill: " + skill.getName() + " (ID: " + skill.getId() + ")");
            } else {
                skill = new Skill(skillName);
                skill = skillRepo.save(skill);
                System.out.println("Created new skill: " + skill.getName() + " (ID: " + skill.getId() + ")");
            }

            // Validate proficiency level
            ProficiencyLevel proficiencyLevel;
            try {
                proficiencyLevel = ProficiencyLevel.valueOf(dto.proficiency.trim().toUpperCase());
                System.out.println("Proficiency level: " + proficiencyLevel);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid proficiency level: " + dto.proficiency);
                return ResponseEntity.badRequest().body("Invalid proficiency level: '" + dto.proficiency +
                        "'. Must be one of: BEGINNER, INTERMEDIATE, EXPERT");
            }

            // Create EmployeeSkill
            EmployeeSkill empSkill = new EmployeeSkill();
            empSkill.setEmployee(employee);
            empSkill.setSkill(skill);
            empSkill.setProficiency(proficiencyLevel);

            EmployeeSkill savedSkill = employeeSkillRepo.save(empSkill);
            System.out.println("EmployeeSkill saved with ID: " + savedSkill.getId());

            return ResponseEntity.ok(savedSkill);

        } catch (Exception e) {
            System.err.println("Error adding employee skill: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error adding skill: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> updateEmployeeSkill(@PathVariable Long id, @RequestBody EmployeeSkillUpdateDTO dto) {
        try {
            System.out.println("=== UPDATING EMPLOYEE SKILL ===");
            System.out.println("EmployeeSkill ID: " + id);
            System.out.println("New proficiency: " + dto.proficiency);

            // Validate input
            if (dto.proficiency == null || dto.proficiency.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Proficiency level is required");
            }

            // Find EmployeeSkill
            Optional<EmployeeSkill> empSkillOpt = employeeSkillRepo.findById(id);
            if (empSkillOpt.isEmpty()) {
                System.out.println("EmployeeSkill not found with ID: " + id);
                return ResponseEntity.notFound().build();
            }

            EmployeeSkill empSkill = empSkillOpt.get();
            System.out.println("Found EmployeeSkill: Employee " + empSkill.getEmployee().getName() +
                    " - Skill " + empSkill.getSkill().getName());

            // Validate proficiency level
            ProficiencyLevel proficiencyLevel;
            try {
                proficiencyLevel = ProficiencyLevel.valueOf(dto.proficiency.trim().toUpperCase());
                System.out.println("Updating proficiency from " + empSkill.getProficiency() + " to " + proficiencyLevel);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid proficiency level: " + dto.proficiency);
                return ResponseEntity.badRequest().body("Invalid proficiency level: '" + dto.proficiency +
                        "'. Must be one of: BEGINNER, INTERMEDIATE, EXPERT");
            }

            empSkill.setProficiency(proficiencyLevel);
            EmployeeSkill updatedSkill = employeeSkillRepo.save(empSkill);
            System.out.println("EmployeeSkill updated successfully");

            return ResponseEntity.ok(updatedSkill);

        } catch (Exception e) {
            System.err.println("Error updating employee skill: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error updating skill: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> deleteEmployeeSkill(@PathVariable Long id) {
        try {
            System.out.println("=== DELETING EMPLOYEE SKILL ===");
            System.out.println("EmployeeSkill ID: " + id);

            Optional<EmployeeSkill> empSkillOpt = employeeSkillRepo.findById(id);
            if (empSkillOpt.isEmpty()) {
                System.out.println("EmployeeSkill not found with ID: " + id);
                return ResponseEntity.notFound().build();
            }

            EmployeeSkill empSkill = empSkillOpt.get();
            System.out.println("Deleting skill: " + empSkill.getSkill().getName() +
                    " from employee: " + empSkill.getEmployee().getName());

            employeeSkillRepo.deleteById(id);
            System.out.println("EmployeeSkill deleted successfully");

            return ResponseEntity.ok().body("Employee skill deleted successfully");

        } catch (Exception e) {
            System.err.println("Error deleting employee skill: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error deleting skill: " + e.getMessage());
        }
    }

    @DeleteMapping("/employee/{employeeId}/skill/{skillName}")
    @Transactional
    public ResponseEntity<?> deleteEmployeeSkillByName(@PathVariable Long employeeId, @PathVariable String skillName) {
        try {
            System.out.println("=== DELETING EMPLOYEE SKILL BY NAME ===");
            System.out.println("Employee ID: " + employeeId);
            System.out.println("Skill name: " + skillName);

            Optional<EmployeeSkill> skillToDelete = employeeSkillRepo.findByEmployeeIdAndSkillName(employeeId, skillName);

            if (skillToDelete.isEmpty()) {
                System.out.println("EmployeeSkill not found for employee " + employeeId + " and skill " + skillName);
                return ResponseEntity.notFound().build();
            }

            EmployeeSkill empSkill = skillToDelete.get();
            System.out.println("Found and deleting skill: " + empSkill.getSkill().getName() +
                    " from employee: " + empSkill.getEmployee().getName());

            employeeSkillRepo.delete(empSkill);
            System.out.println("EmployeeSkill deleted successfully");

            return ResponseEntity.ok().body("Employee skill deleted successfully");

        } catch (Exception e) {
            System.err.println("Error deleting employee skill by name: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error deleting skill: " + e.getMessage());
        }
    }
}