package cl.siga.siga_ipt.service;

import cl.siga.siga_ipt.dto.SemesterClosingResponseDto;
import cl.siga.siga_ipt.entity.*;
import cl.siga.siga_ipt.enums.AcademicStatus;
import cl.siga.siga_ipt.enums.PeriodStatus;
import cl.siga.siga_ipt.enums.RegistrationStatus;
import cl.siga.siga_ipt.enums.StudentStatus;
import cl.siga.siga_ipt.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SemesterClosingService {

    private final AcademicPeriodRepository periodRepository;
    private final RegistrationRepository registrationRepository;
    private final SectionRepository sectionRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final AcademicHistoryRepository academicHistoryRepository;

    // Cierra el semestre dado
    @Transactional
    public SemesterClosingResponseDto closeSemester(String periodCode) {
        // Validar que el período exista y esté OPEN
        AcademicPeriodEntity period = periodRepository.findByPeriodCode(periodCode)
                .orElseThrow(() -> new IllegalArgumentException("El período académico '" + periodCode + "' no existe."));

        if (period.getStatus() != PeriodStatus.OPEN) {
            throw new IllegalStateException("El período académico '" + periodCode + "' no se encuentra ABIERTO para ejecutar el cierre.");
        }

        // Obtener todas las inscripciones del período
        List<RegistrationEntity> registrations = registrationRepository.findByPeriodCode(periodCode);

        // Validar que todas las inscripciones activas tengan nota final
        List<RegistrationEntity> activeRegistrations = registrations.stream()
                .filter(r -> r.getStatus() == RegistrationStatus.REGISTERED)
                .collect(Collectors.toList());

        boolean hasMissingGrades = activeRegistrations.stream()
                .anyMatch(r -> r.getFinalGrade() == null);

        if (hasMissingGrades) {
            throw new IllegalStateException("No se puede cerrar el semestre: existen asignaturas inscritas sin calificación final registrada.");
        }

        int processedRegistrations = 0;
        Set<String> affectedStudentRuns = new HashSet<>();

        // Consolidar en el historial académico
        for (RegistrationEntity reg : activeRegistrations) {
            affectedStudentRuns.add(reg.getStudentRun());

            SectionEntity section = sectionRepository.findByCode(reg.getSectionCode())
                    .orElse(null);

            String teacherRun = "N/A";
            if (section != null && section.getTeacherId() != null) {
                teacherRun = teacherRepository.findById(section.getTeacherId())
                        .map(TeacherEntity::getRun)
                        .orElse("N/A");
            }

            StudentEntity student = studentRepository.findByRun(reg.getStudentRun()).orElse(null);
            String studyPlanCode = (student != null) ? student.getStudyPlanCode() : "N/A";

            AcademicStatus status = reg.getFinalGrade() >= 4.0 ? AcademicStatus.PASSED : AcademicStatus.FAILED;

            AcademicHistoryEntity history = AcademicHistoryEntity.builder()
                    .studentRun(reg.getStudentRun())
                    .courseCode(reg.getCourseCode())
                    .studyPlanCode(studyPlanCode)
                    .sectionCode(reg.getSectionCode())
                    .teacherRun(teacherRun)
                    .periodCode(periodCode)
                    .finalGrade(reg.getFinalGrade())
                    .status(status)
                    .build();

            academicHistoryRepository.save(history);
            processedRegistrations++;
        }

        // contador para saber cuantos aprobaron y reprobaron
        int droppedCount = 0;
        int graduatedCount = 0;

        // Evaluar estado académico de cada estudiante afectado
        for (String studentRun : affectedStudentRuns) {
            Optional<StudentEntity> studentOpt = studentRepository.findByRun(studentRun);
            if (studentOpt.isEmpty()) continue;

            StudentEntity student = studentOpt.get();
            List<AcademicHistoryEntity> fullHistory = academicHistoryRepository.findByStudentRun(studentRun);

            // Regla de eliminación: si reprobó 2 veces la misma asignatura -> DROPPED
            Map<String, Long> failureCounts = fullHistory.stream()
                    .filter(h -> h.getStatus() == AcademicStatus.FAILED)
                    .collect(Collectors.groupingBy(AcademicHistoryEntity::getCourseCode, Collectors.counting()));

            boolean hasDoubleFailure = failureCounts.values().stream().anyMatch(count -> count >= 2);

            if (hasDoubleFailure) {
                student.setStatus(StudentStatus.DROPPED);
                studentRepository.save(student);
                droppedCount++;
                continue; // Si queda eliminado, no puede egresar
            }

            // Regla de egreso: si aprobó todas las asignaturas de su plan de estudios -> GRADUATED
            List<CourseEntity> planCourses = courseRepository.findByStudyPlanCode(student.getStudyPlanCode());
            Set<String> passedCourseCodes = fullHistory.stream()
                    .filter(h -> h.getStatus() == AcademicStatus.PASSED)
                    .map(AcademicHistoryEntity::getCourseCode)
                    .collect(Collectors.toSet());

            boolean allPassed = !planCourses.isEmpty() && planCourses.stream()
                    .allMatch(course -> passedCourseCodes.contains(course.getCode()));

            if (allPassed) {
                student.setStatus(StudentStatus.GRADUATED);
                studentRepository.save(student);
                graduatedCount++;
            }
        }

        // Cerrar período académico
        period.setStatus(PeriodStatus.CLOSED);
        periodRepository.save(period);

        return SemesterClosingResponseDto.builder()
                .periodCode(periodCode)
                .totalRegistrationsProcessed(processedRegistrations)
                .studentsGraduatedCount(graduatedCount)
                .studentsDroppedCount(droppedCount)
                .message("Cierre de semestre del período " + periodCode + " finalizado exitosamente.")
                .build();
    }
}
