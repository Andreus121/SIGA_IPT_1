package cl.siga.siga_ipt.service;

import cl.siga.siga_ipt.dto.RegistrationCreateDto;
import cl.siga.siga_ipt.dto.RegistrationResponseDto;
import cl.siga.siga_ipt.entity.*;
import cl.siga.siga_ipt.enums.*;
import cl.siga.siga_ipt.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final AcademicPeriodRepository periodRepository;
    private final SectionRepository sectionRepository;
    private final SectionScheduleRepository scheduleRepository;
    private final CourseRepository courseRepository;
    private final PrerequisiteRepository prerequisiteRepository;
    private final AcademicHistoryRepository academicHistoryRepository;

    // Hace la toma de un ramo de un alumno en un periodo
    @Transactional
    public RegistrationResponseDto registerCourse(RegistrationCreateDto dto) {
        // Periodo académico existente y ABIERTO
        AcademicPeriodEntity period = periodRepository.findByPeriodCode(dto.getPeriodCode())
                .orElseThrow(() -> new IllegalArgumentException("El período académico no existe: " + dto.getPeriodCode()));

        if (period.getStatus() != PeriodStatus.OPEN) {
            throw new IllegalStateException("Las inscripciones solo pueden realizarse en un período académico ABIERTO.");
        }

        // Estudiante existente y en estado REGULAR
        StudentEntity student = studentRepository.findByRun(dto.getStudentRun())
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado con RUN: " + dto.getStudentRun()));

        if (student.getStatus() != StudentStatus.REGULAR) {
            throw new IllegalStateException("El estudiante debe estar en estado REGULAR para inscribir asignaturas.");
        }

        // Estudiante matriculado en el periodo académico
        EnrollmentEntity enrollment = enrollmentRepository.findByStudentRunAndPeriodCode(dto.getStudentRun(), dto.getPeriodCode())
                .orElseThrow(() -> new IllegalStateException("El estudiante debe estar matriculado en el período académico para inscribir asignaturas."));

        if (enrollment.getStatus() != EnrollmentStatus.ACTIVE) {
            throw new IllegalStateException("La matrícula del estudiante no se encuentra activa en este período.");
        }

        // Sección existente en el periodo
        SectionEntity section = sectionRepository.findByCodeAndPeriodCode(dto.getSectionCode(), dto.getPeriodCode())
                .orElseThrow(() -> new IllegalArgumentException("La sección no existe con código: " + dto.getSectionCode()));

        if (!section.getPeriodCode().equals(dto.getPeriodCode())) {
            throw new IllegalArgumentException("La sección no corresponde al período académico indicado.");
        }

        // Asignatura pertenece al plan de estudios del estudiante
        CourseEntity course = courseRepository.findByCodeAndStudyPlanCode(section.getCourseCode(), student.getStudyPlanCode())
                .orElseThrow(() -> new IllegalArgumentException("La asignatura no pertenece al plan de estudios del estudiante."));

        // Ya inscrita en este periodo
        if (registrationRepository.existsByStudentRunAndCourseCodeAndPeriodCode(student.getRun(), course.getCode(), dto.getPeriodCode())) {
            throw new IllegalArgumentException("El estudiante ya tiene inscrita esta asignatura en el período actual.");
        }

        // No puede estar previamente aprobada en el historial académico
        if (academicHistoryRepository.existsByStudentRunAndCourseCodeAndStatus(student.getRun(), course.getCode(), AcademicStatus.PASSED)) {
            throw new IllegalStateException("La asignatura ya fue aprobada y no puede volver a inscribirse.");
        }

        // Validación de prerrequisitos aprobados
        List<PrerequisiteEntity> prerequisites = prerequisiteRepository.findByCourseCodeAndStudyPlanCode(course.getCode(), student.getStudyPlanCode());
        for (PrerequisiteEntity prereq : prerequisites) {
            boolean approved = academicHistoryRepository.existsByStudentRunAndCourseCodeAndStatus(
                    student.getRun(), prereq.getPrerequisiteCourseCode(), AcademicStatus.PASSED);
            if (!approved) {
                throw new IllegalStateException("No cumple con el prerrequisito aprobado: " + prereq.getPrerequisiteCourseCode());
            }
        }

        // Límite máximo de asignaturas por período (máximo 6)
        long currentRegistrationsCount = registrationRepository.countByStudentRunAndPeriodCode(student.getRun(), dto.getPeriodCode());
        if (currentRegistrationsCount >= 6) {
            throw new IllegalStateException("El estudiante ha alcanzado el límite máximo de 6 asignaturas para el período.");
        }

        // Función auxiliar
        // Verificar que pasó todos los ramos de semestres anteriores al ramo que se quiere tomar
        validateLowerSemesterPriority(student, course, dto.getPeriodCode());

        // Disponibilidad de cupos en la sección
        long registeredCount = registrationRepository.countBySectionCodeAndPeriodCode(section.getCode(), dto.getPeriodCode());
        if (registeredCount >= section.getCapacity()) {
            throw new IllegalStateException("La sección no tiene cupos disponibles (capacidad máxima: " + section.getCapacity() + ").");
        }

        // Función auxiliar
        // Control de tope de horario con otras asignaturas inscritas del estudiante en el periodo
        validateStudentScheduleConflicts(student.getRun(), section, dto.getPeriodCode());

        // Crear registro
        RegistrationEntity registration = RegistrationEntity.builder()
                .studentRun(student.getRun())
                .courseCode(course.getCode())
                .sectionCode(section.getCode())
                .periodCode(dto.getPeriodCode())
                .registrationDate(LocalDateTime.now())
                .status(RegistrationStatus.REGISTERED)
                .build();

        RegistrationEntity saved = registrationRepository.save(registration);
        return mapToDto(saved);
    }

    // Desinscribir un ramo
    @Transactional
    public void dropCourse(Long registrationId) {
        // Verificar que la toma de ramo si existe
        RegistrationEntity registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("Inscripción no encontrada con ID: " + registrationId));

        // Verificar que existe el periodo académico
        AcademicPeriodEntity period = periodRepository.findByPeriodCode(registration.getPeriodCode())
                .orElseThrow(() -> new IllegalArgumentException("Período no encontrado: " + registration.getPeriodCode()));

        // Si el periodo académico ya cerró, no se puede botar el ramo
        if (period.getStatus() != PeriodStatus.OPEN) {
            throw new IllegalStateException("Las anulaciones de asignaturas solo pueden realizarse mientras el período académico se encuentre ABIERTO.");
        }

        // Desinscribir ramo
        registrationRepository.delete(registration);
    }

    // Obtiene todas las tomas de ramo de un estudiante en un periodo
    @Transactional(readOnly = true)
    public List<RegistrationResponseDto> getRegistrationsByStudent(String run, String periodCode) {
        return registrationRepository.findByStudentRunAndPeriodCode(run, periodCode).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Obtiene todos los alumnos registrados en una sección
    @Transactional(readOnly = true)
    public List<RegistrationResponseDto> getRegistrationsBySection(String sectionCode, String periodCode) {
        return registrationRepository.findBySectionCodeAndPeriodCode(sectionCode, periodCode).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Verifica que el alumno no este adelantando un ramo de un semestre superior si aún no supera todos los ramos de los semestres anteriores al ramo que desea tomar
    private void validateLowerSemesterPriority(StudentEntity student, CourseEntity targetCourse, String periodCode) {
        // Obtener todos los ramos de un plan de estudio
        List<CourseEntity> planCourses = courseRepository.findByStudyPlanCode(student.getStudyPlanCode());

        // Por cada ramo del plan de estudio
        for (CourseEntity lowerCourse : planCourses) {
            // si ramo es menor que el ramo que se va a tomar
            if (lowerCourse.getSemester() < targetCourse.getSemester()) {
                // verificar que en el historial académico está aprobado
                boolean alreadyApproved = academicHistoryRepository.existsByStudentRunAndCourseCodeAndStatus(
                        student.getRun(), lowerCourse.getCode(), AcademicStatus.PASSED);

                // verificar que existe la toma del ramo
                boolean alreadyRegistered = registrationRepository.existsByStudentRunAndCourseCodeAndPeriodCode(
                        student.getRun(), lowerCourse.getCode(), periodCode);

                // si no está aprobada o no está inscrito al ramo
                if (!alreadyApproved && !alreadyRegistered) {
                    // Verificar si está ofrecida en este período
                    boolean isOffered = sectionRepository.existsByCourseCodeAndPeriodCode(lowerCourse.getCode(), periodCode);
                    if (isOffered) {
                        // Verificar si tiene sus prerrequisitos aprobados para saber si está disponible
                        boolean prereqsApproved = prerequisiteRepository.findByCourseCodeAndStudyPlanCode(lowerCourse.getCode(), student.getStudyPlanCode())
                                .stream()
                                .allMatch(p -> academicHistoryRepository.existsByStudentRunAndCourseCodeAndStatus(
                                        student.getRun(), p.getPrerequisiteCourseCode(), AcademicStatus.PASSED));

                        if (prereqsApproved) {
                            throw new IllegalStateException("No puede inscribir asignaturas del semestre " + targetCourse.getSemester() +
                                    " teniendo pendiente y disponible '" + lowerCourse.getCode() + "' del semestre " + lowerCourse.getSemester() + ".");
                        }
                    }
                }
            }
        }
    }

    // Verifica que una sección no haga tope de horario con las tomas ya echas de un estudiante en un periodo académico
    private void validateStudentScheduleConflicts(String studentRun, SectionEntity newSection, String periodCode) {
        List<SectionScheduleEntity> newSchedules = scheduleRepository.findBySectionCodeAndPeriodCode(newSection.getCode(), periodCode);
        List<RegistrationEntity> currentRegistrations = registrationRepository.findByStudentRunAndPeriodCode(studentRun, periodCode);

        for (RegistrationEntity reg : currentRegistrations) {
            List<SectionScheduleEntity> existingSchedules = scheduleRepository.findBySectionCodeAndPeriodCode(reg.getSectionCode(), periodCode);
            for (SectionScheduleEntity newSlot : newSchedules) {
                for (SectionScheduleEntity existingSlot : existingSchedules) {
                    if (newSlot.getDay().equalsIgnoreCase(existingSlot.getDay())
                            && newSlot.getModuleNumber().equals(existingSlot.getModuleNumber())) {
                        throw new IllegalStateException("Tope de horario detectado con la sección '" + reg.getSectionCode() +
                                "' en el día " + newSlot.getDay() + " y módulo M" + newSlot.getModuleNumber() + ".");
                    }
                }
            }
        }
    }

    // Función auxiliar
    private RegistrationResponseDto mapToDto(RegistrationEntity entity) {
        return RegistrationResponseDto.builder()
                .id(entity.getId())
                .studentRun(entity.getStudentRun())
                .courseCode(entity.getCourseCode())
                .sectionCode(entity.getSectionCode())
                .periodCode(entity.getPeriodCode())
                .registrationDate(entity.getRegistrationDate())
                .finalGrade(entity.getFinalGrade())
                .status(entity.getStatus())
                .build();
    }
}
