package ae.dar360.user.service.impl;

import ae.dar360.user.model.ApprovalLevel;
import ae.dar360.user.repository.ApprovalLevelRepository;
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
