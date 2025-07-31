package com.sindhuja.skillmatrix.controller;

import com.sindhuja.skillmatrix.model.Skill;
import com.sindhuja.skillmatrix.repository.SkillRepository;
import com.sindhuja.skillmatrix.repository.EmployeeSkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

    @Autowired
    private SkillRepository skillRepo;

    @Autowired
    private EmployeeSkillRepository employeeSkillRepo;

    public static class SkillDTO {
        public String name;
    }

    @GetMapping
    public List<Skill> getAllSkills() {
        return skillRepo.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Skill> getSkillById(@PathVariable Long id) {
        Optional<Skill> skill = skillRepo.findById(id);
        if (skill.isPresent()) {
            return ResponseEntity.ok(skill.get());
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Skill> getSkillByName(@RequestParam String name) {
        Optional<Skill> skill = skillRepo.findByName(name);
        if (skill.isPresent()) {
            return ResponseEntity.ok(skill.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> addSkill(@RequestBody SkillDTO dto) {
        if (dto.name == null || dto.name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Skill name must not be empty");
        }

        // Check if skill already exists
        boolean exists = skillRepo.findByName(dto.name.trim()).isPresent();
        if (exists) {
            return ResponseEntity.badRequest().body("Skill already exists");
        }

        Skill skill = new Skill(dto.name.trim());
        Skill savedSkill = skillRepo.save(skill);
        return ResponseEntity.ok(savedSkill);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateSkill(@PathVariable Long id, @RequestBody SkillDTO dto) {
        try {
            Optional<Skill> skillOpt = skillRepo.findById(id);
            if (skillOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            if (dto.name == null || dto.name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Skill name must not be empty");
            }

            // Check if new name conflicts with existing skill
            Optional<Skill> existingSkill = skillRepo.findByName(dto.name.trim());
            if (existingSkill.isPresent() && !existingSkill.get().getId().equals(id)) {
                return ResponseEntity.badRequest().body("Skill name already exists");
            }

            Skill skill = skillOpt.get();
            skill.setName(dto.name.trim());

            Skill updatedSkill = skillRepo.save(skill);
            return ResponseEntity.ok(updatedSkill);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating skill: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSkill(@PathVariable Long id) {
        try {
            if (!skillRepo.existsById(id)) {
                return ResponseEntity.notFound().build();
            }

            // Check if skill is being used by any employee
            long usageCount = employeeSkillRepo.findBySkill_Name(
                    skillRepo.findById(id).get().getName()
            ).size();

            if (usageCount > 0) {
                return ResponseEntity.badRequest().body(
                        "Cannot delete skill. It is assigned to " + usageCount + " employee(s)"
                );
            }

            skillRepo.deleteById(id);
            return ResponseEntity.ok().body("Skill deleted successfully");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting skill: " + e.getMessage());
        }
    }
}