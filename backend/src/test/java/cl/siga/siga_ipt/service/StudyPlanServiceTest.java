package cl.siga.siga_ipt.service;

import cl.siga.siga_ipt.dto.StudyPlanCreateDto;
import cl.siga.siga_ipt.dto.StudyPlanResponseDto;
import cl.siga.siga_ipt.entity.CareerEntity;
import cl.siga.siga_ipt.entity.StudyPlanEntity;
import cl.siga.siga_ipt.enums.StudyPlanStatus;
import cl.siga.siga_ipt.repository.CareerRepository;
import cl.siga.siga_ipt.repository.CourseRepository;
import cl.siga.siga_ipt.repository.StudentRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudyPlanServiceTest {

    @Mock
    private StudyPlanRepository studyPlanRepository;

    @Mock
    private CareerRepository careerRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private StudyPlanService studyPlanService;

    @Test
    @DisplayName("Should create first study plan for a career as CURRENT automatically")
    void createStudyPlan_FirstPlan_BecomesCurrent() {
        StudyPlanCreateDto dto = new StudyPlanCreateDto();
        dto.setCode("PLAN-TAP-2024");
        dto.setCareerCode("TAP");
        dto.setName("Plan 2024");
        dto.setSetCurrent(false);

        CareerEntity career = CareerEntity.builder().code("TAP").build();

        when(studyPlanRepository.findByCode("PLAN-TAP-2024")).thenReturn(Optional.empty());
        when(careerRepository.findByCode("TAP")).thenReturn(Optional.of(career));
        when(studyPlanRepository.findAllByCareerCode("TAP")).thenReturn(Collections.emptyList());
        when(studyPlanRepository.findByCareerCodeAndStatus("TAP", StudyPlanStatus.CURRENT)).thenReturn(Optional.empty());
        when(studyPlanRepository.save(any(StudyPlanEntity.class))).thenAnswer(i -> i.getArgument(0));

        StudyPlanResponseDto response = studyPlanService.createStudyPlan(dto);

        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo("PLAN-TAP-2024");
        assertThat(response.getStatus()).isEqualTo(StudyPlanStatus.CURRENT);
        verify(studyPlanRepository).save(any(StudyPlanEntity.class));
    }

    @Test
    @DisplayName("Should create second study plan as NOT_CURRENT when setCurrent is false")
    void createStudyPlan_SecondPlanNotCurrent() {
        StudyPlanCreateDto dto = new StudyPlanCreateDto();
        dto.setCode("PLAN-TAP-2025");
        dto.setCareerCode("TAP");
        dto.setName("Plan 2025");
        dto.setSetCurrent(false);

        CareerEntity career = CareerEntity.builder().code("TAP").build();
        StudyPlanEntity existingPlan = StudyPlanEntity.builder().code("PLAN-TAP-2024").build();

        when(studyPlanRepository.findByCode("PLAN-TAP-2025")).thenReturn(Optional.empty());
        when(careerRepository.findByCode("TAP")).thenReturn(Optional.of(career));
        when(studyPlanRepository.findAllByCareerCode("TAP")).thenReturn(List.of(existingPlan));
        when(studyPlanRepository.save(any(StudyPlanEntity.class))).thenAnswer(i -> i.getArgument(0));

        StudyPlanResponseDto response = studyPlanService.createStudyPlan(dto);

        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo("PLAN-TAP-2025");
        assertThat(response.getStatus()).isEqualTo(StudyPlanStatus.NOT_CURRENT);
    }

    @Test
    @DisplayName("Should create study plan as CURRENT and demote previous current plan when setCurrent is true")
    void createStudyPlan_SetCurrentTrue_DemotesPreviousCurrent() {
        StudyPlanCreateDto dto = new StudyPlanCreateDto();
        dto.setCode("PLAN-TAP-2026");
        dto.setCareerCode("TAP");
        dto.setName("Plan 2026");
        dto.setSetCurrent(true);

        CareerEntity career = CareerEntity.builder().code("TAP").build();
        StudyPlanEntity previousCurrent = StudyPlanEntity.builder()
                .code("PLAN-TAP-2024")
                .careerCode("TAP")
                .status(StudyPlanStatus.CURRENT)
                .build();

        when(studyPlanRepository.findByCode("PLAN-TAP-2026")).thenReturn(Optional.empty());
        when(careerRepository.findByCode("TAP")).thenReturn(Optional.of(career));
        when(studyPlanRepository.findByCareerCodeAndStatus("TAP", StudyPlanStatus.CURRENT))
                .thenReturn(Optional.of(previousCurrent));
        when(studyPlanRepository.save(any(StudyPlanEntity.class))).thenAnswer(i -> i.getArgument(0));

        StudyPlanResponseDto response = studyPlanService.createStudyPlan(dto);

        assertThat(response.getStatus()).isEqualTo(StudyPlanStatus.CURRENT);
        assertThat(previousCurrent.getStatus()).isEqualTo(StudyPlanStatus.NOT_CURRENT);
        verify(studyPlanRepository).save(previousCurrent);
    }

    @Test
    @DisplayName("Should throw exception when study plan code already exists")
    void createStudyPlan_DuplicateCode_ThrowsException() {
        StudyPlanCreateDto dto = new StudyPlanCreateDto();
        dto.setCode("PLAN-DUPLICATE");

        when(studyPlanRepository.findByCode("PLAN-DUPLICATE"))
                .thenReturn(Optional.of(StudyPlanEntity.builder().code("PLAN-DUPLICATE").build()));

        assertThatThrownBy(() -> studyPlanService.createStudyPlan(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El código de plan de estudios ya existe.");
    }

    @Test
    @DisplayName("Should throw exception when associated career does not exist")
    void createStudyPlan_CareerNotFound_ThrowsException() {
        StudyPlanCreateDto dto = new StudyPlanCreateDto();
        dto.setCode("PLAN-X");
        dto.setCareerCode("NON_EXIST");

        when(studyPlanRepository.findByCode("PLAN-X")).thenReturn(Optional.empty());
        when(careerRepository.findByCode("NON_EXIST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studyPlanService.createStudyPlan(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La carrera asociada no existe.");
    }

    @Test
    @DisplayName("Should set plan as CURRENT and demote previous current plan")
    void setPlanAsCurrent_Success() {
        StudyPlanEntity targetPlan = StudyPlanEntity.builder()
                .code("PLAN-TAP-2025")
                .careerCode("TAP")
                .status(StudyPlanStatus.NOT_CURRENT)
                .build();

        StudyPlanEntity oldCurrent = StudyPlanEntity.builder()
                .code("PLAN-TAP-2024")
                .careerCode("TAP")
                .status(StudyPlanStatus.CURRENT)
                .build();

        when(studyPlanRepository.findByCode("PLAN-TAP-2025")).thenReturn(Optional.of(targetPlan));
        when(studyPlanRepository.findByCareerCodeAndStatus("TAP", StudyPlanStatus.CURRENT)).thenReturn(Optional.of(oldCurrent));

        StudyPlanResponseDto response = studyPlanService.setPlanAsCurrent("PLAN-TAP-2025");

        assertThat(response.getStatus()).isEqualTo(StudyPlanStatus.CURRENT);
        assertThat(oldCurrent.getStatus()).isEqualTo(StudyPlanStatus.NOT_CURRENT);
        verify(studyPlanRepository).save(oldCurrent);
        verify(studyPlanRepository).save(targetPlan);
    }

    @Test
    @DisplayName("Should return unchanged if plan is already CURRENT")
    void setPlanAsCurrent_AlreadyCurrent() {
        StudyPlanEntity targetPlan = StudyPlanEntity.builder()
                .code("PLAN-TAP-2024")
                .careerCode("TAP")
                .status(StudyPlanStatus.CURRENT)
                .build();

        when(studyPlanRepository.findByCode("PLAN-TAP-2024")).thenReturn(Optional.of(targetPlan));

        StudyPlanResponseDto response = studyPlanService.setPlanAsCurrent("PLAN-TAP-2024");

        assertThat(response.getStatus()).isEqualTo(StudyPlanStatus.CURRENT);
        verify(studyPlanRepository, never()).findByCareerCodeAndStatus(anyString(), any());
    }

    @Test
    @DisplayName("Should throw exception when setting a non-existent plan as current")
    void setPlanAsCurrent_NotFound_ThrowsException() {
        when(studyPlanRepository.findByCode("MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studyPlanService.setPlanAsCurrent("MISSING"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El plan de estudios no existe");
    }

    @Test
    @DisplayName("Should set a plan as current when no previous current plan exists")
    void setPlanAsCurrent_WithoutPreviousCurrent_Success() {
        StudyPlanEntity plan = StudyPlanEntity.builder()
                .code("PLAN-TAP-2025")
                .careerCode("TAP")
                .status(StudyPlanStatus.NOT_CURRENT)
                .build();

        when(studyPlanRepository.findByCode("PLAN-TAP-2025")).thenReturn(Optional.of(plan));
        when(studyPlanRepository.findByCareerCodeAndStatus("TAP", StudyPlanStatus.CURRENT))
                .thenReturn(Optional.empty());

        StudyPlanResponseDto response = studyPlanService.setPlanAsCurrent("PLAN-TAP-2025");

        assertThat(response.getStatus()).isEqualTo(StudyPlanStatus.CURRENT);
        verify(studyPlanRepository).save(plan);
    }

    @Test
    @DisplayName("Should delete study plan when no students or courses are associated")
    void deleteStudyPlan_Success() {
        StudyPlanEntity plan = StudyPlanEntity.builder().code("PLAN-TEST").build();

        when(studyPlanRepository.findByCode("PLAN-TEST")).thenReturn(Optional.of(plan));
        when(studentRepository.existsByStudyPlanCode("PLAN-TEST")).thenReturn(false);
        when(courseRepository.existsByStudyPlanCode("PLAN-TEST")).thenReturn(false);

        studyPlanService.deleteStudyPlan("PLAN-TEST");

        verify(studyPlanRepository).delete(plan);
    }

    @Test
    @DisplayName("Should throw exception when deleting study plan with associated students")
    void deleteStudyPlan_WithStudents_ThrowsException() {
        StudyPlanEntity plan = StudyPlanEntity.builder().code("PLAN-TEST").build();

        when(studyPlanRepository.findByCode("PLAN-TEST")).thenReturn(Optional.of(plan));
        when(studentRepository.existsByStudyPlanCode("PLAN-TEST")).thenReturn(true);

        assertThatThrownBy(() -> studyPlanService.deleteStudyPlan("PLAN-TEST"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se puede eliminar un plan de estudios que posee estudiantes");

        verify(studyPlanRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw exception when deleting study plan with associated courses")
    void deleteStudyPlan_WithCourses_ThrowsException() {
        StudyPlanEntity plan = StudyPlanEntity.builder().code("PLAN-TEST").build();

        when(studyPlanRepository.findByCode("PLAN-TEST")).thenReturn(Optional.of(plan));
        when(studentRepository.existsByStudyPlanCode("PLAN-TEST")).thenReturn(false);
        when(courseRepository.existsByStudyPlanCode("PLAN-TEST")).thenReturn(true);

        assertThatThrownBy(() -> studyPlanService.deleteStudyPlan("PLAN-TEST"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se puede eliminar un plan de estudios que posee asignaturas");

        verify(studyPlanRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw exception when deleting a non-existent study plan")
    void deleteStudyPlan_NotFound_ThrowsException() {
        when(studyPlanRepository.findByCode("MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studyPlanService.deleteStudyPlan("MISSING"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El plan de estudios no existe");
    }

    @Test
    @DisplayName("Should get plans by career code")
    void getPlansByCareer_Success() {
        StudyPlanEntity p1 = StudyPlanEntity.builder().code("PLAN-1").careerCode("TAP").build();
        when(studyPlanRepository.findAllByCareerCode("TAP")).thenReturn(List.of(p1));

        List<StudyPlanResponseDto> list = studyPlanService.getPlansByCareer("TAP");

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getCode()).isEqualTo("PLAN-1");
    }

    @Test
    @DisplayName("Should return all study plans")
    void getAllStudyPlans_Success() {
        StudyPlanEntity plan = StudyPlanEntity.builder()
                .code("PLAN-1")
                .careerCode("TAP")
                .name("Plan 2024")
                .status(StudyPlanStatus.CURRENT)
                .build();
        when(studyPlanRepository.findAll()).thenReturn(List.of(plan));

        List<StudyPlanResponseDto> result = studyPlanService.getAllStudyPlans();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("PLAN-1");
    }

    @Test
    @DisplayName("Should get plan by code successfully")
    void getPlanByCode_Success() {
        StudyPlanEntity p1 = StudyPlanEntity.builder().code("PLAN-1").build();
        when(studyPlanRepository.findByCode("PLAN-1")).thenReturn(Optional.of(p1));

        StudyPlanResponseDto res = studyPlanService.getPlanByCode("PLAN-1");

        assertThat(res).isNotNull();
        assertThat(res.getCode()).isEqualTo("PLAN-1");
    }

    @Test
    @DisplayName("Should throw exception when study plan code is not found")
    void getPlanByCode_NotFound_ThrowsException() {
        when(studyPlanRepository.findByCode("MISSING")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studyPlanService.getPlanByCode("MISSING"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Plan de estudios no encontrado");
    }
}
