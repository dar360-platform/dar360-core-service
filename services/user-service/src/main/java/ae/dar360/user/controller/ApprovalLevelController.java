package ae.dar360.user.controller;

import ae.dar360.user.model.ApprovalLevel;
import ae.dar360.user.service.ApprovalLevelService;
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
