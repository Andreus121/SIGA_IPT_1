package cl.siga.siga_ipt.repository;

import cl.siga.siga_ipt.entity.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {
    // Función usada en StudyPlanService
    // ver si existe una asignatura por su codigo
    boolean existsById(Long id);
    boolean existsByStudyPlanCode(String planCode);
    boolean existsByCodeAndStudyPlanCode(String code, String studyPlanCode);
}
