package cl.siga.siga_ipt.service;

import cl.siga.siga_ipt.dto.TeacherCreateDto;
import cl.siga.siga_ipt.dto.TeacherResponseDto;
import cl.siga.siga_ipt.dto.TeacherUpdateDto;
import cl.siga.siga_ipt.entity.TeacherEntity;
import cl.siga.siga_ipt.enums.PeriodStatus;
import cl.siga.siga_ipt.enums.TeacherStatus;
import cl.siga.siga_ipt.repository.AcademicHistoryRepository;
import cl.siga.siga_ipt.repository.AcademicPeriodRepository;
import cl.siga.siga_ipt.repository.SectionRepository;
import cl.siga.siga_ipt.repository.StudentRepository;
import cl.siga.siga_ipt.repository.TeacherRepository;
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
class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private SectionRepository sectionRepository;

    @Mock
    private AcademicPeriodRepository periodRepository;

    @Mock
    private AcademicHistoryRepository academicHistoryRepository;

    @Mock
    private KeycloakAdminService keycloakAdminService;

    @InjectMocks
    private TeacherService teacherService;

    private TeacherCreateDto buildValidTeacherCreateDto() {
        TeacherCreateDto dto = new TeacherCreateDto();
        dto.setRun("11111111-1");
        dto.setFullName("Profesor Turing");
        dto.setEmail("alan.turing@docentes.ipt.cl");
        dto.setProfessionalTitle("Ingeniero en Informática");
        dto.setAcademicDegree("Doctor en Ciencias de la Computación");
        return dto;
    }

    @Test
    @DisplayName("Should create teacher successfully with ACTIVE status")
    void createTeacher_Success() {
        TeacherCreateDto dto = buildValidTeacherCreateDto();

        when(teacherRepository.existsByRun("11111111-1")).thenReturn(false);
        when(teacherRepository.existsByEmail("alan.turing@docentes.ipt.cl")).thenReturn(false);
        when(teacherRepository.save(any(TeacherEntity.class))).thenAnswer(i -> i.getArgument(0));

        TeacherResponseDto res = teacherService.createTeacher(dto);

        assertThat(res).isNotNull();
        assertThat(res.getRun()).isEqualTo("11111111-1");
        assertThat(res.getStatus()).isEqualTo(TeacherStatus.ACTIVE);
        assertThat(res.getProfessionalTitle()).isEqualTo("Ingeniero en Informática");
        verify(teacherRepository).save(any(TeacherEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when teacher RUN already exists")
    void createTeacher_DuplicateRun_ThrowsException() {
        TeacherCreateDto dto = buildValidTeacherCreateDto();
        when(teacherRepository.existsByRun("11111111-1")).thenReturn(true);

        assertThatThrownBy(() -> teacherService.createTeacher(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un docente registrado con el RUN");
    }

    @Test
    @DisplayName("Should reject a RUN already used by a student")
    void createTeacher_RunUsedByStudent_ThrowsException() {
        TeacherCreateDto dto = buildValidTeacherCreateDto();
        when(teacherRepository.existsByRun("11111111-1")).thenReturn(false);
        when(studentRepository.existsByRun("11111111-1")).thenReturn(true);

        assertThatThrownBy(() -> teacherService.createTeacher(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("registrado como estudiante");
    }

    @Test
    @DisplayName("Should throw exception when teacher email already exists")
    void createTeacher_DuplicateEmail_ThrowsException() {
        TeacherCreateDto dto = buildValidTeacherCreateDto();
        when(teacherRepository.existsByRun("11111111-1")).thenReturn(false);
        when(teacherRepository.existsByEmail("alan.turing@docentes.ipt.cl")).thenReturn(true);

        assertThatThrownBy(() -> teacherService.createTeacher(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un docente registrado con el correo");
    }

    @Test
    @DisplayName("Should update teacher fields successfully")
    void updateTeacher_Success() {
        TeacherEntity teacher = TeacherEntity.builder()
                .id(1L)
                .run("11111111-1")
                .fullName("Profesor Turing")
                .email("alan@ipt.cl")
                .professionalTitle("Ingeniero")
                .academicDegree("Master")
                .status(TeacherStatus.ACTIVE)
                .build();

        TeacherUpdateDto updateDto = new TeacherUpdateDto();
        updateDto.setRun("22222222-2");
        updateDto.setFullName("Alan Mathison Turing");
        updateDto.setEmail("alan.new@ipt.cl");
        updateDto.setProfessionalTitle("Ingeniero Senior");
        updateDto.setAcademicDegree("PhD");

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(teacherRepository.existsByRunAndIdNot("22222222-2", 1L)).thenReturn(false);
        when(teacherRepository.existsByEmailAndIdNot("alan.new@ipt.cl", 1L)).thenReturn(false);
        when(teacherRepository.save(any(TeacherEntity.class))).thenAnswer(i -> i.getArgument(0));

        TeacherResponseDto res = teacherService.updateTeacher(1L, updateDto);

        assertThat(res.getRun()).isEqualTo("22222222-2");
        assertThat(res.getFullName()).isEqualTo("Alan Mathison Turing");
        assertThat(res.getEmail()).isEqualTo("alan.new@ipt.cl");
        assertThat(res.getProfessionalTitle()).isEqualTo("Ingeniero Senior");
        assertThat(res.getAcademicDegree()).isEqualTo("PhD");
    }

    @Test
    @DisplayName("Should throw exception when updating RUN to one already taken by another teacher")
    void updateTeacher_DuplicateRun_ThrowsException() {
        TeacherEntity teacher = TeacherEntity.builder().id(1L).run("11111111-1").build();
        TeacherUpdateDto updateDto = new TeacherUpdateDto();
        updateDto.setRun("22222222-2");

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(teacherRepository.existsByRunAndIdNot("22222222-2", 1L)).thenReturn(true);

        assertThatThrownBy(() -> teacherService.updateTeacher(1L, updateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("El run proporcionado es de otro docente");
    }

    @Test
    @DisplayName("Should change teacher status to ACTIVE without restrictions")
    void changeTeacherStatus_ToActive_Success() {
        TeacherEntity teacher = TeacherEntity.builder().id(1L).status(TeacherStatus.INACTIVE).build();

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(teacherRepository.save(any(TeacherEntity.class))).thenAnswer(i -> i.getArgument(0));

        TeacherResponseDto res = teacherService.changeTeacherStatus(1L, TeacherStatus.ACTIVE);

        assertThat(res.getStatus()).isEqualTo(TeacherStatus.ACTIVE);
    }

    @Test
    @DisplayName("Should allow changing status to INACTIVE when no sections in OPEN periods exist")
    void changeTeacherStatus_ToInactive_Success() {
        TeacherEntity teacher = TeacherEntity.builder().id(1L).status(TeacherStatus.ACTIVE).build();

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(sectionRepository.findAll()).thenReturn(Collections.emptyList());
        when(teacherRepository.save(any(TeacherEntity.class))).thenAnswer(i -> i.getArgument(0));

        TeacherResponseDto res = teacherService.changeTeacherStatus(1L, TeacherStatus.INACTIVE);

        assertThat(res.getStatus()).isEqualTo(TeacherStatus.INACTIVE);
    }

    @Test
    @DisplayName("Should throw exception when inactivating teacher with sections in OPEN periods")
    void changeTeacherStatus_ToInactive_WithOpenPeriodSections_ThrowsException() {
        TeacherEntity teacher = TeacherEntity.builder().id(1L).status(TeacherStatus.ACTIVE).build();
        cl.siga.siga_ipt.entity.SectionEntity section = cl.siga.siga_ipt.entity.SectionEntity.builder()
                .teacherId(1L)
                .periodCode("2027-1")
                .build();
        cl.siga.siga_ipt.entity.AcademicPeriodEntity period = cl.siga.siga_ipt.entity.AcademicPeriodEntity.builder()
                .periodCode("2027-1")
                .status(PeriodStatus.OPEN)
                .build();

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(sectionRepository.findAll()).thenReturn(List.of(section));
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(period));

        assertThatThrownBy(() -> teacherService.changeTeacherStatus(1L, TeacherStatus.INACTIVE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tiene secciones asignadas en un período académico ABIERTO");
    }

    @Test
    @DisplayName("Should delete teacher when no sections and no academic history exist")
    void deleteTeacher_Success() {
        TeacherEntity teacher = TeacherEntity.builder().id(1L).run("11111111-1").build();

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(sectionRepository.existsByTeacherId(1L)).thenReturn(false);
        when(academicHistoryRepository.existsByTeacherRun("11111111-1")).thenReturn(false);

        teacherService.deleteTeacher(1L);

        verify(teacherRepository).delete(teacher);
    }

    @Test
    @DisplayName("Should throw exception when deleting teacher with associated sections")
    void deleteTeacher_WithSections_ThrowsException() {
        TeacherEntity teacher = TeacherEntity.builder().id(1L).run("11111111-1").build();

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(sectionRepository.existsByTeacherId(1L)).thenReturn(true);

        assertThatThrownBy(() -> teacherService.deleteTeacher(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tiene secciones asociadas");

        verify(teacherRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw exception when deleting teacher with academic history")
    void deleteTeacher_WithAcademicHistory_ThrowsException() {
        TeacherEntity teacher = TeacherEntity.builder().id(1L).run("11111111-1").build();

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(sectionRepository.existsByTeacherId(1L)).thenReturn(false);
        when(academicHistoryRepository.existsByTeacherRun("11111111-1")).thenReturn(true);

        assertThatThrownBy(() -> teacherService.deleteTeacher(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("registra historial académico");

        verify(teacherRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should get teacher by ID, RUN and list all")
    void teacherQueries_Success() {
        TeacherEntity teacher = TeacherEntity.builder().id(1L).run("11111111-1").status(TeacherStatus.ACTIVE).build();

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(teacherRepository.findByRun("11111111-1")).thenReturn(Optional.of(teacher));
        when(teacherRepository.findAll()).thenReturn(List.of(teacher));
        when(teacherRepository.findByStatus(TeacherStatus.ACTIVE)).thenReturn(List.of(teacher));

        assertThat(teacherService.getTeacherById(1L)).isNotNull();
        assertThat(teacherService.getTeacherByRun("11111111-1")).isNotNull();
        assertThat(teacherService.getAllTeachers(null)).hasSize(1);
        assertThat(teacherService.getAllTeachers(TeacherStatus.ACTIVE)).hasSize(1);
    }

    @Test
    @DisplayName("Should throw exception when teacher not found by ID or RUN")
    void teacherQueries_NotFound_ThrowsException() {
        when(teacherRepository.findById(999L)).thenReturn(Optional.empty());
        when(teacherRepository.findByRun("999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teacherService.getTeacherById(999L))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> teacherService.getTeacherByRun("999"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should throw exception when updating teacher email to one taken by another teacher")
    void updateTeacher_DuplicateEmail_ThrowsException() {
        TeacherEntity teacher = TeacherEntity.builder().id(1L).run("11111111-1").build();
        TeacherUpdateDto updateDto = new TeacherUpdateDto();
        updateDto.setEmail("taken@docentes.ipt.cl");

        when(teacherRepository.findById(1L)).thenReturn(Optional.of(teacher));
        when(teacherRepository.existsByEmailAndIdNot("taken@docentes.ipt.cl", 1L)).thenReturn(true);

        assertThatThrownBy(() -> teacherService.updateTeacher(1L, updateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya pertenece a otro docente");
    }
}
