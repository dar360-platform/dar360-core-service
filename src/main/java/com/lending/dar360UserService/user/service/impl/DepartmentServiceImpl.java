package com.lending.dar360UserService.user.service.impl;

import com.lending.dar360UserService.user.model.Department;
import com.lending.dar360UserService.user.repository.DepartmentRepository;
import com.lending.dar360UserService.user.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    public Department createDepartment(String departmentName) {
        Department department = new Department();
        department.setName(departmentName);
        return departmentRepository.save(department);
    }

    @Override
    public List<Department> getAllDepartment() {
        return departmentRepository.findAll();
    }

    @Override
    public Optional<Department> getDepartmentById(UUID departmentId) {
        return departmentRepository.findById(departmentId);
    }
}
