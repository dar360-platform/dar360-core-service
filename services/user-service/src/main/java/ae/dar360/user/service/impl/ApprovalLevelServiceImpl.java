package ae.dar360.user.service.impl;

import ae.dar360.user.model.ApprovalLevel;
import ae.dar360.user.repository.ApprovalLevelRepository;
import ae.dar360.user.service.ApprovalLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApprovalLevelServiceImpl implements ApprovalLevelService {

    private final ApprovalLevelRepository approvalLevelRepository;

    @Override
    public List<ApprovalLevel> getAllApprovalLevel() {
        return approvalLevelRepository.findAll();
    }
}
