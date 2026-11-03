package cl.siga.siga_ipt.service;

import cl.siga.siga_ipt.dto.SectionCreateDto;
import cl.siga.siga_ipt.dto.SectionResponseDto;
import cl.siga.siga_ipt.dto.SectionScheduleDto;
import cl.siga.siga_ipt.dto.SectionUpdateDto;
import cl.siga.siga_ipt.entity.AcademicPeriodEntity;
import cl.siga.siga_ipt.entity.CourseEntity;
import cl.siga.siga_ipt.entity.SectionEntity;
import cl.siga.siga_ipt.entity.SectionScheduleEntity;
import cl.siga.siga_ipt.entity.TeacherEntity;
import cl.siga.siga_ipt.enums.PeriodStatus;
import cl.siga.siga_ipt.enums.TeacherStatus;
import cl.siga.siga_ipt.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SectionServiceTest {

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private SectionScheduleRepository scheduleRepository;

    @Mock
    private AcademicPeriodRepository periodRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private RegistrationRepository registrationRepository;

    @InjectMocks
    private SectionService sectionService;

    private SectionCreateDto buildValidSectionCreateDto() {
        return SectionCreateDto.builder()
                .code("INF101-SEC1")
                .courseCode("INF-101")
                .periodCode("2027-1")
                .teacherId(10L)
                .capacity(45)
                .schedules(List.of(
                        new SectionScheduleDto("L", 1),
                        new SectionScheduleDto("W", 2)
                ))
                .build();
    }

    private CourseEntity buildCourseEntity(int theory, int exercise, int lab) {
        return CourseEntity.builder()
                .code("INF-101")
                .theoryHours(theory)
                .exerciseHours(exercise)
                .labHours(lab)
                .build();
    }

    @Test
    @DisplayName("Should create section successfully when all rules are satisfied")
    void createSection_Success() {
        SectionCreateDto dto = buildValidSectionCreateDto();

        AcademicPeriodEntity period = AcademicPeriodEntity.builder().periodCode("2027-1").status(PeriodStatus.OPEN).build();
        CourseEntity course = buildCourseEntity(2, 2, 0); // 4 TEL hours = 2 blocks
        TeacherEntity teacher = TeacherEntity.builder().id(10L).status(TeacherStatus.ACTIVE).build();

        when(sectionRepository.existsByCodeAndPeriodCode("INF101-SEC1", "2027-1")).thenReturn(false);
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(period));
        when(courseRepository.findFirstByCode("INF-101")).thenReturn(Optional.of(course));
        when(sectionRepository.existsByCourseCodeAndPeriodCode("INF-101", "2027-1")).thenReturn(false);
        when(teacherRepository.findById(10L)).thenReturn(Optional.of(teacher));
        when(sectionRepository.findByTeacherIdAndPeriodCode(10L, "2027-1")).thenReturn(Collections.emptyList());

        SectionEntity savedSection = SectionEntity.builder()
                .id(1L)
                .code("INF101-SEC1")
                .courseCode("INF-101")
                .periodCode("2027-1")
                .teacherId(10L)
                .capacity(45)
                .build();
        when(sectionRepository.save(any(SectionEntity.class))).thenReturn(savedSection);

        SectionResponseDto response = sectionService.createSection(dto);

        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo("INF101-SEC1");
        assertThat(response.getCapacity()).isEqualTo(45);
        verify(sectionRepository).save(any(SectionEntity.class));
        verify(scheduleRepository, times(2)).save(any(SectionScheduleEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when section code already exists")
    void createSection_DuplicateCode_ThrowsException() {
        SectionCreateDto dto = buildValidSectionCreateDto();
        when(sectionRepository.existsByCodeAndPeriodCode("INF101-SEC1", "2027-1")).thenReturn(true);

        assertThatThrownBy(() -> sectionService.createSection(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe una sección con el código");
    }

    @Test
    @DisplayName("Should throw exception when academic period does not exist")
    void createSection_PeriodNotFound_ThrowsException() {
        SectionCreateDto dto = buildValidSectionCreateDto();
        when(sectionRepository.existsByCodeAndPeriodCode("INF101-SEC1", "2027-1")).thenReturn(false);
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sectionService.createSection(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no existe");
    }

    @Test
    @DisplayName("Should throw exception when academic period is CLOSED")
    void createSection_PeriodClosed_ThrowsException() {
        SectionCreateDto dto = buildValidSectionCreateDto();
        AcademicPeriodEntity closedPeriod = AcademicPeriodEntity.builder().periodCode("2027-1").status(PeriodStatus.CLOSED).build();

        when(sectionRepository.existsByCodeAndPeriodCode("INF101-SEC1", "2027-1")).thenReturn(false);
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(closedPeriod));

        assertThatThrownBy(() -> sectionService.createSection(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("estado ABIERTO");
    }

    @Test
    @DisplayName("Should throw exception when section already exists for course in the period")
    void createSection_DuplicateCourseInPeriod_ThrowsException() {
        SectionCreateDto dto = buildValidSectionCreateDto();
        AcademicPeriodEntity period = AcademicPeriodEntity.builder().periodCode("2027-1").status(PeriodStatus.OPEN).build();
        CourseEntity course = buildCourseEntity(2, 2, 0);

        when(sectionRepository.existsByCodeAndPeriodCode("INF101-SEC1", "2027-1")).thenReturn(false);
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(period));
        when(courseRepository.findFirstByCode("INF-101")).thenReturn(Optional.of(course));
        when(sectionRepository.existsByCourseCodeAndPeriodCode("INF-101", "2027-1")).thenReturn(true);

        assertThatThrownBy(() -> sectionService.createSection(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe una sección para la asignatura");
    }

    @Test
    @DisplayName("Should throw exception when teacher is INACTIVE")
    void createSection_TeacherInactive_ThrowsException() {
        SectionCreateDto dto = buildValidSectionCreateDto();
        AcademicPeriodEntity period = AcademicPeriodEntity.builder().periodCode("2027-1").status(PeriodStatus.OPEN).build();
        CourseEntity course = buildCourseEntity(2, 2, 0);
        TeacherEntity inactiveTeacher = TeacherEntity.builder().id(10L).status(TeacherStatus.INACTIVE).build();

        when(sectionRepository.existsByCodeAndPeriodCode("INF101-SEC1", "2027-1")).thenReturn(false);
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(period));
        when(courseRepository.findFirstByCode("INF-101")).thenReturn(Optional.of(course));
        when(sectionRepository.existsByCourseCodeAndPeriodCode("INF-101", "2027-1")).thenReturn(false);
        when(teacherRepository.findById(10L)).thenReturn(Optional.of(inactiveTeacher));

        assertThatThrownBy(() -> sectionService.createSection(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Solo se pueden asignar docentes en estado ACTIVO");
    }

    @Test
    @DisplayName("Should throw exception when capacity exceeds 60 or is less than 1")
    void createSection_InvalidCapacity_ThrowsException() {
        SectionCreateDto dto = buildValidSectionCreateDto();
        dto.setCapacity(65);

        AcademicPeriodEntity period = AcademicPeriodEntity.builder().periodCode("2027-1").status(PeriodStatus.OPEN).build();
        CourseEntity course = buildCourseEntity(2, 2, 0);
        TeacherEntity teacher = TeacherEntity.builder().id(10L).status(TeacherStatus.ACTIVE).build();

        when(sectionRepository.existsByCodeAndPeriodCode("INF101-SEC1", "2027-1")).thenReturn(false);
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(period));
        when(courseRepository.findFirstByCode("INF-101")).thenReturn(Optional.of(course));
        when(sectionRepository.existsByCourseCodeAndPeriodCode("INF-101", "2027-1")).thenReturn(false);
        when(teacherRepository.findById(10L)).thenReturn(Optional.of(teacher));

        assertThatThrownBy(() -> sectionService.createSection(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("máximo 60");
    }

    @Test
    @DisplayName("Should throw exception when schedule blocks do not match course TEL hours")
    void createSection_ScheduleBlocksMismatchTEL_ThrowsException() {
        SectionCreateDto dto = buildValidSectionCreateDto();
        // course has 6 TEL hours (needs 3 blocks), but dto provides only 2 blocks
        CourseEntity course = buildCourseEntity(2, 2, 2);
        AcademicPeriodEntity period = AcademicPeriodEntity.builder().periodCode("2027-1").status(PeriodStatus.OPEN).build();
        TeacherEntity teacher = TeacherEntity.builder().id(10L).status(TeacherStatus.ACTIVE).build();

        when(sectionRepository.existsByCodeAndPeriodCode("INF101-SEC1", "2027-1")).thenReturn(false);
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(period));
        when(courseRepository.findFirstByCode("INF-101")).thenReturn(Optional.of(course));
        when(sectionRepository.existsByCourseCodeAndPeriodCode("INF-101", "2027-1")).thenReturn(false);
        when(teacherRepository.findById(10L)).thenReturn(Optional.of(teacher));

        assertThatThrownBy(() -> sectionService.createSection(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no coincide con las horas TEL");
    }

    @Test
    @DisplayName("Should throw exception when schedules have duplicate blocks in same section")
    void createSection_DuplicateScheduleBlocks_ThrowsException() {
        SectionCreateDto dto = buildValidSectionCreateDto();
        dto.setSchedules(List.of(
                new SectionScheduleDto("L", 1),
                new SectionScheduleDto("L", 1) // duplicate
        ));

        CourseEntity course = buildCourseEntity(2, 2, 0);
        AcademicPeriodEntity period = AcademicPeriodEntity.builder().periodCode("2027-1").status(PeriodStatus.OPEN).build();
        TeacherEntity teacher = TeacherEntity.builder().id(10L).status(TeacherStatus.ACTIVE).build();

        when(sectionRepository.existsByCodeAndPeriodCode("INF101-SEC1", "2027-1")).thenReturn(false);
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(period));
        when(courseRepository.findFirstByCode("INF-101")).thenReturn(Optional.of(course));
        when(sectionRepository.existsByCourseCodeAndPeriodCode("INF-101", "2027-1")).thenReturn(false);
        when(teacherRepository.findById(10L)).thenReturn(Optional.of(teacher));

        assertThatThrownBy(() -> sectionService.createSection(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicados");
    }

    @Test
    @DisplayName("Should throw exception when teacher has schedule conflict with another section in same period")
    void createSection_TeacherScheduleConflict_ThrowsException() {
        SectionCreateDto dto = buildValidSectionCreateDto(); // L-1, W-2

        CourseEntity course = buildCourseEntity(2, 2, 0);
        AcademicPeriodEntity period = AcademicPeriodEntity.builder().periodCode("2027-1").status(PeriodStatus.OPEN).build();
        TeacherEntity teacher = TeacherEntity.builder().id(10L).status(TeacherStatus.ACTIVE).build();

        SectionEntity otherSection = SectionEntity.builder().code("BD101-SEC1").periodCode("2027-1").teacherId(10L).build();
        SectionScheduleEntity occupiedBlock = SectionScheduleEntity.builder()
                .sectionCode("BD101-SEC1")
                .periodCode("2027-1")
                .day("L")
                .moduleNumber(1) // conflict with proposed L-1
                .build();

        when(sectionRepository.existsByCodeAndPeriodCode("INF101-SEC1", "2027-1")).thenReturn(false);
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(period));
        when(courseRepository.findFirstByCode("INF-101")).thenReturn(Optional.of(course));
        when(sectionRepository.existsByCourseCodeAndPeriodCode("INF-101", "2027-1")).thenReturn(false);
        when(teacherRepository.findById(10L)).thenReturn(Optional.of(teacher));
        when(sectionRepository.findByTeacherIdAndPeriodCode(10L, "2027-1")).thenReturn(List.of(otherSection));
        when(scheduleRepository.findBySectionCodeAndPeriodCode("BD101-SEC1", "2027-1")).thenReturn(List.of(occupiedBlock));

        assertThatThrownBy(() -> sectionService.createSection(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya tiene asignada la sección 'BD101-SEC1'");
    }

    @Test
    @DisplayName("Should update section capacity and teacher successfully")
    void updateSection_Success() {
        SectionEntity section = SectionEntity.builder()
                .id(1L)
                .code("INF101-SEC1")
                .courseCode("INF-101")
                .periodCode("2027-1")
                .teacherId(10L)
                .capacity(40)
                .build();

        AcademicPeriodEntity period = AcademicPeriodEntity.builder().periodCode("2027-1").status(PeriodStatus.OPEN).build();
        CourseEntity course = buildCourseEntity(2, 2, 0);
        TeacherEntity newTeacher = TeacherEntity.builder().id(20L).status(TeacherStatus.ACTIVE).build();

        SectionUpdateDto updateDto = SectionUpdateDto.builder()
                .teacherId(20L)
                .capacity(50)
                .build();

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(period));
        when(courseRepository.findFirstByCode("INF-101")).thenReturn(Optional.of(course));
        when(teacherRepository.findById(20L)).thenReturn(Optional.of(newTeacher));
        when(sectionRepository.findByTeacherIdAndPeriodCode(20L, "2027-1")).thenReturn(Collections.emptyList());
        when(sectionRepository.save(any(SectionEntity.class))).thenAnswer(i -> i.getArgument(0));

        SectionResponseDto response = sectionService.updateSection(1L, updateDto);

        assertThat(response.getCapacity()).isEqualTo(50);
        assertThat(response.getTeacherId()).isEqualTo(20L);
    }

    @Test
    @DisplayName("Should throw exception when updating section in CLOSED period")
    void updateSection_PeriodClosed_ThrowsException() {
        SectionEntity section = SectionEntity.builder().id(1L).periodCode("2026-2").build();
        AcademicPeriodEntity period = AcademicPeriodEntity.builder().periodCode("2026-2").status(PeriodStatus.CLOSED).build();

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(periodRepository.findByPeriodCode("2026-2")).thenReturn(Optional.of(period));

        SectionUpdateDto updateDto = SectionUpdateDto.builder().capacity(30).build();

        assertThatThrownBy(() -> sectionService.updateSection(1L, updateDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CERRADO");
    }

    @Test
    @DisplayName("Should delete section when no students are registered")
    void deleteSection_Success() {
        SectionEntity section = SectionEntity.builder().id(1L).code("SEC-1").periodCode("2027-1").build();

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(registrationRepository.existsBySectionCodeAndPeriodCode("SEC-1", "2027-1")).thenReturn(false);
        when(registrationRepository.existsBySectionCode("SEC-1")).thenReturn(false);

        sectionService.deleteSection(1L);

        verify(scheduleRepository).deleteBySectionCodeAndPeriodCode("SEC-1", "2027-1");
        verify(sectionRepository).delete(section);
    }

    @Test
    @DisplayName("Should throw exception when deleting section with registered students")
    void deleteSection_WithStudents_ThrowsException() {
        SectionEntity section = SectionEntity.builder().id(1L).code("SEC-1").periodCode("2027-1").build();

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(registrationRepository.existsBySectionCodeAndPeriodCode("SEC-1", "2027-1")).thenReturn(true);

        assertThatThrownBy(() -> sectionService.deleteSection(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tiene estudiantes inscritos");

        verify(sectionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should get section by ID, code and period")
    void getSectionQueries_Success() {
        SectionEntity section = SectionEntity.builder().id(1L).code("SEC-1").periodCode("2027-1").build();

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(sectionRepository.findByCodeAndPeriodCode("SEC-1", "2027-1")).thenReturn(Optional.of(section));
        when(sectionRepository.findByPeriodCode("2027-1")).thenReturn(List.of(section));

        assertThat(sectionService.getSectionById(1L)).isNotNull();
        assertThat(sectionService.getSectionByCodeAndPeriod("SEC-1", "2027-1")).isNotNull();
        assertThat(sectionService.getSectionsByPeriod("2027-1")).hasSize(1);
    }

    @Test
    @DisplayName("Should update section with new schedules successfully")
    void updateSection_WithSchedules_Success() {
        SectionEntity section = SectionEntity.builder()
                .id(1L)
                .code("SEC-1")
                .courseCode("INF-101")
                .periodCode("2027-1")
                .teacherId(10L)
                .capacity(30)
                .build();

        AcademicPeriodEntity period = AcademicPeriodEntity.builder().periodCode("2027-1").status(PeriodStatus.OPEN).build();
        CourseEntity course = buildCourseEntity(2, 2, 0);

        SectionUpdateDto updateDto = SectionUpdateDto.builder()
                .schedules(List.of(new SectionScheduleDto("M", 1), new SectionScheduleDto("J", 2)))
                .build();

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(period));
        when(courseRepository.findFirstByCode("INF-101")).thenReturn(Optional.of(course));
        when(sectionRepository.findByTeacherIdAndPeriodCode(10L, "2027-1")).thenReturn(List.of(section));
        when(sectionRepository.save(any(SectionEntity.class))).thenAnswer(i -> i.getArgument(0));

        SectionResponseDto res = sectionService.updateSection(1L, updateDto);

        assertThat(res).isNotNull();
        verify(scheduleRepository).deleteBySectionCodeAndPeriodCode("SEC-1", "2027-1");
    }

    @Test
    @DisplayName("Should throw exception when updating section with inactive teacher")
    void updateSection_InactiveTeacher_ThrowsException() {
        SectionEntity section = SectionEntity.builder()
                .id(1L)
                .code("SEC-1")
                .courseCode("INF-101")
                .periodCode("2027-1")
                .teacherId(10L)
                .build();

        AcademicPeriodEntity period = AcademicPeriodEntity.builder().periodCode("2027-1").status(PeriodStatus.OPEN).build();
        CourseEntity course = buildCourseEntity(2, 2, 0);
        TeacherEntity inactiveTeacher = TeacherEntity.builder().id(20L).status(TeacherStatus.INACTIVE).build();

        SectionUpdateDto updateDto = SectionUpdateDto.builder().teacherId(20L).build();

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(period));
        when(courseRepository.findFirstByCode("INF-101")).thenReturn(Optional.of(course));
        when(teacherRepository.findById(20L)).thenReturn(Optional.of(inactiveTeacher));

        assertThatThrownBy(() -> sectionService.updateSection(1L, updateDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Solo se pueden asignar docentes en estado ACTIVO");
    }

    @Test
    @DisplayName("Should throw exception when section not found by ID or code")
    void sectionQueries_NotFound_ThrowsException() {
        when(sectionRepository.findById(999L)).thenReturn(Optional.empty());
        when(sectionRepository.findByCodeAndPeriodCode("NON-EXIST", "2027-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sectionService.getSectionById(999L))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> sectionService.getSectionByCodeAndPeriod("NON-EXIST", "2027-1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should reject section when course or teacher does not exist")
    void createSection_MissingCourseOrTeacher_ThrowsException() {
        SectionCreateDto dto = buildValidSectionCreateDto();
        AcademicPeriodEntity period = AcademicPeriodEntity.builder()
                .periodCode("2027-1").status(PeriodStatus.OPEN).build();

        when(sectionRepository.existsByCodeAndPeriodCode(dto.getCode(), dto.getPeriodCode())).thenReturn(false);
        when(periodRepository.findByPeriodCode(dto.getPeriodCode())).thenReturn(Optional.of(period));
        when(courseRepository.findFirstByCode(dto.getCourseCode())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sectionService.createSection(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La asignatura");

        CourseEntity course = buildCourseEntity(2, 2, 0);
        when(courseRepository.findFirstByCode(dto.getCourseCode())).thenReturn(Optional.of(course));
        when(sectionRepository.existsByCourseCodeAndPeriodCode(dto.getCourseCode(), dto.getPeriodCode())).thenReturn(false);
        when(teacherRepository.findById(dto.getTeacherId())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sectionService.createSection(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El docente");
    }

    @Test
    @DisplayName("Should reject null, empty, invalid and duplicate schedules")
    void createSection_InvalidSchedules_ThrowsException() {
        SectionCreateDto dto = buildValidSectionCreateDto();
        AcademicPeriodEntity period = AcademicPeriodEntity.builder()
                .periodCode("2027-1").status(PeriodStatus.OPEN).build();
        CourseEntity course = buildCourseEntity(2, 2, 0);
        TeacherEntity teacher = TeacherEntity.builder().id(10L).status(TeacherStatus.ACTIVE).build();

        when(sectionRepository.existsByCodeAndPeriodCode(dto.getCode(), dto.getPeriodCode())).thenReturn(false);
        when(periodRepository.findByPeriodCode(dto.getPeriodCode())).thenReturn(Optional.of(period));
        when(courseRepository.findFirstByCode(dto.getCourseCode())).thenReturn(Optional.of(course));
        when(sectionRepository.existsByCourseCodeAndPeriodCode(dto.getCourseCode(), dto.getPeriodCode())).thenReturn(false);
        when(teacherRepository.findById(dto.getTeacherId())).thenReturn(Optional.of(teacher));

        dto.setSchedules(List.of());
        assertThatThrownBy(() -> sectionService.createSection(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Debe asignar horarios");

        dto.setSchedules(List.of(new SectionScheduleDto(null, 1), new SectionScheduleDto("W", 2)));
        assertThatThrownBy(() -> sectionService.createSection(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Día de horario inválido");

        dto.setSchedules(List.of(new SectionScheduleDto("X", 1), new SectionScheduleDto("W", 2)));
        assertThatThrownBy(() -> sectionService.createSection(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Día de horario inválido");

        dto.setSchedules(List.of(new SectionScheduleDto("L", null), new SectionScheduleDto("W", 2)));
        assertThatThrownBy(() -> sectionService.createSection(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Módulo inválido");

        dto.setSchedules(List.of(new SectionScheduleDto("L", 0), new SectionScheduleDto("W", 2)));
        assertThatThrownBy(() -> sectionService.createSection(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Módulo inválido");

        dto.setSchedules(List.of(new SectionScheduleDto("L", 1), new SectionScheduleDto("L", 1)));
        assertThatThrownBy(() -> sectionService.createSection(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicados");
    }

    @Test
    @DisplayName("Should reject update when section, period or course does not exist")
    void updateSection_MissingData_ThrowsException() {
        SectionUpdateDto dto = SectionUpdateDto.builder().capacity(30).build();
        when(sectionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sectionService.updateSection(99L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no encontrada");

        SectionEntity section = SectionEntity.builder().id(1L).periodCode("2027-1").build();
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sectionService.updateSection(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("período");

        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(
                AcademicPeriodEntity.builder().periodCode("2027-1").status(PeriodStatus.OPEN).build()));
        when(courseRepository.findFirstByCode(null)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sectionService.updateSection(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("asociada");
    }

    @Test
    @DisplayName("Should reject update with missing teacher or teacher schedule conflict")
    void updateSection_TeacherValidation_ThrowsException() {
        SectionEntity section = SectionEntity.builder()
                .id(1L).code("SEC-1").courseCode("INF-101").periodCode("2027-1").teacherId(10L).build();
        AcademicPeriodEntity period = AcademicPeriodEntity.builder()
                .periodCode("2027-1").status(PeriodStatus.OPEN).build();
        CourseEntity course = buildCourseEntity(2, 2, 0);
        SectionUpdateDto dto = SectionUpdateDto.builder().teacherId(20L).build();

        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(period));
        when(courseRepository.findFirstByCode("INF-101")).thenReturn(Optional.of(course));
        when(teacherRepository.findById(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sectionService.updateSection(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El docente");

        TeacherEntity teacher = TeacherEntity.builder().id(20L).status(TeacherStatus.ACTIVE).build();
        SectionEntity otherSection = SectionEntity.builder().code("OTHER").periodCode("2027-1").build();
        when(teacherRepository.findById(20L)).thenReturn(Optional.of(teacher));
        when(sectionRepository.findByTeacherIdAndPeriodCode(20L, "2027-1")).thenReturn(List.of(otherSection));
        when(scheduleRepository.findBySectionCodeAndPeriodCode("SEC-1", "2027-1"))
                .thenReturn(List.of(new SectionScheduleEntity(1L, "SEC-1", "2027-1", "L", 1)));
        when(scheduleRepository.findBySectionCodeAndPeriodCode("OTHER", "2027-1"))
                .thenReturn(List.of(new SectionScheduleEntity(2L, "OTHER", "2027-1", "L", 1)));

        assertThatThrownBy(() -> sectionService.updateSection(1L, dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya tiene asignada");
    }

    @Test
    @DisplayName("Should reject update when the new schedule is invalid")
    void updateSection_InvalidSchedules_ThrowsException() {
        SectionEntity section = SectionEntity.builder()
                .id(1L).code("SEC-1").courseCode("INF-101").periodCode("2027-1").teacherId(10L).build();
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(
                AcademicPeriodEntity.builder().periodCode("2027-1").status(PeriodStatus.OPEN).build()));
        when(courseRepository.findFirstByCode("INF-101")).thenReturn(Optional.of(buildCourseEntity(2, 2, 0)));

        SectionUpdateDto dto = SectionUpdateDto.builder()
                .schedules(List.of(new SectionScheduleDto("L", 1))).build();

        assertThatThrownBy(() -> sectionService.updateSection(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no coincide");
    }

    @Test
    @DisplayName("Should reject delete when a registration exists in another registration lookup")
    void deleteSection_WithHistoricalRegistration_ThrowsException() {
        SectionEntity section = SectionEntity.builder().id(1L).code("SEC-1").periodCode("2027-1").build();
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(section));
        when(registrationRepository.existsBySectionCodeAndPeriodCode("SEC-1", "2027-1")).thenReturn(false);
        when(registrationRepository.existsBySectionCode("SEC-1")).thenReturn(true);

        assertThatThrownBy(() -> sectionService.deleteSection(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("estudiantes inscritos");
    }
}
