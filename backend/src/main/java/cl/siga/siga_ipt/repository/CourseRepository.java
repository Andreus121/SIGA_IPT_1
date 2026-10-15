package cl.siga.siga_ipt.repository;

import cl.siga.siga_ipt.entity.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {
    boolean existsByStudyPlanCode(String planCode);
    boolean existsByCodeAndStudyPlanCode(String code, String studyPlanCode);
    Optional<CourseEntity> findByCodeAndStudyPlanCode(String code, String studyPlanCode);
    Optional<CourseEntity> findFirstByCode(String code);
    List<CourseEntity> findByStudyPlanCode(String studyPlanCode);
}
