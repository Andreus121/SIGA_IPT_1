package cl.siga.siga_ipt.service;

import cl.siga.siga_ipt.config.SeedData;
import cl.siga.siga_ipt.entity.*;
import cl.siga.siga_ipt.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeedDataService {

    private static final long SEED_LOCK_KEY = 874_392_615L;

    private final JdbcTemplate jdbcTemplate;
    private final KeycloakAdminService keycloakAdminService;
    private final CareerRepository careerRepository;
    private final StudyPlanRepository studyPlanRepository;
    private final CourseRepository courseRepository;
    private final PrerequisiteRepository prerequisiteRepository;
    private final AcademicPeriodRepository academicPeriodRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final SectionRepository sectionRepository;
    private final SectionScheduleRepository sectionScheduleRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final RegistrationRepository registrationRepository;
    private final AcademicHistoryRepository academicHistoryRepository;

    @Transactional
    public void seedIfDatabaseIsEmpty(SeedData.DataSet data) {
        Objects.requireNonNull(data, "El conjunto de datos no puede ser null");

        if (!isPostgreSql()) {
            log.info("Seed omitido: el mecanismo de coordinación requiere PostgreSQL.");
            return;
        }

        Boolean lockAcquired = jdbcTemplate.queryForObject(
                "SELECT pg_try_advisory_xact_lock(?)",
                Boolean.class,
                SEED_LOCK_KEY
        );
        if (!Boolean.TRUE.equals(lockAcquired)) {
            log.info("Seed omitido: otra instancia está verificando o poblando la base de datos.");
            return;
        }

        if (!isDatabaseEmpty()) {
            log.info("Seed omitido: la base de datos ya contiene datos.");
            return;
        }

        log.info("Base de datos vacía; iniciando seed de datos de prueba.");
        seed(data);
    }

    @Transactional
    public void seed(SeedData.DataSet data) {
        Objects.requireNonNull(data, "El conjunto de datos no puede ser null");

        data.careers().forEach(this::saveCareer);
        data.studyPlans().forEach(this::saveStudyPlan);
        data.courses().forEach(this::saveCourse);
        data.prerequisites().forEach(this::savePrerequisite);
        data.academicPeriods().forEach(this::saveAcademicPeriod);
        data.teachers().forEach(this::saveTeacher);
        data.students().forEach(this::saveStudent);
        data.sections().forEach(this::saveSection);
        data.sectionSchedules().forEach(this::saveSectionSchedule);
        data.enrollments().forEach(this::saveEnrollment);
        data.registrations().forEach(this::saveRegistration);
        data.academicHistories().forEach(this::saveAcademicHistory);
    }

    private boolean isPostgreSql() {
        return Boolean.TRUE.equals(jdbcTemplate.execute((ConnectionCallback<Boolean>) connection ->
                "PostgreSQL".equalsIgnoreCase(connection.getMetaData().getDatabaseProductName())));
    }

    private boolean isDatabaseEmpty() {
        return careerRepository.count() == 0
                && studyPlanRepository.count() == 0
                && courseRepository.count() == 0
                && prerequisiteRepository.count() == 0
                && academicPeriodRepository.count() == 0
                && teacherRepository.count() == 0
                && studentRepository.count() == 0
                && sectionRepository.count() == 0
                && sectionScheduleRepository.count() == 0
                && enrollmentRepository.count() == 0
                && registrationRepository.count() == 0
                && academicHistoryRepository.count() == 0;
    }

    private void saveCareer(SeedData.CareerSeed seed) {
        if (!careerRepository.existsByCode(seed.code())) {
            careerRepository.save(CareerEntity.builder()
                    .code(seed.code()).name(seed.name()).description(seed.description()).status(seed.status()).build());
        }
    }

    private void saveStudyPlan(SeedData.StudyPlanSeed seed) {
        requireCareer(seed.careerCode());
        if (!studyPlanRepository.existsByCode(seed.code())) {
            studyPlanRepository.save(StudyPlanEntity.builder()
                    .code(seed.code()).careerCode(seed.careerCode()).name(seed.name()).status(seed.status()).build());
        }
    }

    private void saveCourse(SeedData.CourseSeed seed) {
        requireStudyPlan(seed.studyPlanCode());
        if (!courseRepository.existsByCodeAndStudyPlanCode(seed.code(), seed.studyPlanCode())) {
            courseRepository.save(CourseEntity.builder()
                    .code(seed.code()).studyPlanCode(seed.studyPlanCode()).name(seed.name())
                    .semester(seed.semester()).theoryHours(seed.theoryHours()).exerciseHours(seed.exerciseHours())
                    .labHours(seed.labHours()).sct(seed.sct()).build());
        }
    }

    private void savePrerequisite(SeedData.PrerequisiteSeed seed) {
        requireCourse(seed.courseCode(), seed.studyPlanCode());
        requireCourse(seed.prerequisiteCourseCode(), seed.studyPlanCode());
        boolean exists = prerequisiteRepository.findByCourseCodeAndStudyPlanCode(seed.courseCode(), seed.studyPlanCode())
                .stream().anyMatch(item -> item.getPrerequisiteCourseCode().equals(seed.prerequisiteCourseCode()));
        if (!exists) {
            prerequisiteRepository.save(PrerequisiteEntity.builder()
                    .studyPlanCode(seed.studyPlanCode()).courseCode(seed.courseCode())
                    .prerequisiteCourseCode(seed.prerequisiteCourseCode()).build());
        }
    }

    private void saveAcademicPeriod(SeedData.AcademicPeriodSeed seed) {
        if (!academicPeriodRepository.existsByPeriodCode(seed.periodCode())) {
            academicPeriodRepository.save(AcademicPeriodEntity.builder()
                    .periodCode(seed.periodCode()).academicPeriod(seed.academicPeriod())
                    .status(seed.status()).build());
        }
    }

    private void saveTeacher(SeedData.TeacherSeed seed) {
        if (!teacherRepository.existsByRun(seed.run())) {
            keycloakAdminService.createUser(seed.email(), seed.fullName(), seed.password(), "TEACHER");
            teacherRepository.save(TeacherEntity.builder()
                    .run(seed.run()).fullName(seed.fullName()).email(seed.email())
                    .professionalTitle(seed.professionalTitle()).academicDegree(seed.academicDegree())
                    .status(seed.status()).build());
        }
    }

    private void saveStudent(SeedData.StudentSeed seed) {
        requireCareer(seed.careerCode());
        requireStudyPlan(seed.studyPlanCode());
        if (!studentRepository.existsByRun(seed.run())) {
            keycloakAdminService.createUser(seed.email(), seed.fullName(), seed.password(), "STUDENT");
            studentRepository.save(StudentEntity.builder()
                    .run(seed.run()).fullName(seed.fullName()).email(seed.email())
                    .careerCode(seed.careerCode()).studyPlanCode(seed.studyPlanCode()).status(seed.status()).build());
        }
    }

    private void saveSection(SeedData.SectionSeed seed) {
        requirePeriod(seed.periodCode());
        requireCourseByCode(seed.courseCode());
        TeacherEntity teacher = teacherRepository.findByRun(seed.teacherRun())
                .orElseThrow(() -> new IllegalArgumentException("No existe el profesor: " + seed.teacherRun()));
        if (!sectionRepository.existsByCodeAndPeriodCode(seed.code(), seed.periodCode())) {
            sectionRepository.save(SectionEntity.builder()
                    .code(seed.code()).courseCode(seed.courseCode()).periodCode(seed.periodCode())
                    .teacherId(teacher.getId()).capacity(seed.capacity()).build());
        }
    }

    private void saveSectionSchedule(SeedData.SectionScheduleSeed seed) {
        requireSection(seed.sectionCode(), seed.periodCode());
        boolean exists = sectionScheduleRepository.findBySectionCodeAndPeriodCode(seed.sectionCode(), seed.periodCode())
                .stream().anyMatch(item -> item.getDay().equals(seed.day())
                        && item.getModuleNumber().equals(seed.moduleNumber()));
        if (!exists) {
            sectionScheduleRepository.save(SectionScheduleEntity.builder()
                    .sectionCode(seed.sectionCode()).periodCode(seed.periodCode())
                    .day(seed.day()).moduleNumber(seed.moduleNumber()).build());
        }
    }

    private void saveEnrollment(SeedData.EnrollmentSeed seed) {
        requireStudent(seed.studentRun());
        requirePeriod(seed.periodCode());
        if (!enrollmentRepository.existsByStudentRunAndPeriodCode(seed.studentRun(), seed.periodCode())) {
            enrollmentRepository.save(EnrollmentEntity.builder()
                    .studentRun(seed.studentRun()).periodCode(seed.periodCode())
                    .enrollmentDate(seed.enrollmentDate()).status(seed.status()).build());
        }
    }

    private void saveRegistration(SeedData.RegistrationSeed seed) {
        requireStudent(seed.studentRun());
        requirePeriod(seed.periodCode());
        requireSection(seed.sectionCode(), seed.periodCode());
        requireCourseByCode(seed.courseCode());
        if (!registrationRepository.existsByStudentRunAndCourseCodeAndPeriodCode(
                seed.studentRun(), seed.courseCode(), seed.periodCode())) {
            registrationRepository.save(RegistrationEntity.builder()
                    .studentRun(seed.studentRun()).courseCode(seed.courseCode()).sectionCode(seed.sectionCode())
                    .periodCode(seed.periodCode()).registrationDate(seed.registrationDate())
                    .finalGrade(seed.finalGrade()).status(seed.status()).build());
        }
    }

    private void saveAcademicHistory(SeedData.AcademicHistorySeed seed) {
        requireStudent(seed.studentRun());
        requireStudyPlan(seed.studyPlanCode());
        requirePeriod(seed.periodCode());
        requireSection(seed.sectionCode(), seed.periodCode());
        teacherRepository.findByRun(seed.teacherRun())
                .orElseThrow(() -> new IllegalArgumentException("No existe el profesor: " + seed.teacherRun()));
        boolean exists = academicHistoryRepository.findByStudentRun(seed.studentRun()).stream()
                .anyMatch(item -> item.getCourseCode().equals(seed.courseCode())
                        && item.getPeriodCode().equals(seed.periodCode()));
        if (!exists) {
            academicHistoryRepository.save(AcademicHistoryEntity.builder()
                    .studentRun(seed.studentRun()).courseCode(seed.courseCode()).studyPlanCode(seed.studyPlanCode())
                    .sectionCode(seed.sectionCode()).teacherRun(seed.teacherRun()).periodCode(seed.periodCode())
                    .finalGrade(seed.finalGrade()).status(seed.status()).build());
        }
    }

    private void requireCareer(String code) {
        if (!careerRepository.existsByCode(code)) {
            throw new IllegalArgumentException("No existe la carrera: " + code);
        }
    }

    private void requireStudyPlan(String code) {
        if (!studyPlanRepository.existsByCode(code)) {
            throw new IllegalArgumentException("No existe el plan de estudio: " + code);
        }
    }

    private void requireCourse(String code, String studyPlanCode) {
        if (!courseRepository.existsByCodeAndStudyPlanCode(code, studyPlanCode)) {
            throw new IllegalArgumentException("No existe la asignatura " + code + " en el plan " + studyPlanCode);
        }
    }

    private void requireCourseByCode(String code) {
        if (courseRepository.findFirstByCode(code).isEmpty()) {
            throw new IllegalArgumentException("No existe la asignatura: " + code);
        }
    }

    private void requirePeriod(String code) {
        if (!academicPeriodRepository.existsByPeriodCode(code)) {
            throw new IllegalArgumentException("No existe el periodo académico: " + code);
        }
    }

    private void requireStudent(String run) {
        if (!studentRepository.existsByRun(run)) {
            throw new IllegalArgumentException("No existe el alumno: " + run);
        }
    }

    private void requireSection(String code, String periodCode) {
        if (!sectionRepository.existsByCodeAndPeriodCode(code, periodCode)) {
            throw new IllegalArgumentException("No existe la sección " + code + " en el periodo " + periodCode);
        }
    }
}
