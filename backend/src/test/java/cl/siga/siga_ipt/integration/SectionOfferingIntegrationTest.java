package cl.siga.siga_ipt.integration;

import cl.siga.siga_ipt.dto.SectionCreateDto;
import cl.siga.siga_ipt.dto.SectionScheduleDto;
import cl.siga.siga_ipt.dto.SectionUpdateDto;
import cl.siga.siga_ipt.entity.*;
import cl.siga.siga_ipt.enums.*;
import cl.siga.siga_ipt.repository.*;
import cl.siga.siga_ipt.service.SectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SectionOfferingIntegrationTest {

    @Autowired
    private SectionService sectionService;

    @Autowired
    private AcademicPeriodRepository periodRepository;

    @Autowired
    private CareerRepository careerRepository;

    @Autowired
    private StudyPlanRepository studyPlanRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private SectionScheduleRepository scheduleRepository;

    private AcademicPeriodEntity openPeriod;
    private AcademicPeriodEntity closedPeriod;
    private TeacherEntity activeTeacher;
    private TeacherEntity inactiveTeacher;
    private CourseEntity testCourse;

    @BeforeEach
    void setUp() {
        scheduleRepository.deleteAll();
        sectionRepository.deleteAll();
        courseRepository.deleteAll();
        studyPlanRepository.deleteAll();
        careerRepository.deleteAll();
        teacherRepository.deleteAll();
        periodRepository.deleteAll();

        // 1. Períodos
        openPeriod = periodRepository.save(AcademicPeriodEntity.builder()
                .periodCode("2027-1")
                .academicPeriod("2027-1")
                .status(PeriodStatus.OPEN)
                .build());

        closedPeriod = periodRepository.save(AcademicPeriodEntity.builder()
                .periodCode("2026-2")
                .academicPeriod("2026-2")
                .status(PeriodStatus.CLOSED)
                .build());

        // 2. Docentes
        activeTeacher = teacherRepository.save(TeacherEntity.builder()
                .run("11111111-1")
                .fullName("Profesor Activo")
                .email("activo@ipt.cl")
                .professionalTitle("Ingeniero de Software")
                .academicDegree("Magíster")
                .status(TeacherStatus.ACTIVE)
                .build());

        inactiveTeacher = teacherRepository.save(TeacherEntity.builder()
                .run("22222222-2")
                .fullName("Profesor Inactivo")
                .email("inactivo@ipt.cl")
                .professionalTitle("Licenciado en Computación")
                .academicDegree("Doctor")
                .status(TeacherStatus.INACTIVE)
                .build());

        // 3. Carrera, Plan y Asignatura (TEL = 2 theory + 0 exercise + 0 lab = 2 horas = 1 bloque horario)
        careerRepository.save(CareerEntity.builder()
                .code("TAP")
                .name("Tecnico Analista Programador")
                .description("Carrera tecnica")
                .status(CareerStatus.ACTIVE)
                .build());

        studyPlanRepository.save(StudyPlanEntity.builder()
                .code("TAP-2024")
                .careerCode("TAP")
                .name("Plan 2024")
                .status(StudyPlanStatus.CURRENT)
                .build());

        testCourse = courseRepository.save(CourseEntity.builder()
                .code("PROG1")
                .studyPlanCode("TAP-2024")
                .name("Programacion 1")
                .semester(1)
                .sct(4)
                .theoryHours(2)
                .exerciseHours(0)
                .labHours(0)
                .build());
    }

    // ==========================================
    // ESCENARIOS EXITOSOS (3)
    // ==========================================

    @Test
    @DisplayName("Éxito 1: Crear sección con docente activo, horario válido y período abierto")
    void testCreateSection_Success() throws Exception {
        SectionCreateDto dto = SectionCreateDto.builder()
                .code("SEC-PROG1-2027-1")
                .courseCode("PROG1")
                .periodCode("2027-1")
                .teacherId(activeTeacher.getId())
                .capacity(30)
                .schedules(List.of(
                        SectionScheduleDto.builder().day("L").moduleNumber(1).build()
                ))
                .build();

        var response = sectionService.createSection(dto);
        assertThat(response.getCode()).isEqualTo("SEC-PROG1-2027-1");
        assertThat(response.getCapacity()).isEqualTo(30);
        assertThat(response.getTeacherId()).isEqualTo(activeTeacher.getId());
    }

    @Test
    @DisplayName("Éxito 2: Modificar cupo de sección existente en período abierto")
    void testUpdateSectionCapacity_Success() throws Exception {
        SectionEntity section = sectionRepository.save(SectionEntity.builder()
                .code("SEC-PROG1-2027-1")
                .courseCode("PROG1")
                .periodCode("2027-1")
                .teacherId(activeTeacher.getId())
                .capacity(30)
                .build());

        SectionUpdateDto updateDto = SectionUpdateDto.builder()
                .capacity(45)
                .build();

        var response = sectionService.updateSection(section.getId(), updateDto);
        assertThat(response.getCapacity()).isEqualTo(45);
    }

    @Test
    @DisplayName("Éxito 3: Consultar secciones disponibles por período académico")
    void testGetSectionsByPeriod_Success() throws Exception {
        sectionRepository.save(SectionEntity.builder()
                .code("SEC-PROG1-2027-1")
                .courseCode("PROG1")
                .periodCode("2027-1")
                .teacherId(activeTeacher.getId())
                .capacity(30)
                .build());

        var response = sectionService.getSectionsByPeriod("2027-1");
        assertThat(response).hasSize(1);
        assertThat(response.get(0).getCode()).isEqualTo("SEC-PROG1-2027-1");
    }

    // ==========================================
    // ESCENARIOS DE RECHAZO (3)
    // ==========================================

    @Test
    @DisplayName("Rechazo 1: Rechazar creación de sección si el período académico está CLOSED")
    void testCreateSection_PeriodClosed_Fails() throws Exception {
        SectionCreateDto dto = SectionCreateDto.builder()
                .code("SEC-PROG1-2026-2")
                .courseCode("PROG1")
                .periodCode("2026-2") // período cerrado
                .teacherId(activeTeacher.getId())
                .capacity(30)
                .schedules(List.of(
                        SectionScheduleDto.builder().day("L").moduleNumber(1).build()
                ))
                .build();

        assertThatThrownBy(() -> sectionService.createSection(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ABIERTO");
    }

    @Test
    @DisplayName("Rechazo 2: Rechazar creación si el docente asignado está INACTIVE")
    void testCreateSection_InactiveTeacher_Fails() throws Exception {
        SectionCreateDto dto = SectionCreateDto.builder()
                .code("SEC-PROG1-2027-1")
                .courseCode("PROG1")
                .periodCode("2027-1")
                .teacherId(inactiveTeacher.getId()) // docente inactivo
                .capacity(30)
                .schedules(List.of(
                        SectionScheduleDto.builder().day("L").moduleNumber(1).build()
                ))
                .build();

        assertThatThrownBy(() -> sectionService.createSection(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ACTIVO");
    }

    @Test
    @DisplayName("Rechazo 3: Rechazar si se produce conflicto de horario para el docente")
    void testCreateSection_TeacherScheduleConflict_Fails() throws Exception {
        // Sección previa del docente los Lunes M1
        sectionRepository.save(SectionEntity.builder()
                .code("SEC-EXISTING-2027-1")
                .courseCode("PROG1")
                .periodCode("2027-1")
                .teacherId(activeTeacher.getId())
                .capacity(30)
                .build());

        scheduleRepository.save(SectionScheduleEntity.builder()
                .sectionCode("SEC-EXISTING-2027-1")
                .periodCode("2027-1")
                .day("L")
                .moduleNumber(1)
                .build());

        // Asignatura 2
        courseRepository.save(CourseEntity.builder()
                .code("BD1")
                .studyPlanCode("TAP-2024")
                .name("Bases de Datos 1")
                .semester(1)
                .sct(4)
                .theoryHours(2)
                .exerciseHours(0)
                .labHours(0)
                .build());

        // Intentar asignar al mismo docente en Lunes M1
        SectionCreateDto dto = SectionCreateDto.builder()
                .code("SEC-BD1-2027-1")
                .courseCode("BD1")
                .periodCode("2027-1")
                .teacherId(activeTeacher.getId())
                .capacity(25)
                .schedules(List.of(
                        SectionScheduleDto.builder().day("L").moduleNumber(1).build()
                ))
                .build();

        assertThatThrownBy(() -> sectionService.createSection(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("asignada");
    }
}
