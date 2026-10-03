package cl.siga.siga_ipt.service;

import cl.siga.siga_ipt.dto.CareerCreateDto;
import cl.siga.siga_ipt.dto.CareerUpdateDto;
import cl.siga.siga_ipt.entity.CareerEntity;
import cl.siga.siga_ipt.enums.CareerStatus;
import cl.siga.siga_ipt.repository.CareerRepository;
import cl.siga.siga_ipt.repository.StudentRepository;
import cl.siga.siga_ipt.repository.StudyPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import cl.siga.siga_ipt.dto.CareerResponseDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CareerService {

    private final CareerRepository careerRepository;
    private final StudentRepository studentRepository;
    private final StudyPlanRepository studyPlanRepository;

    // Crear una carrera nueva (carrera sin plan de estudio)
    @Transactional
    public CareerResponseDto createCareer(CareerCreateDto dto){
        if (careerRepository.existsByCode(dto.getCode())){
            throw new IllegalArgumentException("Ya existe una carrera con este código");
        }

        // Crear la entidad de la carrera
        CareerEntity career = CareerEntity.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .description(dto.getDescription())
                .status(CareerStatus.ACTIVE)
                .build();

        // Guardar la carrera en la base de datos
        careerRepository.save(career);

        return mapCareerToResponseDto(career);
    }

    // Actualizar una carrera ya existente
    @Transactional
    public CareerResponseDto updateCareer(String code, CareerUpdateDto dto) {
        CareerEntity career = careerRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("La carrera especificada no existe."));

        // Cambiar los valores que no estén vacíos
        if (dto.getName() != null && !dto.getName().isBlank()) {
            career.setName(dto.getName());
        }
        if (dto.getDescription() != null && !dto.getDescription().isBlank()) {
            career.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            career.setStatus(dto.getStatus());
        }
        // Guardar carrera y retornar respuesta
        careerRepository.save(career);
        return mapCareerToResponseDto(career);
    }

    // Borrar una carrera
    @Transactional
    public void deleteCareer(String code) {
        CareerEntity career = careerRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("La carrera especificada no existe."));

        // Regla: No se puede eliminar si tiene estudiantes o planes de estudio asociados
        boolean hasStudents = studentRepository.existsByCareerCode(code);
        boolean hasStudyPlans = studyPlanRepository.existsByCareerCode(code);

        // Si tiene un estudiante o plan de estudio, no se elimina
        if (hasStudents || hasStudyPlans) {
            throw new IllegalStateException("No se puede eliminar físicamente una carrera con estudiantes o planes de estudio asociados. Modifique su estado a INACTIVA.");
        }

        // No tiene nada, se puede eliminar
        careerRepository.delete(career);
    }

    // Obtener una carrera por su código
    @Transactional(readOnly = true)
    public CareerResponseDto getCareerByCode(String code) {
        CareerEntity career = careerRepository.findByCode(code).
                orElseThrow(() -> new IllegalArgumentException("La carrera especificada no existe,"));

        return mapCareerToResponseDto(career);
    }

    // Obtener TODAS las carreras (consulta pesada)
    @Transactional(readOnly = true)
    public List<CareerResponseDto> getAllCareers() {
        return careerRepository.findAll().stream()
                .map(this::mapCareerToResponseDto)
                .toList();
    }

    // Funciones auxiliares
    private CareerResponseDto mapCareerToResponseDto(CareerEntity career){
        return CareerResponseDto.builder()
                .id(career.getId())
                .code(career.getCode())
                .name(career.getName())
                .description(career.getDescription())
                .status(career.getStatus())
                .build();
    }

}
