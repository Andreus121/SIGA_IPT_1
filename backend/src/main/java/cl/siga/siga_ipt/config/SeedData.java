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
                                "10450",
                                "Técnico Analista Programador",
                                "Carrera de prueba 1",
                                CareerStatus.ACTIVE
                        ),
                        new CareerSeed(
                                "10451",
                                "Técnico en Análisis de Datos",
                                "Carrera de prueba 2",
                                CareerStatus.ACTIVE
                        ),
                        new CareerSeed(
                                "10452",
                                "Técnico en CIberseguridad",
                                "Carrera de prueba 3",
                                CareerStatus.ACTIVE
                        )
                ),
                // Planes de estudio
                List.of(
                        new StudyPlanSeed(
                                "2021.3",
                                "10450",
                                "Plan de Estudio 2021.3",
                                StudyPlanStatus.CURRENT
                        ),
                        new StudyPlanSeed(
                                "2022.3",
                                "10451",
                                "Plan de Estudio 2022.3",
                                StudyPlanStatus.CURRENT
                        ),
                        new StudyPlanSeed(
                                "2023.3",
                                "10452",
                                "Plan de Estudio 2023.3",
                                StudyPlanStatus.CURRENT
                        )
                ),
                // Asignaturas
                List.of(
                        // Asignaturas de primer y segundo semestre de 10450
                        new CourseSeed(
                                "TAP101",
                                "2021.3",
                                "Fundamentos de Programación",
                                1,
                                2,
                                2,
                                2,
                                6
                        ),
                        new CourseSeed(
                                "TAP102",
                                "2021.3",
                                "Matemática Aplicada",
                                1,
                                2,
                                2,
                                0,
                                5
                        ),
                        new CourseSeed(
                                "TAP103",
                                "2021.3",
                                "Fundamentos de Computación",
                                1,
                                2,
                                0,
                                2,
                                4
                        ),
                        new CourseSeed(
                                "TAP104",
                                "2021.3",
                                "Bases de Datos I",
                                1,
                                2,
                                0,
                                2,
                                5
                        ),
                        new CourseSeed(
                                "TAP105",
                                "2021.3",
                                "Comunicación Efectiva",
                                2,
                                2,
                                2,
                                0,
                                4
                        ),
                        new CourseSeed(
                                "TAP106",
                                "2021.3",
                                "Inglés Técnico I",
                                2,
                                2,
                                2,
                                0,
                                4
                        ),
                        new CourseSeed(
                                "TAP201",
                                "2021.3",
                                "Programación Orientada a Objetos",
                                2,
                                2,
                                2,
                                2,
                                6
                        ),
                        new CourseSeed(
                                "TAP202",
                                "2021.3",
                                "Estructura de Datos",
                                2,
                                2,
                                2,
                                2,
                                6
                        ),
                        new CourseSeed(
                                "TAP203",
                                "2021.3",
                                "Bases de Datos II",
                                2,
                                2,
                                0,
                                2,
                                5
                        ),
                        new CourseSeed(
                                "TAP204",
                                "2021.3",
                                "Desarrollo Web I",
                                2,
                                2,
                                0,
                                4,
                                6
                        ),
                        new CourseSeed(
                                "TAP205",
                                "2021.3",
                                "Sistemas Operativos",
                                2,
                                2,
                                0,
                                2,
                                4
                        ),
                        new CourseSeed(
                                "TAP206",
                                "2021.3",
                                "Inglés Técnico II",
                                2,
                                2,
                                2,
                                0,
                                3
                        ),
                        // Asignaturas de primer y segundo semestre de 10451
                        new CourseSeed(
                                "TAD101",
                                "2022.3",
                                "Fundamentos de Programación",
                                1,
                                2,
                                2,
                                2,
                                6
                        ),
                        new CourseSeed(
                                "TAD102",
                                "2022.3",
                                "Matemática Aplicada",
                                1,
                                2,
                                2,
                                0,
                                5
                        ),
                        new CourseSeed(
                                "TAD103",
                                "2022.3",
                                "Introducción al Análisis de Datos",
                                1,
                                2,
                                0,
                                2,
                                5
                        ),
                        new CourseSeed(
                                "TAD104",
                                "2022.3",
                                "Bases de Datos I",
                                1,
                                2,
                                0,
                                2,
                                5
                        ),
                        new CourseSeed(
                                "TAD105",
                                "2022.3",
                                "Herramientas Computacionales",
                                1,
                                0,
                                0,
                                4,
                                4
                        ),
                        new CourseSeed(
                                "TAD106",
                                "2022.3",
                                "Comunicación Efectiva",
                                1,
                                2,
                                2,
                                0,
                                4
                        ),
                        new CourseSeed(
                                "TAD201",
                                "2022.3",
                                "Programación para Análisis de Datos",
                                2,
                                2,
                                0,
                                4,
                                6
                        ),
                        new CourseSeed(
                                "TAD202",
                                "2022.3",
                                "Probabilidad y Estadística",
                                2,
                                2,
                                2,
                                2,
                                6
                        ),
                        new CourseSeed(
                                "TAD203",
                                "2022.3",
                                "Bases de Datos II",
                                2,
                                2,
                                0,
                                2,
                                5
                        ),
                        new CourseSeed(
                                "TAD204",
                                "2022.3",
                                "Preparación y Calidad de Datos",
                                2,
                                2,
                                0,
                                4,
                                5
                        ),
                        new CourseSeed(
                                "TAD205",
                                "2022.3",
                                "Visualización de Datos",
                                2,
                                2,
                                0,
                                2,
                                5
                        ),
                        new CourseSeed(
                                "TAD206",
                                "2022.3",
                                "Inglés Técnico",
                                2,
                                2,
                                2,
                                0,
                                3
                        ),
                        // Asignaturas de primer y segundo semestre de 10452
                        new CourseSeed(
                                "TCS101",
                                "2023.3",
                                "Fundamentos de Programación",
                                1,
                                2,
                                2,
                                2,
                                6
                        ),
                        new CourseSeed(
                                "TCS102",
                                "2023.3",
                                "Fundamentos de Sistemas Computacionales",
                                1,
                                2,
                                0,
                                2,
                                5
                        ),
                        new CourseSeed(
                                "TCS103",
                                "2023.3",
                                "Redes de Computadores I",
                                1,
                                2,
                                0,
                                2,
                                5
                        ),
                        new CourseSeed(
                                "TCS104",
                                "2023.3",
                                "Sistemas Operativos",
                                1,
                                2,
                                0,
                                2,
                                5
                        ),
                        new CourseSeed(
                                "TCS105",
                                "2023.3",
                                "Matemática Aplicada",
                                1,
                                2,
                                2,
                                0,
                                5
                        ),
                        new CourseSeed(
                                "TCS106",
                                "2023.3",
                                "Comunicación Efectiva",
                                1,
                                2,
                                2,
                                0,
                                4
                        ),
                        new CourseSeed(
                                "TCS201",
                                "2023.3",
                                "Programación para Ciberseguridad",
                                2,
                                2,
                                0,
                                4,
                                5
                        ),
                        new CourseSeed(
                                "TCS202",
                                "2023.3",
                                "Redes de Computadores II",
                                2,
                                2,
                                0,
                                4,
                                6
                        ),
                        new CourseSeed(
                                "TCS203",
                                "2023.3",
                                "Administración de Sistemas Linux",
                                2,
                                2,
                                0,
                                4,
                                5
                        ),
                        new CourseSeed(
                                "TCS204",
                                "2023.3",
                                "Fundamentos de Ciberseguridad",
                                2,
                                2,
                                0,
                                4,
                                6
                        ),
                        new CourseSeed(
                                "TCS205",
                                "2023.3",
                                "Bases de Datos",
                                2,
                                2,
                                0,
                                2,
                                5
                        ),
                        new CourseSeed(
                                "TCS206",
                                "2023.3",
                                "Inglés Técnico",
                                2,
                                2,
                                2,
                                0,
                                3
                        )
                ),
                // Prerrequisitos
                List.of(
                        // Prerrequisitos de asignaturas de 10450
                        new PrerequisiteSeed(
                            "2021.3",
                            "TAP201",
                            "TAP101"
                        ),
                        new PrerequisiteSeed(
                            "2021.3",
                            "TAP202",
                            "TAP101"
                        ),
                        new PrerequisiteSeed(
                            "2021.3",
                            "TAP203",
                            "TAP104"
                        ),
                        new PrerequisiteSeed(
                            "2021.3",
                            "TAP204",
                            "TAP101"
                        ),
                        new PrerequisiteSeed(
                            "2021.3",
                            "TAP205",
                            "TAP103"
                        ),
                        new PrerequisiteSeed(
                            "2021.3",
                            "TAP206",
                            "TAP106"
                        ),
                        // Prerrequisitos de asignaturas de 10451
                        new PrerequisiteSeed(
                            "2022.3",
                            "TAD201",
                            "TAD101"
                        ),
                        new PrerequisiteSeed(
                            "2022.3",
                            "TAD202",
                            "TAD102"
                        ),
                        new PrerequisiteSeed(
                            "2022.3",
                            "TAD203",
                            "TAD104"
                        ),
                        new PrerequisiteSeed(
                            "2022.3",
                            "TAD204",
                            "TAD103"
                        ),
                        new PrerequisiteSeed(
                            "2022.3",
                            "TAD204",
                            "TAD101"
                        ),
                        new PrerequisiteSeed(
                            "2022.3",
                            "TAD205",
                            "TAD103"
                        ),
                        // Prerrequisitos de asignaturas de 10452
                        new PrerequisiteSeed(
                            "2023.3",
                            "TCS201",
                            "TCS101"
                        ),
                        new PrerequisiteSeed(
                            "2023.3",
                            "TCS202",
                            "TCS103"
                        ),
                        new PrerequisiteSeed(
                            "2023.3",
                            "TCS203",
                            "TCS104"
                        ),
                        new PrerequisiteSeed(
                            "2023.3",
                            "TCS204",
                            "TCS102"
                        ),
                        new PrerequisiteSeed(
                            "2023.3",
                            "TCS204",
                            "TCS103"
                        ),
                        new PrerequisiteSeed(
                            "2023.3",
                            "TCS205",
                            "TCS101"
                        )
                ),
                // Periodos académicos
                List.of(
                        new AcademicPeriodSeed(
                                "2026-2",
                                "2026-2",
                                PeriodStatus.CLOSED
                        ),
                        new AcademicPeriodSeed(
                                "2027-1",
                                "2027-1",
                                PeriodStatus.OPEN
                        )
                ),
                // Profesores
                List.of(
                        new TeacherSeed(
                                "12.345.678-9",
                                "docente1",
                                "docente1@siga.cl",
                                "Ingeniera Informática",
                                "Magíster en Computación",
                                TeacherStatus.ACTIVE,
                                "123456"
                        )
                ),
                // Alumnos
                List.of(
                        new StudentSeed(
                                "21.345.678-9",
                                "estudiante1",
                                "estudiante1@siga.cl",
                                "10450",
                                "2021.3",
                                StudentStatus.REGULAR,
                                "123456"
                        ),
                        new StudentSeed(
                                "22.345.678-9",
                                "estudiante2",
                                "estudiante2@siga.cl",
                                "10450",
                                "2021.3",
                                StudentStatus.REGULAR,
                                "123456"
                        )
                ),
                // Secciones
                List.of(
                        new SectionSeed(
                                "TAP101-A",
                                "TAP101",
                                "2026-2",
                                "12.345.678-9",
                                30
                        ),
                        new SectionSeed(
                                "TAP102-A",
                                "TAP102",
                                "2026-2",
                                "12.345.678-9",
                                30
                        ),
                        new SectionSeed(
                                "TAP103-A",
                                "TAP103",
                                "2026-2",
                                "12.345.678-9",
                                30
                        ),
                        new SectionSeed(
                                "TAP104-A",
                                "TAP104",
                                "2026-2",
                                "12.345.678-9",
                                30
                        ),
                        new SectionSeed(
                                "TAP105-A",
                                "TAP105",
                                "2026-2",
                                "12.345.678-9",
                                30
                        ),
                        new SectionSeed(
                                "TAP106-A",
                                "TAP106",
                                "2026-2",
                                "12.345.678-9",
                                30
                        ),
                        new SectionSeed(
                                "TAP201-A",
                                "TAP201",
                                "2026-2",
                                "12.345.678-9",
                                30
                        ),
                        new SectionSeed(
                                "TAP202-A",
                                "TAP202",
                                "2026-2",
                                "12.345.678-9",
                                30
                        ),
                        new SectionSeed(
                                "TAP203-A",
                                "TAP203",
                                "2026-2",
                                "12.345.678-9",
                                30
                        ),
                        new SectionSeed(
                                "TAP204-A",
                                "TAP204",
                                "2026-2",
                                "12.345.678-9",
                                30
                        ),
                        new SectionSeed(
                                "TAP205-A",
                                "TAP205",
                                "2026-2",
                                "12.345.678-9",
                                30
                        ),
                        new SectionSeed(
                                "TAP206-A",
                                "TAP206",
                                "2026-2",
                                "12.345.678-9",
                                30
                        ),
                        new SectionSeed(
                                "TAP101-A",
                                "TAP101",
                                "2027-1",
                                "12.345.678-9",
                                30
                        ),
                        new SectionSeed(
                                "TAP102-A",
                                "TAP102",
                                "2027-1",
                                "12.345.678-9",
                                30
                        ),
                        new SectionSeed(
                                "TAP103-A",
                                "TAP103",
                                "2027-1",
                                "12.345.678-9",
                                30
                        ),
                        new SectionSeed(
                                "TAP104-A",
                                "TAP104",
                                "2027-1",
                                "12.345.678-9",
                                30
                        ),
                        new SectionSeed(
                                "TAP105-A",
                                "TAP105",
                                "2027-1",
                                "12.345.678-9",
                                30
                        ),
                        new SectionSeed(
                                "TAP106-A",
                                "TAP106",
                                "2027-1",
                                "12.345.678-9",
                                30
                        ),
                        new SectionSeed(
                                "TAP201-A",
                                "TAP201",
                                "2027-1",
                                "12.345.678-9",
                                30
                        ),
                        new SectionSeed(
                                "TAP202-A",
                                "TAP202",
                                "2027-1",
                                "12.345.678-9",
                                30
                        ),
                        new SectionSeed(
                                "TAP203-A",
                                "TAP203",
                                "2027-1",
                                "12.345.678-9",
                                30
                        ),
                        new SectionSeed(
                                "TAP204-A",
                                "TAP204",
                                "2027-1",
                                "12.345.678-9",
                                30
                        ),
                        new SectionSeed(
                                "TAP205-A",
                                "TAP205",
                                "2027-1",
                                "12.345.678-9",
                                30
                        ),
                        new SectionSeed(
                                "TAP206-A",
                                "TAP206",
                                "2027-1",
                                "12.345.678-9",
                                30
                        )
                ),
                // Horarios de secciones
                List.of(
                        new SectionScheduleSeed(
                                "TAP101-A",
                                "2026-2",
                                "L",
                                1
                        ),
                        new SectionScheduleSeed(
                                "TAP102-A",
                                "2026-2",
                                "M",
                                2
                        ),
                        new SectionScheduleSeed("TAP103-A", "2026-2", "W", 1),
                        new SectionScheduleSeed("TAP104-A", "2026-2", "J", 2),
                        new SectionScheduleSeed("TAP105-A", "2026-2", "V", 1),
                        new SectionScheduleSeed("TAP106-A", "2026-2", "L", 3),
                        new SectionScheduleSeed("TAP201-A", "2026-2", "M", 4),
                        new SectionScheduleSeed("TAP202-A", "2026-2", "W", 4),
                        new SectionScheduleSeed("TAP203-A", "2026-2", "J", 4),
                        new SectionScheduleSeed("TAP204-A", "2026-2", "V", 4),
                        new SectionScheduleSeed("TAP205-A", "2026-2", "L", 5),
                        new SectionScheduleSeed("TAP206-A", "2026-2", "M", 5),
                        new SectionScheduleSeed("TAP101-A", "2027-1", "L", 1),
                        new SectionScheduleSeed("TAP102-A", "2027-1", "M", 2),
                        new SectionScheduleSeed("TAP103-A", "2027-1", "W", 1),
                        new SectionScheduleSeed("TAP104-A", "2027-1", "J", 2),
                        new SectionScheduleSeed("TAP105-A", "2027-1", "V", 1),
                        new SectionScheduleSeed("TAP106-A", "2027-1", "L", 3),
                        new SectionScheduleSeed("TAP201-A", "2027-1", "M", 4),
                        new SectionScheduleSeed("TAP202-A", "2027-1", "W", 4),
                        new SectionScheduleSeed("TAP203-A", "2027-1", "J", 4),
                        new SectionScheduleSeed("TAP204-A", "2027-1", "V", 4),
                        new SectionScheduleSeed("TAP205-A", "2027-1", "L", 5),
                        new SectionScheduleSeed("TAP206-A", "2027-1", "M", 5)
                ),
                // Matrículas académicas
                List.of(
                        new EnrollmentSeed(
                                "21.345.678-9",
                                "2026-2",
                                LocalDate.of(2026, 8, 1),
                                EnrollmentStatus.ACTIVE
                        ),
                        new EnrollmentSeed(
                                "22.345.678-9",
                                "2026-2",
                                LocalDate.of(2026, 8, 1),
                                EnrollmentStatus.ACTIVE
                        ),
                        new EnrollmentSeed(
                                "21.345.678-9",
                                "2027-1",
                                LocalDate.of(2027, 3, 1),
                                EnrollmentStatus.ACTIVE
                        ),
                        new EnrollmentSeed(
                                "22.345.678-9",
                                "2027-1",
                                LocalDate.of(2027, 3, 1),
                                EnrollmentStatus.ACTIVE
                        )
                ),
                // Inscripciones de asignaturas
                List.of(
                        new RegistrationSeed(
                                "21.345.678-9",
                                "TAP101",
                                "TAP101-A",
                                "2026-2",
                                LocalDateTime.of(2026, 8, 2, 9, 0),
                                3.5,
                                RegistrationStatus.REGISTERED
                        ),
                        new RegistrationSeed(
                                "21.345.678-9",
                                "TAP102",
                                "TAP102-A",
                                "2026-2",
                                LocalDateTime.of(2026, 8, 2, 9, 5),
                                5.0,
                                RegistrationStatus.REGISTERED
                        ),
                        new RegistrationSeed(
                                "21.345.678-9",
                                "TAP103",
                                "TAP103-A",
                                "2026-2",
                                LocalDateTime.of(2026, 8, 2, 9, 10),
                                6.0,
                                RegistrationStatus.REGISTERED
                        ),
                        new RegistrationSeed(
                                "22.345.678-9",
                                "TAP101",
                                "TAP101-A",
                                "2026-2",
                                LocalDateTime.of(2026, 8, 2, 9, 15),
                                5.5,
                                RegistrationStatus.REGISTERED
                        ),
                        new RegistrationSeed(
                                "22.345.678-9",
                                "TAP102",
                                "TAP102-A",
                                "2026-2",
                                LocalDateTime.of(2026, 8, 2, 9, 20),
                                5.0,
                                RegistrationStatus.REGISTERED
                        ),
                        new RegistrationSeed(
                                "22.345.678-9",
                                "TAP103",
                                "TAP103-A",
                                "2026-2",
                                LocalDateTime.of(2026, 8, 2, 9, 25),
                                6.0,
                                RegistrationStatus.REGISTERED
                        ),
                        new RegistrationSeed(
                                "22.345.678-9",
                                "TAP104",
                                "TAP104-A",
                                "2026-2",
                                LocalDateTime.of(2026, 8, 2, 9, 30),
                                5.0,
                                RegistrationStatus.REGISTERED
                        ),
                        new RegistrationSeed(
                                "22.345.678-9",
                                "TAP105",
                                "TAP105-A",
                                "2026-2",
                                LocalDateTime.of(2026, 8, 2, 9, 35),
                                5.5,
                                RegistrationStatus.REGISTERED
                        ),
                        new RegistrationSeed(
                                "22.345.678-9",
                                "TAP106",
                                "TAP106-A",
                                "2026-2",
                                LocalDateTime.of(2026, 8, 2, 9, 40),
                                5.0,
                                RegistrationStatus.REGISTERED
                        ),
                        new RegistrationSeed(
                                "22.345.678-9",
                                "TAP201",
                                "TAP201-A",
                                "2027-1",
                                LocalDateTime.of(2027, 3, 2, 9, 0),
                                null,
                                RegistrationStatus.REGISTERED
                        ),
                        new RegistrationSeed(
                                "22.345.678-9",
                                "TAP202",
                                "TAP202-A",
                                "2027-1",
                                LocalDateTime.of(2027, 3, 2, 9, 5),
                                null,
                                RegistrationStatus.REGISTERED
                        ),
                        new RegistrationSeed(
                                "22.345.678-9",
                                "TAP203",
                                "TAP203-A",
                                "2027-1",
                                LocalDateTime.of(2027, 3, 2, 9, 10),
                                null,
                                RegistrationStatus.REGISTERED
                        ),
                        new RegistrationSeed(
                                "22.345.678-9",
                                "TAP204",
                                "TAP204-A",
                                "2027-1",
                                LocalDateTime.of(2027, 3, 2, 9, 15),
                                null,
                                RegistrationStatus.REGISTERED
                        ),
                        new RegistrationSeed(
                                "22.345.678-9",
                                "TAP205",
                                "TAP205-A",
                                "2027-1",
                                LocalDateTime.of(2027, 3, 2, 9, 20),
                                null,
                                RegistrationStatus.REGISTERED
                        ),
                        new RegistrationSeed(
                                "22.345.678-9",
                                "TAP206",
                                "TAP206-A",
                                "2027-1",
                                LocalDateTime.of(2027, 3, 2, 9, 25),
                                null,
                                RegistrationStatus.REGISTERED
                        )
                ),
                // Historial académico
                List.of(
                        new AcademicHistorySeed(
                                "21.345.678-9",
                                "TAP101",
                                "2021.3",
                                "TAP101-A",
                                "12.345.678-9",
                                "2026-2",
                                3.5,
                                AcademicStatus.FAILED
                        ),
                        new AcademicHistorySeed(
                                "21.345.678-9",
                                "TAP102",
                                "2021.3",
                                "TAP102-A",
                                "12.345.678-9",
                                "2026-2",
                                5.0,
                                AcademicStatus.PASSED
                        ),
                        new AcademicHistorySeed(
                                "21.345.678-9",
                                "TAP103",
                                "2021.3",
                                "TAP103-A",
                                "12.345.678-9",
                                "2026-2",
                                6.0,
                                AcademicStatus.PASSED
                        ),
                        new AcademicHistorySeed(
                                "22.345.678-9",
                                "TAP101",
                                "2021.3",
                                "TAP101-A",
                                "12.345.678-9",
                                "2026-2",
                                5.5,
                                AcademicStatus.PASSED
                        ),
                        new AcademicHistorySeed(
                                "22.345.678-9",
                                "TAP102",
                                "2021.3",
                                "TAP102-A",
                                "12.345.678-9",
                                "2026-2",
                                5.0,
                                AcademicStatus.PASSED
                        ),
                        new AcademicHistorySeed(
                                "22.345.678-9",
                                "TAP103",
                                "2021.3",
                                "TAP103-A",
                                "12.345.678-9",
                                "2026-2",
                                6.0,
                                AcademicStatus.PASSED
                        ),
                        new AcademicHistorySeed(
                                "22.345.678-9",
                                "TAP104",
                                "2021.3",
                                "TAP104-A",
                                "12.345.678-9",
                                "2026-2",
                                5.0,
                                AcademicStatus.PASSED
                        ),
                        new AcademicHistorySeed(
                                "22.345.678-9",
                                "TAP105",
                                "2021.3",
                                "TAP105-A",
                                "12.345.678-9",
                                "2026-2",
                                5.5,
                                AcademicStatus.PASSED
                        ),
                        new AcademicHistorySeed(
                                "22.345.678-9",
                                "TAP106",
                                "2021.3",
                                "TAP106-A",
                                "12.345.678-9",
                                "2026-2",
                                5.0,
                                AcademicStatus.PASSED
                        )
                )
        );
    }

}
