package com.dermavet.backend.service;

import com.dermavet.backend.dto.DoctorDTO;
import com.dermavet.backend.repository.DoctorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DoctorService {

    private final DoctorRepository repository;

    public DoctorService(DoctorRepository repository) {
        this.repository = repository;
    }

    // readOnly = true jer samo citamo - i drzi transakciju otvorenu dok se lazy 'usluge' mapiraju u DTO
    @Transactional(readOnly = true)
    public List<DoctorDTO> getSviLekari() {
        return repository.findAll().stream()
                .map(DoctorDTO::new)
                .toList();
    }
}
