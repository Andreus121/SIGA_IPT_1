package cl.siga.siga_ipt.repository;

import cl.siga.siga_ipt.entity.StudyPlanEntity;
import cl.siga.siga_ipt.enums.StudyPlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudyPlanRepository extends JpaRepository<StudyPlanEntity, Long> {
    Optional<StudyPlanEntity> findByCode(String code);
    Optional<StudyPlanEntity> findByCareerCodeAndStatus(String careerCode, StudyPlanStatus status);
    List<StudyPlanEntity> findAllByCareerCode(String careerCode);
    Boolean existsByCareerCode(String careerCode);
}
