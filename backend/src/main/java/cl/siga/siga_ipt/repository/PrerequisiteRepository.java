package cl.siga.siga_ipt.repository;

import cl.siga.siga_ipt.entity.PrerequisiteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PrerequisiteRepository extends JpaRepository<PrerequisiteEntity, Long> {

    // Retorna la lista de entidades PrerequisiteEntity asociadas a una asignatura y un plan de estudio
    List<PrerequisiteEntity> findByCourseCodeAndStudyPlanCode(String courseCode, String studyPlanCode);
}
