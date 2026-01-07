package com.lending.dar360UserService.user.service;

import com.lending.dar360UserService.user.model.Department;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepartmentService {

    Department createDepartment(String departmentName);

    List<Department> getAllDepartment();

    Optional<Department> getDepartmentById(UUID departmentId);
}
