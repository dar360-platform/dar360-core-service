package com.lending.dar360UserService.user.service.impl;

import com.lending.dar360UserService.user.model.ApprovalLevel;
import com.lending.dar360UserService.user.repository.ApprovalLevelRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;

public class ApprovalLevelServiceImplTest {
    @InjectMocks
    private ApprovalLevelServiceImpl approvalLevelService;

    @Mock
    private ApprovalLevelRepository approvalLevelRepository;

    @Before
    public void setup(){
        MockitoAnnotations.openMocks(this);
    }
    @Test
    public void testGetAllApprovalLevel(){
    Mockito.when(this.approvalLevelRepository.findAll()).thenReturn(List.of(new ApprovalLevel()));
        Assert.assertNotNull(this.approvalLevelService.getAllApprovalLevel());
    }

}
