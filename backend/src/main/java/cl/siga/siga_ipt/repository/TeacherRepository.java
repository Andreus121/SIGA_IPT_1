package cl.siga.siga_ipt.repository;

import cl.siga.siga_ipt.entity.TeacherEntity;
import cl.siga.siga_ipt.enums.TeacherStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<TeacherEntity, Long> {
    boolean existsByRun(String run);
    boolean existsByEmail(String email);
    boolean existsByRunAndIdNot(String run, Long id);
    boolean existsByEmailAndIdNot(String email, Long id);
    List<TeacherEntity> findByStatus(TeacherStatus status);
    Optional<TeacherEntity> findByRun(String run);
}