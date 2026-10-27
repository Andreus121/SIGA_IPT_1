package cl.siga.siga_ipt.repository;

import cl.siga.siga_ipt.entity.RegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegistrationRepository extends JpaRepository<RegistrationEntity, Long> {
    boolean existsByStudentRun(String studentRun);
}
