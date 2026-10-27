package cl.siga.siga_ipt.repository;

import cl.siga.siga_ipt.entity.EnrollmentEntity;
import cl.siga.siga_ipt.enums.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, Long> {
    boolean existsByStudentRunAndPeriodCode(String studentRun, String periodCode);
    Optional<EnrollmentEntity> findByStudentRunAndPeriodCode(String studentRun, String periodCode);
    List<EnrollmentEntity> findByPeriodCode(String periodCode);
    List<EnrollmentEntity> findByStudentRun(String studentRun);
}
