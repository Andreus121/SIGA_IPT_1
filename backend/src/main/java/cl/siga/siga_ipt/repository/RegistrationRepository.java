package cl.siga.siga_ipt.repository;

import cl.siga.siga_ipt.entity.RegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistrationRepository extends JpaRepository<RegistrationEntity, Long> {
    Boolean existsByStudentRun(String studentRun);
    List<RegistrationEntity> findAllByStudentRun(String studentRun);
    List<RegistrationEntity> findAllByStudentRunAndPeriodCode(String studentRun, String periodCode);
    List<RegistrationEntity> findAllBySectionCodeAndPeriodCode(String sectionCode, String periodCode);
}
