package com.lending.dar360UserService.user.service.impl;

import com.lending.dar360UserService.user.model.Department;
import com.lending.dar360UserService.user.repository.DepartmentRepository;
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
