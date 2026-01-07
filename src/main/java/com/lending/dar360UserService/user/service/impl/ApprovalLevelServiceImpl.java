package com.lending.dar360UserService.user.service.impl;

import com.lending.dar360UserService.user.model.ApprovalLevel;
import com.lending.dar360UserService.user.repository.ApprovalLevelRepository;
import com.lending.dar360UserService.user.service.ApprovalLevelService;
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
