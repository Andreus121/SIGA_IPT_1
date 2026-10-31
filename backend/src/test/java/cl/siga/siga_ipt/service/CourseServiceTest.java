package cl.siga.siga_ipt.service;

import cl.siga.siga_ipt.dto.CourseCreateDto;
import cl.siga.siga_ipt.dto.CourseResponseDto;
import cl.siga.siga_ipt.dto.CourseUpdateDto;
import cl.siga.siga_ipt.entity.CourseEntity;
import cl.siga.siga_ipt.entity.PrerequisiteEntity;
import cl.siga.siga_ipt.repository.AcademicHistoryRepository;
import cl.siga.siga_ipt.repository.CourseRepository;
import cl.siga.siga_ipt.repository.PrerequisiteRepository;
import cl.siga.siga_ipt.repository.StudyPlanRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private StudyPlanRepository studyPlanRepository;

    @Mock
    private PrerequisiteRepository prerequisiteRepository;

    @Mock
    private AcademicHistoryRepository academicHistoryRepository;

    @InjectMocks
    private CourseService courseService;

    private CourseCreateDto buildValidCreateDto() {
        CourseCreateDto dto = new CourseCreateDto();
        dto.setCode("INF-101");
        dto.setStudyPlanCode("PLAN-2024");
        dto.setName("Introducción a la Programación");
        dto.setSemester(1);
        dto.setTheoryHours(2);
        dto.setExerciseHours(2);
        dto.setLabHours(2); // sum = 6 < 8
        dto.setSct(5);
        dto.setPrerequisitesCodes(Collections.emptySet());
        return dto;
    }

    @Test
    @DisplayName("Should create course successfully with valid rules")
    void createCourse_Success() {
        CourseCreateDto dto = buildValidCreateDto();

        when(studyPlanRepository.existsByCode("PLAN-2024")).thenReturn(true);
        when(courseRepository.existsByCodeAndStudyPlanCode("INF-101", "PLAN-2024")).thenReturn(false);
        when(courseRepository.save(any(CourseEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(prerequisiteRepository.findByCourseCodeAndStudyPlanCode("INF-101", "PLAN-2024"))
                .thenReturn(Collections.emptyList());

        CourseResponseDto response = courseService.createCourse(dto);

        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo("INF-101");
        assertThat(response.getName()).isEqualTo("Introducción a la Programación");
        assertThat(response.getSct()).isEqualTo(5);
        verify(courseRepository).save(any(CourseEntity.class));
    }

    @Test
    @DisplayName("Should create course with valid prerequisites")
    void createCourse_WithPrerequisites_Success() {
        CourseCreateDto dto = buildValidCreateDto();
        dto.setCode("INF-201");
        dto.setSemester(2);
        dto.setPrerequisitesCodes(Set.of("INF-101"));

        CourseEntity prereq = CourseEntity.builder()
                .code("INF-101")
                .studyPlanCode("PLAN-2024")
                .semester(1)
                .build();

        when(studyPlanRepository.existsByCode("PLAN-2024")).thenReturn(true);
        when(courseRepository.existsByCodeAndStudyPlanCode("INF-201", "PLAN-2024")).thenReturn(false);
        when(courseRepository.findByCodeAndStudyPlanCode("INF-101", "PLAN-2024")).thenReturn(Optional.of(prereq));
        when(courseRepository.save(any(CourseEntity.class))).thenAnswer(i -> i.getArgument(0));

        PrerequisiteEntity savedPrereq = PrerequisiteEntity.builder()
                .courseCode("INF-201")
                .studyPlanCode("PLAN-2024")
                .prerequisiteCourseCode("INF-101")
                .build();
        when(prerequisiteRepository.findByCourseCodeAndStudyPlanCode("INF-201", "PLAN-2024"))
                .thenReturn(List.of(savedPrereq));

        CourseResponseDto response = courseService.createCourse(dto);

        assertThat(response.getPrerequisitesCodes()).contains("INF-101");
        verify(prerequisiteRepository).save(any(PrerequisiteEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when study plan does not exist")
    void createCourse_StudyPlanNotFound_ThrowsException() {
        CourseCreateDto dto = buildValidCreateDto();
        when(studyPlanRepository.existsByCode("PLAN-2024")).thenReturn(false);

        assertThatThrownBy(() -> courseService.createCourse(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no existe");
    }

    @Test
    @DisplayName("Should throw exception when course code already exists in plan")
    void createCourse_DuplicateCodeInPlan_ThrowsException() {
        CourseCreateDto dto = buildValidCreateDto();
        when(studyPlanRepository.existsByCode("PLAN-2024")).thenReturn(true);
        when(courseRepository.existsByCodeAndStudyPlanCode("INF-101", "PLAN-2024")).thenReturn(true);

        assertThatThrownBy(() -> courseService.createCourse(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya existe en el plan de estudios");
    }

    @Test
    @DisplayName("Should throw exception when semester is outside 1-4 range")
    void createCourse_InvalidSemester_ThrowsException() {
        CourseCreateDto dto = buildValidCreateDto();
        dto.setSemester(5);

        when(studyPlanRepository.existsByCode("PLAN-2024")).thenReturn(true);
        when(courseRepository.existsByCodeAndStudyPlanCode("INF-101", "PLAN-2024")).thenReturn(false);

        assertThatThrownBy(() -> courseService.createCourse(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El semestre debe ser un número entero entre 1 y 4");
    }

    @Test
    @DisplayName("Should throw exception when TEL hours sum is 8 or greater")
    void createCourse_TELHoursExceeded_ThrowsException() {
        CourseCreateDto dto = buildValidCreateDto();
        dto.setTheoryHours(4);
        dto.setExerciseHours(2);
        dto.setLabHours(2); // sum = 8

        when(studyPlanRepository.existsByCode("PLAN-2024")).thenReturn(true);
        when(courseRepository.existsByCodeAndStudyPlanCode("INF-101", "PLAN-2024")).thenReturn(false);

        assertThatThrownBy(() -> courseService.createCourse(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("TEL debe ser inferior a 8 horas");
    }

    @Test
    @DisplayName("Should throw exception when TEL hours are negative")
    void createCourse_NegativeTELHours_ThrowsException() {
        CourseCreateDto dto = buildValidCreateDto();
        dto.setTheoryHours(-1);

        when(studyPlanRepository.existsByCode("PLAN-2024")).thenReturn(true);
        when(courseRepository.existsByCodeAndStudyPlanCode("INF-101", "PLAN-2024")).thenReturn(false);

        assertThatThrownBy(() -> courseService.createCourse(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no pueden ser negativas");
    }

    @Test
    @DisplayName("Should throw exception when SCT is invalid")
    void createCourse_InvalidSCT_ThrowsException() {
        CourseCreateDto dto = buildValidCreateDto();
        dto.setSct(8);

        when(studyPlanRepository.existsByCode("PLAN-2024")).thenReturn(true);
        when(courseRepository.existsByCodeAndStudyPlanCode("INF-101", "PLAN-2024")).thenReturn(false);

        assertThatThrownBy(() -> courseService.createCourse(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El valor SCT debe ser mayor que cero y menor o igual a 7");
    }

    @Test
    @DisplayName("Should throw exception when more than 3 prerequisites are provided")
    void createCourse_MoreThan3Prerequisites_ThrowsException() {
        CourseCreateDto dto = buildValidCreateDto();
        dto.setSemester(4);
        dto.setPrerequisitesCodes(Set.of("P1", "P2", "P3", "P4"));

        when(studyPlanRepository.existsByCode("PLAN-2024")).thenReturn(true);
        when(courseRepository.existsByCodeAndStudyPlanCode("INF-101", "PLAN-2024")).thenReturn(false);

        assertThatThrownBy(() -> courseService.createCourse(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no puede tener más de 3 prerrequisitos");
    }

    @Test
    @DisplayName("Should throw exception when prerequisite is from same or later semester")
    void createCourse_PrerequisiteNotFromEarlierSemester_ThrowsException() {
        CourseCreateDto dto = buildValidCreateDto();
        dto.setSemester(2);
        dto.setPrerequisitesCodes(Set.of("PREREQ-2"));

        CourseEntity prereqCourse = CourseEntity.builder()
                .code("PREREQ-2")
                .studyPlanCode("PLAN-2024")
                .semester(2) // same semester
                .build();

        when(studyPlanRepository.existsByCode("PLAN-2024")).thenReturn(true);
        when(courseRepository.existsByCodeAndStudyPlanCode("INF-101", "PLAN-2024")).thenReturn(false);
        when(courseRepository.findByCodeAndStudyPlanCode("PREREQ-2", "PLAN-2024"))
                .thenReturn(Optional.of(prereqCourse));

        assertThatThrownBy(() -> courseService.createCourse(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("debe pertenecer a un semestre anterior");
    }

    @Test
    @DisplayName("Should throw exception when prerequisite does not exist")
    void createCourse_PrerequisiteNotFound_ThrowsException() {
        CourseCreateDto dto = buildValidCreateDto();
        dto.setSemester(2);
        dto.setPrerequisitesCodes(Set.of("MISSING"));

        when(studyPlanRepository.existsByCode("PLAN-2024")).thenReturn(true);
        when(courseRepository.existsByCodeAndStudyPlanCode("INF-101", "PLAN-2024")).thenReturn(false);
        when(courseRepository.findByCodeAndStudyPlanCode("MISSING", "PLAN-2024"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.createCourse(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("MISSING")
                .hasMessageContaining("no existe");
    }

    @Test
    @DisplayName("Should update course fields and validate new semester rules")
    void updateCourse_Success() {
        CourseEntity course = CourseEntity.builder()
                .code("INF-101")
                .studyPlanCode("PLAN-2024")
                .name("Prog 1")
                .semester(1)
                .theoryHours(2)
                .exerciseHours(2)
                .labHours(2)
                .sct(4)
                .build();

        CourseUpdateDto updateDto = new CourseUpdateDto();
        updateDto.setName("Programación Avanzada");
        updateDto.setTheoryHours(3);
        updateDto.setSct(6);

        when(courseRepository.findByCodeAndStudyPlanCode("INF-101", "PLAN-2024")).thenReturn(Optional.of(course));
        when(courseRepository.save(any(CourseEntity.class))).thenAnswer(i -> i.getArgument(0));
        when(prerequisiteRepository.findByCourseCodeAndStudyPlanCode("INF-101", "PLAN-2024"))
                .thenReturn(Collections.emptyList());

        CourseResponseDto res = courseService.updateCourse("PLAN-2024", "INF-101", updateDto);

        assertThat(res.getName()).isEqualTo("Programación Avanzada");
        assertThat(res.getTheoryHours()).isEqualTo(3);
        assertThat(res.getSct()).isEqualTo(6);
    }

    @Test
    @DisplayName("Should reject updating a non-existent course")
    void updateCourse_NotFound_ThrowsException() {
        when(courseRepository.findByCodeAndStudyPlanCode("MISSING", "PLAN-2024"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.updateCourse(
                "PLAN-2024", "MISSING", new CourseUpdateDto()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no existe");
    }

    @Test
    @DisplayName("Should reject moving a course after one of its prerequisites")
    void updateCourse_PrerequisiteSemesterViolation_ThrowsException() {
        CourseEntity course = CourseEntity.builder()
                .code("INF-201")
                .studyPlanCode("PLAN-2024")
                .semester(2)
                .theoryHours(2)
                .exerciseHours(2)
                .labHours(2)
                .sct(4)
                .build();
        PrerequisiteEntity prerequisite = PrerequisiteEntity.builder()
                .courseCode("INF-201")
                .studyPlanCode("PLAN-2024")
                .prerequisiteCourseCode("INF-101")
                .build();
        CourseUpdateDto updateDto = new CourseUpdateDto();
        updateDto.setSemester(1);

        when(courseRepository.findByCodeAndStudyPlanCode("INF-201", "PLAN-2024"))
                .thenReturn(Optional.of(course));
        when(prerequisiteRepository.findByCourseCodeAndStudyPlanCode("INF-201", "PLAN-2024"))
                .thenReturn(List.of(prerequisite));
        when(courseRepository.findByCodeAndStudyPlanCode("INF-101", "PLAN-2024"))
                .thenReturn(Optional.of(CourseEntity.builder().semester(2).build()));

        assertThatThrownBy(() -> courseService.updateCourse(
                "PLAN-2024", "INF-201", updateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("estrictamente posterior");
    }

    @Test
    @DisplayName("Should throw exception when moving course to semester invalid for its dependents")
    void updateCourse_MovingSemesterViolatesDependent_ThrowsException() {
        CourseEntity course = CourseEntity.builder()
                .code("INF-101")
                .studyPlanCode("PLAN-2024")
                .semester(1)
                .theoryHours(2)
                .exerciseHours(2)
                .labHours(2)
                .sct(4)
                .build();

        CourseUpdateDto updateDto = new CourseUpdateDto();
        updateDto.setSemester(3);

        PrerequisiteEntity depPrereq = PrerequisiteEntity.builder()
                .courseCode("INF-201")
                .studyPlanCode("PLAN-2024")
                .prerequisiteCourseCode("INF-101")
                .build();

        CourseEntity dependentCourse = CourseEntity.builder()
                .code("INF-201")
                .studyPlanCode("PLAN-2024")
                .semester(2)
                .build();

        when(courseRepository.findByCodeAndStudyPlanCode("INF-101", "PLAN-2024")).thenReturn(Optional.of(course));
        when(prerequisiteRepository.findByCourseCodeAndStudyPlanCode("INF-101", "PLAN-2024")).thenReturn(Collections.emptyList());
        when(prerequisiteRepository.findByPrerequisiteCourseCodeAndStudyPlanCode("INF-101", "PLAN-2024")).thenReturn(List.of(depPrereq));
        when(courseRepository.findByCodeAndStudyPlanCode("INF-201", "PLAN-2024")).thenReturn(Optional.of(dependentCourse));

        assertThatThrownBy(() -> courseService.updateCourse("PLAN-2024", "INF-101", updateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No se puede mover la asignatura al semestre 3");
    }

    @Test
    @DisplayName("Should delete course when no academic history or dependent prerequisites exist")
    void deleteCourse_Success() {
        CourseEntity course = CourseEntity.builder().code("INF-101").studyPlanCode("PLAN-2024").build();

        when(courseRepository.findByCodeAndStudyPlanCode("INF-101", "PLAN-2024")).thenReturn(Optional.of(course));
        when(academicHistoryRepository.existsByCourseCodeAndStudyPlanCode("INF-101", "PLAN-2024")).thenReturn(false);
        when(prerequisiteRepository.existsByPrerequisiteCourseCodeAndStudyPlanCode("INF-101", "PLAN-2024")).thenReturn(false);

        courseService.deleteCourse("PLAN-2024", "INF-101");

        verify(prerequisiteRepository).deleteByCourseCodeAndStudyPlanCode("INF-101", "PLAN-2024");
        verify(courseRepository).delete(course);
    }

    @Test
    @DisplayName("Should throw exception when deleting course with academic history")
    void deleteCourse_WithAcademicHistory_ThrowsException() {
        CourseEntity course = CourseEntity.builder().code("INF-101").studyPlanCode("PLAN-2024").build();

        when(courseRepository.findByCodeAndStudyPlanCode("INF-101", "PLAN-2024")).thenReturn(Optional.of(course));
        when(academicHistoryRepository.existsByCourseCodeAndStudyPlanCode("INF-101", "PLAN-2024")).thenReturn(true);

        assertThatThrownBy(() -> courseService.deleteCourse("PLAN-2024", "INF-101"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("historial académico");

        verify(courseRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw exception when deleting course that is prerequisite for another")
    void deleteCourse_IsPrerequisiteForAnother_ThrowsException() {
        CourseEntity course = CourseEntity.builder().code("INF-101").studyPlanCode("PLAN-2024").build();

        when(courseRepository.findByCodeAndStudyPlanCode("INF-101", "PLAN-2024")).thenReturn(Optional.of(course));
        when(academicHistoryRepository.existsByCourseCodeAndStudyPlanCode("INF-101", "PLAN-2024")).thenReturn(false);
        when(prerequisiteRepository.existsByPrerequisiteCourseCodeAndStudyPlanCode("INF-101", "PLAN-2024")).thenReturn(true);

        assertThatThrownBy(() -> courseService.deleteCourse("PLAN-2024", "INF-101"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("es prerrequisito de otra asignatura");

        verify(courseRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should reject deleting a non-existent course")
    void deleteCourse_NotFound_ThrowsException() {
        when(courseRepository.findByCodeAndStudyPlanCode("MISSING", "PLAN-2024"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.deleteCourse("PLAN-2024", "MISSING"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no existe");
    }

    @Test
    @DisplayName("Should get courses by study plan code")
    void getCoursesByPlan_Success() {
        CourseEntity c1 = CourseEntity.builder().code("INF-101").studyPlanCode("PLAN-2024").build();
        when(studyPlanRepository.existsByCode("PLAN-2024")).thenReturn(true);
        when(courseRepository.findByStudyPlanCode("PLAN-2024")).thenReturn(List.of(c1));
        when(prerequisiteRepository.findByCourseCodeAndStudyPlanCode("INF-101", "PLAN-2024")).thenReturn(Collections.emptyList());

        List<CourseResponseDto> list = courseService.getCoursesByPlan("PLAN-2024");

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getCode()).isEqualTo("INF-101");
    }

    @Test
    @DisplayName("Should throw exception when getting courses for non-existent study plan")
    void getCoursesByPlan_PlanNotFound_ThrowsException() {
        when(studyPlanRepository.existsByCode("NON_EXIST")).thenReturn(false);

        assertThatThrownBy(() -> courseService.getCoursesByPlan("NON_EXIST"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no existe");
    }

    @Test
    @DisplayName("Should return all courses")
    void getAllCourses_Success() {
        CourseEntity course = CourseEntity.builder()
                .code("INF-101")
                .studyPlanCode("PLAN-2024")
                .name("Programación")
                .build();
        when(courseRepository.findAll()).thenReturn(List.of(course));
        when(prerequisiteRepository.findByCourseCodeAndStudyPlanCode("INF-101", "PLAN-2024"))
                .thenReturn(Collections.emptyList());

        List<CourseResponseDto> result = courseService.getAllCourses();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("INF-101");
    }

    @Test
    @DisplayName("Should get course by code and plan code successfully")
    void getCourse_Success() {
        CourseEntity course = CourseEntity.builder().code("INF-101").studyPlanCode("PLAN-2024").build();
        when(courseRepository.findByCodeAndStudyPlanCode("INF-101", "PLAN-2024")).thenReturn(Optional.of(course));
        when(prerequisiteRepository.findByCourseCodeAndStudyPlanCode("INF-101", "PLAN-2024")).thenReturn(Collections.emptyList());

        CourseResponseDto res = courseService.getCourse("PLAN-2024", "INF-101");

        assertThat(res).isNotNull();
        assertThat(res.getCode()).isEqualTo("INF-101");
    }

    @Test
    @DisplayName("Should throw exception when getting non-existent course")
    void getCourse_NotFound_ThrowsException() {
        when(courseRepository.findByCodeAndStudyPlanCode("INF-999", "PLAN-2024")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getCourse("PLAN-2024", "INF-999"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no existe");
    }

    @Test
    @DisplayName("Should update course with new prerequisites")
    void updateCourse_WithPrerequisites_Success() {
        CourseEntity course = CourseEntity.builder()
                .code("INF-201")
                .studyPlanCode("PLAN-2024")
                .semester(2)
                .theoryHours(2)
                .exerciseHours(2)
                .labHours(2)
                .sct(4)
                .build();

        CourseEntity prereq = CourseEntity.builder()
                .code("INF-101")
                .studyPlanCode("PLAN-2024")
                .semester(1)
                .build();

        CourseUpdateDto updateDto = new CourseUpdateDto();
        updateDto.setPrerequisitesCodes(Set.of("INF-101"));

        when(courseRepository.findByCodeAndStudyPlanCode("INF-201", "PLAN-2024")).thenReturn(Optional.of(course));
        when(courseRepository.findByCodeAndStudyPlanCode("INF-101", "PLAN-2024")).thenReturn(Optional.of(prereq));
        when(courseRepository.save(any(CourseEntity.class))).thenAnswer(i -> i.getArgument(0));

        PrerequisiteEntity pe = PrerequisiteEntity.builder()
                .courseCode("INF-201")
                .studyPlanCode("PLAN-2024")
                .prerequisiteCourseCode("INF-101")
                .build();
        when(prerequisiteRepository.findByCourseCodeAndStudyPlanCode("INF-201", "PLAN-2024")).thenReturn(List.of(pe));

        CourseResponseDto res = courseService.updateCourse("PLAN-2024", "INF-201", updateDto);

        assertThat(res.getPrerequisitesCodes()).contains("INF-101");
        verify(prerequisiteRepository).deleteByCourseCodeAndStudyPlanCode("INF-201", "PLAN-2024");
        verify(prerequisiteRepository).save(any(PrerequisiteEntity.class));
    }
}
