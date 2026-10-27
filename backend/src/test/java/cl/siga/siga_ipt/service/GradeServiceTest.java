package cl.siga.siga_ipt.service;

import cl.siga.siga_ipt.dto.GradeEntryDto;
import cl.siga.siga_ipt.dto.RegistrationResponseDto;
import cl.siga.siga_ipt.entity.AcademicPeriodEntity;
import cl.siga.siga_ipt.entity.RegistrationEntity;
import cl.siga.siga_ipt.enums.PeriodStatus;
import cl.siga.siga_ipt.enums.RegistrationStatus;
import cl.siga.siga_ipt.repository.AcademicPeriodRepository;
import cl.siga.siga_ipt.repository.RegistrationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {

    @Mock
    private RegistrationRepository registrationRepository;

    @Mock
    private AcademicPeriodRepository periodRepository;

    @InjectMocks
    private GradeService gradeService;

    private RegistrationEntity registration;
    private AcademicPeriodEntity period;
    private GradeEntryDto gradeDto;

    @BeforeEach
    void setUp() {
        period = AcademicPeriodEntity.builder()
                .id(1L)
                .periodCode("2027-1")
                .academicPeriod("2027-1")
                .status(PeriodStatus.OPEN)
                .build();

        registration = RegistrationEntity.builder()
                .id(10L)
                .studentRun("12345678-9")
                .courseCode("PROG1")
                .sectionCode("SEC-01")
                .periodCode("2027-1")
                .registrationDate(LocalDateTime.now())
                .status(RegistrationStatus.REGISTERED)
                .build();

        gradeDto = GradeEntryDto.builder()
                .registrationId(10L)
                .finalGrade(5.5)
                .build();
    }

    @Test
    @DisplayName("Debe registrar calificación final exitosamente en período OPEN")
    void testRegisterGrade_Success() {
        when(registrationRepository.findById(10L)).thenReturn(Optional.of(registration));
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(period));
        when(registrationRepository.save(any(RegistrationEntity.class))).thenAnswer(i -> i.getArgument(0));

        RegistrationResponseDto result = gradeService.registerGrade(gradeDto);

        assertThat(result).isNotNull();
        assertThat(result.getFinalGrade()).isEqualTo(5.5);
        verify(registrationRepository).save(registration);
    }

    @Test
    @DisplayName("Debe fallar si la inscripción no existe")
    void testRegisterGrade_RegistrationNotFound() {
        when(registrationRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gradeService.registerGrade(gradeDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Inscripción no encontrada");
    }

    @Test
    @DisplayName("Debe fallar si la inscripción no está activa (ej: CANCELED)")
    void testRegisterGrade_RegistrationNotActive() {
        registration.setStatus(RegistrationStatus.CANCELED);
        when(registrationRepository.findById(10L)).thenReturn(Optional.of(registration));

        assertThatThrownBy(() -> gradeService.registerGrade(gradeDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Solo se pueden registrar calificaciones en inscripciones activas");
    }

    @Test
    @DisplayName("Debe fallar si el período académico no existe")
    void testRegisterGrade_PeriodNotFound() {
        when(registrationRepository.findById(10L)).thenReturn(Optional.of(registration));
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gradeService.registerGrade(gradeDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no existe");
    }

    @Test
    @DisplayName("Debe fallar si el período académico está CLOSED")
    void testRegisterGrade_PeriodClosed() {
        period.setStatus(PeriodStatus.CLOSED);
        when(registrationRepository.findById(10L)).thenReturn(Optional.of(registration));
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(period));

        assertThatThrownBy(() -> gradeService.registerGrade(gradeDto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("períodos académicos con estado ABIERTO");
    }

    @Test
    @DisplayName("Debe fallar si la nota está fuera de rango (< 1.0 o > 7.0)")
    void testRegisterGrade_GradeOutOfRange() {
        when(registrationRepository.findById(10L)).thenReturn(Optional.of(registration));
        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(period));

        gradeDto.setFinalGrade(7.5);
        assertThatThrownBy(() -> gradeService.registerGrade(gradeDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("entre 1.0 y 7.0");

        gradeDto.setFinalGrade(0.9);
        assertThatThrownBy(() -> gradeService.registerGrade(gradeDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("entre 1.0 y 7.0");
    }

    @Test
    @DisplayName("Debe obtener notas por sección y período")
    void testGetGradesBySection() {
        registration.setFinalGrade(6.0);
        when(registrationRepository.findBySectionCodeAndPeriodCode("SEC-01", "2027-1"))
                .thenReturn(List.of(registration));

        List<RegistrationResponseDto> list = gradeService.getGradesBySection("SEC-01", "2027-1");

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getFinalGrade()).isEqualTo(6.0);
    }
}
