package cl.siga.siga_ipt.repository;

import cl.siga.siga_ipt.entity.SectionEntity;
import cl.siga.siga_ipt.enums.PeriodStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SectionRepository extends JpaRepository<SectionEntity, Long> {
    boolean existsByTeacherId(Long teacherId);
    boolean existsByTeacherIdAndPeriodCode(Long teacherId, String periodCode);
    boolean existsByCourseCodeAndPeriodCode(String courseCode, String periodCode);
    boolean existsByCode(String code);
    java.util.Optional<SectionEntity> findByCode(String code);
    java.util.List<SectionEntity> findByPeriodCode(String periodCode);
    java.util.List<SectionEntity> findByTeacherIdAndPeriodCode(Long teacherId, String periodCode);
}
