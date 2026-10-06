package cl.siga.siga_ipt.repository;

import cl.siga.siga_ipt.entity.PrerequisiteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrerequisiteRepository extends JpaRepository<PrerequisiteEntity, Long> {
    List<PrerequisiteEntity> findByCourseCodeAndStudyPlanCode(String courseCode, String studyPlanCode);
    List<PrerequisiteEntity> findByPrerequisiteCourseCodeAndStudyPlanCode(String prerequisiteCourseCode, String studyPlanCode);
    void deleteByCourseCodeAndStudyPlanCode(String courseCode, String studyPlanCode);
    boolean existsByPrerequisiteCourseCodeAndStudyPlanCode(String prerequisiteCourseCode, String studyPlanCode);
}