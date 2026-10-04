package cl.siga.siga_ipt.repository;

import cl.siga.siga_ipt.entity.CourseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {
    // Función usada en StudyPlanService
    // ver si existe una asignatura por su codigo
    Boolean existsByStudyPlanCode(String planCode);
    Boolean existsByCodeAndStudyPlanCode(String code, String studyPlanCode);
}
