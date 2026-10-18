package cl.siga.siga_ipt.repository;

import cl.siga.siga_ipt.entity.RegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistrationRepository extends JpaRepository<RegistrationEntity, Long> {
    boolean existsByStudentRun(String studentRun);
    boolean existsBySectionCodeAndPeriodCode(String sectionCode, String periodCode);
    boolean existsBySectionCode(String sectionCode);
    boolean existsByStudentRunAndCourseCodeAndPeriodCode(String studentRun, String courseCode, String periodCode);
    long countByStudentRunAndPeriodCode(String studentRun, String periodCode);
    long countBySectionCodeAndPeriodCode(String sectionCode, String periodCode);
    java.util.List<RegistrationEntity> findByStudentRunAndPeriodCode(String studentRun, String periodCode);
    java.util.List<RegistrationEntity> findBySectionCodeAndPeriodCode(String sectionCode, String periodCode);
    java.util.List<RegistrationEntity> findByPeriodCode(String periodCode);
}
