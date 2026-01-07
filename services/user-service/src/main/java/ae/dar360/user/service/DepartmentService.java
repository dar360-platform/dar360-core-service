package ae.dar360.user.service;

import ae.dar360.user.model.Department;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DepartmentService {

    Department createDepartment(String departmentName);

    List<Department> getAllDepartment();

    Optional<Department> getDepartmentById(UUID departmentId);
}
