package cl.siga.siga_ipt.repository;

import cl.siga.siga_ipt.entity.SectionEntity;
import cl.siga.siga_ipt.enums.PeriodStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SectionRepository extends JpaRepository<SectionEntity, Long> {
    boolean existsByTeacherIdAndPeriodStatus(Long teacherId, PeriodStatus periodStatus);
    boolean existsByTeacherId(Long teacherId);
}
