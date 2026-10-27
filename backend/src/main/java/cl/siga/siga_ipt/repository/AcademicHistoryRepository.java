package cl.siga.siga_ipt.repository;

import cl.siga.siga_ipt.entity.AcademicHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AcademicHistoryRepository extends JpaRepository<AcademicHistoryEntity, Long> {
    boolean existsByStudentRun(String studentRun);
    boolean existsByTeacherRun(String teacherRun);
    boolean existsByCourseCodeAndStudyPlanCode(String courseCode, String studyPlanCode);
}