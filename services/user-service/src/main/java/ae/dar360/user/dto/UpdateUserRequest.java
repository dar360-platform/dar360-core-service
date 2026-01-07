package ae.dar360.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_ABSENT)
public record UpdateUserRequest(
        @Schema(description = "Full name of the user", example = "John Emo")
        Optional<String> fullName,

        @Schema(description = "Employee identifier of the user", example = "EMP-001")
        Optional<String> employeeId,

        @Schema(description = "Identifier of the Department", example = "0013db76-92c6-466f-84da-ee4e83ffcd90")
        Optional<UUID> departmentId,

        @Schema(description = "Role identifiers assigned to the user")
        Optional<Set<String>> roleIds,

        @Schema(description = "Email address of the user", example = "john.emo@domain.com")
        Optional<String> email,

        @Schema(description = "Line manager email address", example = "manager@appro.ae")
        Optional<String> lineManagerEmail,

        @Schema(description = "Mobile number in E.164 format", example = "+971501234567")
        Optional<String> mobileNumber
) {

    public UpdateUserRequest {
        fullName = normaliseString(fullName);
        employeeId = normaliseString(employeeId);
        email = normaliseString(email);
        lineManagerEmail = normaliseString(lineManagerEmail);
        mobileNumber = normaliseString(mobileNumber);
        roleIds = normaliseSet(roleIds);
        departmentId = departmentId == null ? Optional.empty() : departmentId;
    }

    public UpdateUserRequest(
            Optional<String> fullName,
            Optional<String> employeeId,
            Optional<UUID> departmentId,
            Optional<Set<String>> roleIds,
            Optional<String> email,
            Optional<String> mobileNumber
    ) {
        this(fullName, employeeId, departmentId, roleIds, email, Optional.empty(), mobileNumber);
    }

    private static Optional<String> normaliseString(Optional<String> value) {
        if (value == null) {
            return Optional.empty();
        }
        return value.map(String::trim);
    }

    private static Optional<Set<String>> normaliseSet(Optional<Set<String>> values) {
        if (values == null || values.isEmpty()) {
            return Optional.empty();
        }
        Set<String> trimmed =
                values.get().stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toCollection(LinkedHashSet::new));
        return trimmed.isEmpty() ? Optional.empty() : Optional.of(trimmed);
    }
}
