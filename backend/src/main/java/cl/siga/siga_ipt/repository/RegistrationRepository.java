package cl.siga.siga_ipt.repository;

import jakarta.servlet.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    Boolean existsByStudentRun(String studentRun);
    List<Registration> findAllByStudentRun(String studentRun);
    List<Registration> findAllByStudentRunAndPeriodCode(String studentRun, String periodCode);
    List<Registration> findAllBySectionCodeAndPeriodCode(String sectionCode, String periodCode);
}
