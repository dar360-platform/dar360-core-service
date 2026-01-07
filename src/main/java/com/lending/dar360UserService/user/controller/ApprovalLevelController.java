package com.lending.dar360UserService.user.controller;

import com.lending.dar360UserService.user.model.ApprovalLevel;
import com.lending.dar360UserService.user.service.ApprovalLevelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping({"/approval-levels", "/approvals"})
@RequiredArgsConstructor
@Tag(name = "Approval Level", description = "the Approval Level API")
public class ApprovalLevelController {

  private final ApprovalLevelService approvalLevelService;


  @GetMapping("")
  @Operation(summary = "Get all approval levels", description = "Get all approval levels")
  public ResponseEntity<List<ApprovalLevel>> getAllApprovalLevel() {
    return ResponseEntity.ok(approvalLevelService.getAllApprovalLevel());
  }
}
