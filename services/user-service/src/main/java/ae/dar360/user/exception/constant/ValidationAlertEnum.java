package ae.dar360.user.exception.constant;

import ae.dar360.user.exception.dto.AlertCode;
import ae.dar360.user.exception.dto.IAlertCode;

import java.util.Optional;

public enum ValidationAlertEnum implements IAlertCode {
  USER_EXIST("user.exist", "user.exist"),
  EMPLOYEE_ID_EXISTED("employee.id.existed","employee.id.existed"),
  DEPARTMENT_NOT_EXIST("department.not.exist", "department.not.exist"),
  USER_NOT_EXIST("user.not.exist", "user.not.exist"),
  LOGIN_FAIL("login.fail", "login.fail"),
  USER_NOT_LOCK("user.not.lock", "user.not.lock"),
  USER_STATUS_NOT_MATCH("user.status.not.match", "user.status.not.match"),
  TOKEN_EXPIRED("token.expired", "token.expired"),
  TOKEN_NOT_EXIST("token.not.exist", "token.not.exist"),
  UNAUTHORIZED("unauthorized","unauthorized"),
  IEM113("IEM113", "IEM113"),
  IEM115("IEM115", "IEM115"),
  IEM016("IEM016", "IEM016"),
  TOKEN_NOT_EXPIRED("token.not.expired", "token.not.expired"),
  IEM112("IEM112", "IEM112"),
  IEM004("IEM004", "IEM004"),
  IEM117("IEM117", "IEM117"),
  IEM001("IEM001", "IEM001"),
  IEM037("IEM037", "IEM037"),
  IEM038("IEM038", "IEM038"),
  IEM119("IEM119", "IEM119"),
  IEM116("IEM116", "IEM116"),
  IEM020("IEM120", "IEM120"),
  INVALID_FULL_NAME("invalid.full.name", "invalid.full.name"),
  INVALID_EMAIL("invalid.email", "invalid.email"),
  INVALID_EMPLOYEE_ID("invalid.employee.id", "invalid.employee.id"),
  INVALID_MOBILE("invalid.mobile", "invalid.mobile"),
  INVALID_ROLE_ASSIGNMENT("invalid.role.assignment", "invalid.role.assignment"),
  ROLE_NOT_EXIST("role.not.exist", "role.not.exist"),
  IDENTIFIER_REQUIRED("identifier.required", "identifier.required"),
  PASSPORT_PHOTO_UPLOAD_FAILED("passport.photo.upload.failed", "passport.photo.upload.failed");
  private final AlertCode alertCode;

  ValidationAlertEnum(final String code, final String label) {
    this.alertCode = new AlertCode(code, label);
  }

  /**
   * get ErrorCode by label
   *
   * @param label
   * @return ErrorCode
   */
  public static Optional<ValidationAlertEnum> findByLabel(String label) {
    for (ValidationAlertEnum err : ValidationAlertEnum.values()) {
      if (err.getLabel().equals(label)) {
        return Optional.of(err);
      }
    }
    return Optional.empty();
  }

  @Override
  public String getCode() {
    return this.alertCode.getCode();
  }

  @Override
  public String getLabel() {
    return this.alertCode.getLabel();
  }
}
