package cl.siga.siga_ipt.config;

import cl.siga.siga_ipt.enums.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class SeedData {

    public record DataSet(
            List<CareerSeed> careers,
            List<StudyPlanSeed> studyPlans,
            List<CourseSeed> courses,
            List<PrerequisiteSeed> prerequisites,
            List<AcademicPeriodSeed> academicPeriods,
            List<TeacherSeed> teachers,
            List<StudentSeed> students,
            List<SectionSeed> sections,
            List<SectionScheduleSeed> sectionSchedules,
            List<EnrollmentSeed> enrollments,
            List<RegistrationSeed> registrations,
            List<AcademicHistorySeed> academicHistories
    ) {}

    public record CareerSeed(String code, String name, String description, CareerStatus status) {}

    public record StudyPlanSeed(String code, String careerCode, String name, StudyPlanStatus status) {}

    public record CourseSeed(
            String code,
            String studyPlanCode,
            String name,
            Integer semester,
            Integer theoryHours,
            Integer exerciseHours,
            Integer labHours,
            Integer sct
    ) {}

    public record PrerequisiteSeed(String studyPlanCode, String courseCode, String prerequisiteCourseCode) {}

    public record AcademicPeriodSeed(String periodCode, String academicPeriod, PeriodStatus status) {}

    public record TeacherSeed(
            String run,
            String fullName,
            String email,
            String professionalTitle,
            String academicDegree,
            TeacherStatus status,
            String password
    ) {}

    public record StudentSeed(
            String run,
            String fullName,
            String email,
            String careerCode,
            String studyPlanCode,
            StudentStatus status,
            String password
    ) {}

    public record SectionSeed(
            String code,
            String courseCode,
            String periodCode,
            String teacherRun,
            Integer capacity
    ) {}

    public record SectionScheduleSeed(
            String sectionCode,
            String periodCode,
            String day,
            Integer moduleNumber
    ) {}

    public record EnrollmentSeed(
            String studentRun,
            String periodCode,
            LocalDate enrollmentDate,
            EnrollmentStatus status
    ) {}

    public record RegistrationSeed(
            String studentRun,
            String courseCode,
            String sectionCode,
            String periodCode,
            LocalDateTime registrationDate,
            Double finalGrade,
            RegistrationStatus status
    ) {}

    public record AcademicHistorySeed(
            String studentRun,
            String courseCode,
            String studyPlanCode,
            String sectionCode,
            String teacherRun,
            String periodCode,
            Double finalGrade,
            AcademicStatus status
    ) {}

    /**
     * Reemplaza estos ejemplos por tus datos de prueba.
     * Mantén las referencias: los códigos usados deben existir en las listas correspondientes.
     */
    public static DataSet data() {
        return new DataSet(
                // Carreras
                List.of(
                        new CareerSeed(
                                "INF",
                                "Ingeniería Informática",
                                "Carrera de prueba",
                                CareerStatus.ACTIVE
                        )
                ),
                // Planes de estudio
                List.of(
                        new StudyPlanSeed(
                                "INF-2025",
                                "INF",
                                "Plan de Ingeniería Informática 2025",
                                StudyPlanStatus.CURRENT
                        )
                ),
                // Asignaturas
                List.of(
                        new CourseSeed(
                                "INF101",
                                "INF-2025",
                                "Programación I",
                                1,
                                2,
                                2,
                                2,
                                6
                        ),
                        new CourseSeed(
                                "INF102",
                                "INF-2025",
                                "Programación II",
                                2,
                                2,
                                2,
                                2,
                                6
                        )
                ),
                // Prerrequisitos
                List.of(
                        new PrerequisiteSeed("INF-2025", "INF102", "INF101")
                ),
                // Periodos académicos
                List.of(
                        new AcademicPeriodSeed(
                                "2026-2",
                                "2026-2",
                                PeriodStatus.OPEN
                        )
                ),
                // Profesores
                List.of(
                        new TeacherSeed(
                                "12.345.678-9",
                                "Ana Profesora Ejemplo",
                                "ana.ejemplo@siga.cl",
                                "Ingeniera Informática",
                                "Magíster en Computación",
                                TeacherStatus.ACTIVE,
                                "PasswordTemporal123!"
                        )
                ),
                // Alumnos
                List.of(
                        new StudentSeed(
                                "21.345.678-9",
                                "Juan Alumno Ejemplo",
                                "juan.ejemplo@siga.cl",
                                "INF",
                                "INF-2025",
                                StudentStatus.REGULAR,
                                "PasswordTemporal123!"
                        )
                ),
                // Secciones
                List.of(
                        new SectionSeed(
                                "INF101-A",
                                "INF101",
                                "2026-2",
                                "12.345.678-9",
                                 30
                        ),
                        new SectionSeed(
                                 "INF102-A",
                                 "INF102",
                                 "2026-2",
                                 "12.345.678-9",
                                 30
                        )
                ),
                // Horarios de secciones
                List.of(
                        new SectionScheduleSeed(
                                "INF101-A",
                                "2026-2",
                                "L",
                                1
                        ),
                        new SectionScheduleSeed(
                                "INF102-A",
                                "2026-2",
                                "M",
                                2
                        )
                ),
                // Matrículas académicas
                List.of(
                        new EnrollmentSeed(
                                "21.345.678-9",
                                "2026-2",
                                LocalDate.of(2026, 8, 1),
                                EnrollmentStatus.ACTIVE
                        )
                ),
                // Inscripciones de asignaturas
                List.of(
                        new RegistrationSeed(
                                "21.345.678-9",
                                "INF101",
                                "INF101-A",
                                "2026-2",
                                LocalDateTime.of(2026, 8, 2, 10, 0),
                                null,
                                RegistrationStatus.REGISTERED
                        )
                ),
                // Historial académico
                List.of(
                        new AcademicHistorySeed(
                                "21.345.678-9",
                                "INF101",
                                "INF-2025",
                                "INF101-A",
                                "12.345.678-9",
                                "2026-2",
                                6.5,
                                AcademicStatus.PASSED
                        )
                )
        );
    }

}
