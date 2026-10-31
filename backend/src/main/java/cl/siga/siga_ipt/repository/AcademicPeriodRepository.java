package cl.siga.siga_ipt.repository;

import cl.siga.siga_ipt.entity.AcademicPeriodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AcademicPeriodRepository extends JpaRepository<AcademicPeriodEntity, Long> {
    Optional<AcademicPeriodEntity> findByPeriodCode(String periodCode);
    boolean existsByPeriodCode(String periodCode);
    boolean existsByAcademicPeriod(String academicPeriod);
}
