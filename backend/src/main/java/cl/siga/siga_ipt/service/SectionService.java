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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SectionService {

    private final SectionRepository sectionRepository;
    private final SectionScheduleRepository scheduleRepository;
    private final AcademicPeriodRepository periodRepository;
    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final RegistrationRepository registrationRepository;

    private static final Set<String> VALID_DAYS = Set.of("L", "M", "W", "J", "V");

    @Transactional
    public SectionResponseDto createSection(SectionCreateDto dto) {
        // Unicidad del código de sección
        if (sectionRepository.existsByCode(dto.getCode())) {
            throw new IllegalArgumentException("Ya existe una sección con el código: " + dto.getCode());
        }

        // Validar que el período académico exista y esté ABIERTO
        AcademicPeriodEntity period = periodRepository.findByPeriodCode(dto.getPeriodCode())
                .orElseThrow(() -> new IllegalArgumentException("El período académico '" + dto.getPeriodCode() + "' no existe."));

        if (period.getStatus() != PeriodStatus.OPEN) {
            throw new IllegalStateException("Solo se pueden crear secciones en un período académico con estado ABIERTO.");
        }

        // Validar existencia de la asignatura
        CourseEntity course = courseRepository.findFirstByCode(dto.getCourseCode())
                .orElseThrow(() -> new IllegalArgumentException("La asignatura con código '" + dto.getCourseCode() + "' no existe."));

        // Regla: Cada asignatura ofrecida tiene una única sección por período académico
        if (sectionRepository.existsByCourseCodeAndPeriodCode(dto.getCourseCode(), dto.getPeriodCode())) {
            throw new IllegalArgumentException("Ya existe una sección para la asignatura '" + dto.getCourseCode() +
                    "' en el período académico '" + dto.getPeriodCode() + "'.");
        }

        // Validar que el docente asignado exista y esté ACTIVO
        TeacherEntity teacher = teacherRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new IllegalArgumentException("El docente con ID " + dto.getTeacherId() + " no existe."));

        if (teacher.getStatus() != TeacherStatus.ACTIVE) {
            throw new IllegalStateException("Solo se pueden asignar docentes en estado ACTIVO a una sección.");
        }

        // Validar capacidad (1 a 60)
        validateCapacity(dto.getCapacity());

        // Validar horarios y correspondencia con horas TEL
        validateSchedules(dto.getSchedules(), course, teacher.getId(), dto.getPeriodCode(), null);

        // Crear y guardar SectionEntity
        SectionEntity section = SectionEntity.builder()
                .code(dto.getCode())
                .courseCode(dto.getCourseCode())
                .periodCode(dto.getPeriodCode())
                .teacherId(teacher.getId())
                .capacity(dto.getCapacity())
                .build();

        SectionEntity savedSection = sectionRepository.save(section);

        // Guardar bloques de horarios
        saveSchedules(savedSection.getCode(), savedSection.getPeriodCode(), dto.getSchedules());

        return mapToResponseDto(savedSection);
    }

    @Transactional
    public SectionResponseDto updateSection(Long id, SectionUpdateDto dto) {
        SectionEntity section = sectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sección no encontrada con el ID: " + id));

        // Validar que el período siga ABIERTO para hacer modificaciones
        AcademicPeriodEntity period = periodRepository.findByPeriodCode(section.getPeriodCode())
                .orElseThrow(() -> new IllegalArgumentException("El período académico no existe."));

        if (period.getStatus() != PeriodStatus.OPEN) {
            throw new IllegalStateException("No se pueden modificar secciones en un período académico CERRADO.");
        }

        CourseEntity course = courseRepository.findFirstByCode(section.getCourseCode())
                .orElseThrow(() -> new IllegalArgumentException("La asignatura asociada a la sección no existe."));

        Long teacherIdToUse = section.getTeacherId();

        // Si actualiza el docente
        if (dto.getTeacherId() != null && !dto.getTeacherId().equals(section.getTeacherId())) {
            TeacherEntity newTeacher = teacherRepository.findById(dto.getTeacherId())
                    .orElseThrow(() -> new IllegalArgumentException("El docente con ID " + dto.getTeacherId() + " no existe."));

            if (newTeacher.getStatus() != TeacherStatus.ACTIVE) {
                throw new IllegalStateException("Solo se pueden asignar docentes en estado ACTIVO a una sección.");
            }
            teacherIdToUse = newTeacher.getId();
            section.setTeacherId(teacherIdToUse);
        }

        // Si actualiza la capacidad
        if (dto.getCapacity() != null) {
            validateCapacity(dto.getCapacity());
            section.setCapacity(dto.getCapacity());
        }

        // Si actualiza horarios
        if (dto.getSchedules() != null) {
            validateSchedules(dto.getSchedules(), course, teacherIdToUse, section.getPeriodCode(), section.getCode());
            scheduleRepository.deleteBySectionCodeAndPeriodCode(section.getCode(), section.getPeriodCode());
            saveSchedules(section.getCode(), section.getPeriodCode(), dto.getSchedules());
        } else if (dto.getTeacherId() != null) {
            // Si solo cambió el docente pero mantuvo los horarios, validar que el nuevo docente no tenga conflictos con los horarios actuales
            List<SectionScheduleDto> currentSchedules = scheduleRepository.findBySectionCodeAndPeriodCode(section.getCode(), section.getPeriodCode())
                    .stream()
                    .map(s -> new SectionScheduleDto(s.getDay(), s.getModuleNumber()))
                    .toList();
            validateTeacherScheduleConflicts(currentSchedules, teacherIdToUse, section.getPeriodCode(), section.getCode());
        }

        SectionEntity updatedSection = sectionRepository.save(section);
        return mapToResponseDto(updatedSection);
    }

    @Transactional
    public void deleteSection(Long id) {
        SectionEntity section = sectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sección no encontrada con el ID: " + id));

        // Regla: No eliminable si tiene estudiantes inscritos
        if (registrationRepository.existsBySectionCodeAndPeriodCode(section.getCode(), section.getPeriodCode())
                || registrationRepository.existsBySectionCode(section.getCode())) {
            throw new IllegalStateException("No se puede eliminar la sección porque tiene estudiantes inscritos.");
        }

        scheduleRepository.deleteBySectionCodeAndPeriodCode(section.getCode(), section.getPeriodCode());
        sectionRepository.delete(section);
    }

    @Transactional(readOnly = true)
    public SectionResponseDto getSectionById(Long id) {
        SectionEntity section = sectionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sección no encontrada con el ID: " + id));
        return mapToResponseDto(section);
    }

    @Transactional(readOnly = true)
    public SectionResponseDto getSectionByCodeAndPeriod(String code, String periodCode) {
        SectionEntity section = sectionRepository.findByCodeAndPeriodCode(code, periodCode)
                .orElseThrow(() -> new IllegalArgumentException("Sección no encontrada con código: " + code + " y periodo: " + periodCode));
        return mapToResponseDto(section);
    }

    @Transactional(readOnly = true)
    public List<SectionResponseDto> getSectionsByPeriod(String periodCode) {
        return sectionRepository.findByPeriodCode(periodCode).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    private void validateCapacity(Integer capacity) {
        if (capacity == null || capacity < 1 || capacity > 60) {
            throw new IllegalArgumentException("La capacidad de la sección debe ser mayor que 0 y máximo 60 estudiantes.");
        }
    }

    private void validateSchedules(List<SectionScheduleDto> schedules, CourseEntity course, Long teacherId, String periodCode, String currentSectionCode) {
        if (schedules == null || schedules.isEmpty()) {
            throw new IllegalArgumentException("Debe asignar horarios a la sección.");
        }

        // Calcular total de horas pedagógicas TEL de la asignatura
        int totalTEL = (course.getTheoryHours() != null ? course.getTheoryHours() : 0)
                + (course.getExerciseHours() != null ? course.getExerciseHours() : 0)
                + (course.getLabHours() != null ? course.getLabHours() : 0);

        // 1 bloque pedagógico = 2 horas pedagógicas
        int expectedBlocks = (int) Math.ceil(totalTEL / 2.0);

        if (schedules.size() != expectedBlocks) {
            throw new IllegalArgumentException("La cantidad de bloques de horario (" + schedules.size() +
                    ") no coincide con las horas TEL de la asignatura (" + totalTEL + " horas TEL equivalen a " +
                    expectedBlocks + " bloques).");
        }

        // Verificar que no haya bloques duplicados en la misma sección y días/módulos válidos
        Set<String> uniqueSlots = new HashSet<>();
        for (SectionScheduleDto slot : schedules) {
            if (slot.getDay() == null || !VALID_DAYS.contains(slot.getDay())) {
                throw new IllegalArgumentException("Día de horario inválido: '" + slot.getDay() + "'. Valores válidos: L, M, W, J, V.");
            }
            if (slot.getModuleNumber() == null || slot.getModuleNumber() < 1 || slot.getModuleNumber() > 6) {
                throw new IllegalArgumentException("Módulo inválido: " + slot.getModuleNumber() + ". Debe ser entre 1 y 6 (M1 a M6).");
            }
            String key = slot.getDay() + "-" + slot.getModuleNumber();
            if (!uniqueSlots.add(key)) {
                throw new IllegalArgumentException("Existen bloques de horario duplicados en la sección para el día " +
                        slot.getDay() + " y módulo " + slot.getModuleNumber() + ".");
            }
        }

        // Validar tope de horario para el docente en el período
        validateTeacherScheduleConflicts(schedules, teacherId, periodCode, currentSectionCode);
    }

    private void validateTeacherScheduleConflicts(List<SectionScheduleDto> schedules, Long teacherId, String periodCode, String currentSectionCode) {
        // Obtener todas las secciones del docente en este período
        List<SectionEntity> teacherSections = sectionRepository.findByTeacherIdAndPeriodCode(teacherId, periodCode);

        for (SectionEntity otherSection : teacherSections) {
            // Ignorar la misma sección en caso de actualización
            if (currentSectionCode != null && otherSection.getCode().equals(currentSectionCode)) {
                continue;
            }

            List<SectionScheduleEntity> otherSchedules = scheduleRepository.findBySectionCodeAndPeriodCode(otherSection.getCode(), periodCode);
            for (SectionScheduleDto proposed : schedules) {
                for (SectionScheduleEntity occupied : otherSchedules) {
                    if (occupied.getDay().equalsIgnoreCase(proposed.getDay())
                            && occupied.getModuleNumber().equals(proposed.getModuleNumber())) {
                        throw new IllegalStateException("El docente ya tiene asignada la sección '" + otherSection.getCode() +
                                "' en el día " + proposed.getDay() + " y módulo M" + proposed.getModuleNumber() + ".");
                    }
                }
            }
        }
    }

    private void saveSchedules(String sectionCode, String periodCode, List<SectionScheduleDto> schedules) {
        for (SectionScheduleDto dto : schedules) {
            SectionScheduleEntity entity = SectionScheduleEntity.builder()
                    .sectionCode(sectionCode)
                    .periodCode(periodCode)
                    .day(dto.getDay())
                    .moduleNumber(dto.getModuleNumber())
                    .build();
            scheduleRepository.save(entity);
        }
    }

    private SectionResponseDto mapToResponseDto(SectionEntity entity) {
        List<SectionScheduleEntity> schedules = scheduleRepository.findBySectionCodeAndPeriodCode(entity.getCode(), entity.getPeriodCode());
        List<SectionScheduleDto> scheduleDtos = schedules.stream()
                .map(s -> SectionScheduleDto.builder()
                        .day(s.getDay())
                        .moduleNumber(s.getModuleNumber())
                        .build())
                .collect(Collectors.toList());

        return SectionResponseDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .courseCode(entity.getCourseCode())
                .periodCode(entity.getPeriodCode())
                .teacherId(entity.getTeacherId())
                .capacity(entity.getCapacity())
                .schedules(scheduleDtos)
                .build();
    }
}
