package cl.siga.siga_ipt.service;

import cl.siga.siga_ipt.dto.CareerCreateDto;
import cl.siga.siga_ipt.dto.CareerResponseDto;
import cl.siga.siga_ipt.dto.CareerUpdateDto;
import cl.siga.siga_ipt.entity.CareerEntity;
import cl.siga.siga_ipt.enums.CareerStatus;
import cl.siga.siga_ipt.repository.CareerRepository;
import cl.siga.siga_ipt.repository.StudentRepository;
import cl.siga.siga_ipt.repository.StudyPlanRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CareerServiceTest {

    @Mock
    private CareerRepository careerRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private StudyPlanRepository studyPlanRepository;

    @InjectMocks
    private CareerService careerService;

    @Test
    @DisplayName("Should create career successfully when code is unique")
    void createCareer_Success() {
        CareerCreateDto dto = new CareerCreateDto();
        dto.setCode("TCS");
        dto.setName("Técnico en Ciberseguridad");
        dto.setDescription("Carrera técnica de ciberseguridad");

        when(careerRepository.existsByCode("TCS")).thenReturn(false);
        when(careerRepository.save(any(CareerEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CareerResponseDto response = careerService.createCareer(dto);

        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo("TCS");
        assertThat(response.getName()).isEqualTo("Técnico en Ciberseguridad");
        assertThat(response.getStatus()).isEqualTo(CareerStatus.ACTIVE);
        verify(careerRepository).save(any(CareerEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when creating career with already existing code")
    void createCareer_DuplicateCode_ThrowsException() {
        CareerCreateDto dto = new CareerCreateDto();
        dto.setCode("TAP");
        dto.setName("Técnico Analista Programador");

        when(careerRepository.existsByCode("TAP")).thenReturn(true);

        assertThatThrownBy(() -> careerService.createCareer(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe una carrera con este código");

        verify(careerRepository, never()).save(any(CareerEntity.class));
    }

    @Test
    @DisplayName("Should update existing career fields successfully")
    void updateCareer_Success() {
        CareerEntity existing = CareerEntity.builder()
                .id(1L)
                .code("TAD")
                .name("Técnico en Análisis de Datos")
                .description("Antigua descripción")
                .status(CareerStatus.ACTIVE)
                .build();

        CareerUpdateDto updateDto = new CareerUpdateDto();
        updateDto.setName("Técnico en Análisis de Datos e IA");
        updateDto.setDescription("Nueva descripción");
        updateDto.setStatus(CareerStatus.INACTIVE);

        when(careerRepository.findByCode("TAD")).thenReturn(Optional.of(existing));
        when(careerRepository.save(any(CareerEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CareerResponseDto response = careerService.updateCareer("TAD", updateDto);

        assertThat(response.getName()).isEqualTo("Técnico en Análisis de Datos e IA");
        assertThat(response.getDescription()).isEqualTo("Nueva descripción");
        assertThat(response.getStatus()).isEqualTo(CareerStatus.INACTIVE);
        verify(careerRepository).save(existing);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent career")
    void updateCareer_NotFound_ThrowsException() {
        CareerUpdateDto updateDto = new CareerUpdateDto();
        updateDto.setName("Nuevo");
        when(careerRepository.findByCode("NON_EXIST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> careerService.updateCareer("NON_EXIST", updateDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("La carrera especificada no existe.");
    }

    @Test
    @DisplayName("Should delete career successfully when no students or study plans are associated")
    void deleteCareer_Success() {
        CareerEntity existing = CareerEntity.builder()
                .id(1L)
                .code("TEST")
                .build();

        when(careerRepository.findByCode("TEST")).thenReturn(Optional.of(existing));
        when(studentRepository.existsByCareerCode("TEST")).thenReturn(false);
        when(studyPlanRepository.existsByCareerCode("TEST")).thenReturn(false);

        careerService.deleteCareer("TEST");

        verify(careerRepository).delete(existing);
    }

    @Test
    @DisplayName("Should throw exception when deleting career with associated students")
    void deleteCareer_WithStudents_ThrowsException() {
        CareerEntity existing = CareerEntity.builder().code("TAP").build();

        when(careerRepository.findByCode("TAP")).thenReturn(Optional.of(existing));
        when(studentRepository.existsByCareerCode("TAP")).thenReturn(true);

        assertThatThrownBy(() -> careerService.deleteCareer("TAP"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se puede eliminar físicamente una carrera con estudiantes");

        verify(careerRepository, never()).delete(any(CareerEntity.class));
    }

    @Test
    @DisplayName("Should throw exception when deleting career with associated study plans")
    void deleteCareer_WithStudyPlans_ThrowsException() {
        CareerEntity existing = CareerEntity.builder().code("TAP").build();

        when(careerRepository.findByCode("TAP")).thenReturn(Optional.of(existing));
        when(studentRepository.existsByCareerCode("TAP")).thenReturn(false);
        when(studyPlanRepository.existsByCareerCode("TAP")).thenReturn(true);

        assertThatThrownBy(() -> careerService.deleteCareer("TAP"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No se puede eliminar físicamente una carrera con estudiantes o planes de estudio");

        verify(careerRepository, never()).delete(any(CareerEntity.class));
    }

    @Test
    @DisplayName("Should get career by code successfully")
    void getCareerByCode_Success() {
        CareerEntity existing = CareerEntity.builder()
                .id(2L)
                .code("TCS")
                .name("Técnico en Ciberseguridad")
                .status(CareerStatus.ACTIVE)
                .build();

        when(careerRepository.findByCode("TCS")).thenReturn(Optional.of(existing));

        CareerResponseDto response = careerService.getCareerByCode("TCS");

        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo("TCS");
        assertThat(response.getName()).isEqualTo("Técnico en Ciberseguridad");
    }

    @Test
    @DisplayName("Should return all careers")
    void getAllCareers_Success() {
        CareerEntity c1 = CareerEntity.builder().code("TAP").build();
        CareerEntity c2 = CareerEntity.builder().code("TCS").build();

        when(careerRepository.findAll()).thenReturn(List.of(c1, c2));

        List<CareerResponseDto> list = careerService.getAllCareers();

        assertThat(list).hasSize(2);
        assertThat(list.get(0).getCode()).isEqualTo("TAP");
        assertThat(list.get(1).getCode()).isEqualTo("TCS");
    }
}
