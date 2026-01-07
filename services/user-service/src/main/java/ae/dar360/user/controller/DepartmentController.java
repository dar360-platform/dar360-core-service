package ae.dar360.user.controller;

import ae.dar360.user.dto.DepartmentRequest;
import ae.dar360.user.model.Department;
import ae.dar360.user.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
@Tag(name = "Department", description = "the Department API")
public class DepartmentController {

  private final DepartmentService departmentService;

  @PostMapping("")
  @Operation(summary = "Create department", description = "Create department")
  public ResponseEntity<Void> createDepartment(
      @RequestBody DepartmentRequest departmentRequest) {
    departmentService.createDepartment(departmentRequest.getName());
    return new ResponseEntity<>(HttpStatus.OK);
  }
  @GetMapping("")
  @Operation(summary = "Get all departments", description = "Get all departments")
  public ResponseEntity<List<Department>> getAllDepartment() {
    return ResponseEntity.ok(departmentService.getAllDepartment());
  }

  @GetMapping("/{departmentId}")
  @Operation(summary = "Get department by id", description = "Get department by id")
  public ResponseEntity<Department> getDepartmentById(@PathVariable("departmentId") UUID departmentId) {
    return departmentService.getDepartmentById(departmentId)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
  }
}
