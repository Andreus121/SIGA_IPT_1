package cl.siga.siga_ipt.repository;

import cl.siga.siga_ipt.entity.AcademicHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AcademicHistoryRepository extends JpaRepository<AcademicHistoryEntity, Long> {
    Boolean existsByStudentRun(String studentRun);
    Boolean existsByTeacherCode(String teacherCode);
    List<AcademicHistoryEntity> findAllByStudentRun(String studentRun);
    List<AcademicHistoryEntity> findAllByStudentRunAndTeacherCode(String studentRun, String teacherCode);
}