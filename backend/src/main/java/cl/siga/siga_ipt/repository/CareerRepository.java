package cl.siga.siga_ipt.repository;

import cl.siga.siga_ipt.entity.CareerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CareerRepository extends JpaRepository<CareerEntity, Long> {
    Optional<CareerEntity> findByCode(String code);
    boolean existsByCode(String code);
}
