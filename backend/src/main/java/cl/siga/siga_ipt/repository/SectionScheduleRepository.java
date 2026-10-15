package cl.siga.siga_ipt.repository;

import cl.siga.siga_ipt.entity.SectionScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SectionScheduleRepository extends JpaRepository<SectionScheduleEntity, Long> {
    List<SectionScheduleEntity> findBySectionCodeAndPeriodCode(String sectionCode, String periodCode);
    void deleteBySectionCodeAndPeriodCode(String sectionCode, String periodCode);
}
