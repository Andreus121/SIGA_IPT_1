package cl.siga.siga_ipt.service;

import cl.siga.siga_ipt.dto.AcademicPeriodCreateDto;
import cl.siga.siga_ipt.dto.AcademicPeriodResponseDto;
import cl.siga.siga_ipt.entity.AcademicPeriodEntity;
import cl.siga.siga_ipt.enums.PeriodStatus;
import cl.siga.siga_ipt.repository.AcademicPeriodRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcademicPeriodServiceTest {

    @Mock
    private AcademicPeriodRepository periodRepository;

    @InjectMocks
    private AcademicPeriodService periodService;

    @Test
    @DisplayName("Should create academic period successfully")
    void createPeriod_Success() {
        AcademicPeriodCreateDto createDto = AcademicPeriodCreateDto.builder()
                .periodCode("2027-1")
                .status(PeriodStatus.OPEN)
                .build();

        AcademicPeriodEntity savedEntity = AcademicPeriodEntity.builder()
                .id(1L)
                .periodCode("2027-1")
                .academicPeriod("2027-1")
                .status(PeriodStatus.OPEN)
                .build();

        when(periodRepository.save(any(AcademicPeriodEntity.class))).thenReturn(savedEntity);

        AcademicPeriodResponseDto response = periodService.createPeriod(createDto);

        assertThat(response).isNotNull();
        assertThat(response.getPeriodCode()).isEqualTo("2027-1");
        assertThat(response.getStatus()).isEqualTo(PeriodStatus.OPEN);
        verify(periodRepository).save(any(AcademicPeriodEntity.class));
    }

    @Test
    @DisplayName("Debe rechazar un período con código duplicado")
    void createPeriod_DuplicateCode() {
        AcademicPeriodCreateDto createDto = AcademicPeriodCreateDto.builder()
                .periodCode("2027-1")
                .status(PeriodStatus.OPEN)
                .build();
        when(periodRepository.existsByPeriodCode("2027-1")).thenReturn(true);

        assertThatThrownBy(() -> periodService.createPeriod(createDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un período académico");

        verify(periodRepository, never()).save(any(AcademicPeriodEntity.class));
    }

    @Test
    @DisplayName("Should get academic period by code successfully")
    void getByCode_Success() {
        AcademicPeriodEntity entity = AcademicPeriodEntity.builder()
                .id(1L)
                .periodCode("2027-1")
                .academicPeriod("2027-1")
                .status(PeriodStatus.OPEN)
                .build();

        when(periodRepository.findByPeriodCode("2027-1")).thenReturn(Optional.of(entity));

        AcademicPeriodResponseDto response = periodService.getByCode("2027-1");

        assertThat(response).isNotNull();
        assertThat(response.getPeriodCode()).isEqualTo("2027-1");
        assertThat(response.getStatus()).isEqualTo(PeriodStatus.OPEN);
        verify(periodRepository).findByPeriodCode("2027-1");
    }

    @Test
    @DisplayName("Should throw exception when academic period not found by code")
    void getByCode_NotFound() {
        when(periodRepository.findByPeriodCode("9999-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> periodService.getByCode("9999-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Periodo no encontrado");
    }

    @Test
    @DisplayName("Should return all academic periods")
    void getAllPeriods_Success() {
        AcademicPeriodEntity p1 = AcademicPeriodEntity.builder()
                .periodCode("2027-1")
                .status(PeriodStatus.OPEN)
                .build();
        AcademicPeriodEntity p2 = AcademicPeriodEntity.builder()
                .periodCode("2026-2")
                .status(PeriodStatus.CLOSED)
                .build();

        when(periodRepository.findAll()).thenReturn(List.of(p1, p2));

        List<AcademicPeriodResponseDto> result = periodService.getAllPeriods();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getPeriodCode()).isEqualTo("2027-1");
        assertThat(result.get(1).getPeriodCode()).isEqualTo("2026-2");
        verify(periodRepository).findAll();
    }
}
