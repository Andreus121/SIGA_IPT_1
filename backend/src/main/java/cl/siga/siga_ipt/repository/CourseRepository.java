package cl.siga.siga_ipt.repository;

import cl.siga.siga_ipt.entity.CourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {
    public boolean existsByStudyPlanCode(String planCode);
}
