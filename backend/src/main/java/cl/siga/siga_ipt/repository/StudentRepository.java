package cl.siga.siga_ipt.repository;

import cl.siga.siga_ipt.entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<StudentEntity, Long> {
    Optional<StudentEntity> findByRun(String run);
    Optional<StudentEntity> findByEmail(String email);
    Boolean existsByRun(String run);
    Boolean existsByEmail(String email);
}