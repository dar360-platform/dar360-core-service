package ae.dar360.user.service.impl;

import ae.dar360.user.model.Department;
import ae.dar360.user.repository.DepartmentRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;

public class DepartmentServiceImplTest {
  @InjectMocks private DepartmentServiceImpl departmentService;

  @Mock private DepartmentRepository departmentRepository;

  @Before
  public void setup() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testGetAllDepartment() {
    Mockito.when(this.departmentRepository.findAll()).thenReturn(List.of(new Department()));
    Assert.assertNotNull(this.departmentService.getAllDepartment());
  }

  @Test
  public void testCreateDepartment() {
    String departmentName = "department";
    Department entity = new Department();
    entity.setName(departmentName);
    Mockito.when(this.departmentRepository.save(Mockito.any())).thenReturn(entity);
    Assert.assertNotNull(this.departmentService.createDepartment(entity.getName()));
  }
}
