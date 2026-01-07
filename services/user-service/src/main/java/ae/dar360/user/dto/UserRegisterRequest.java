package ae.dar360.user.dto;

import ae.dar360.user.enums.Dar360Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRegisterRequest {
    @NotBlank
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    private String mobile;

    @NotNull
    private Dar360Role role;

    private String agencyName; // Optional, for agents
    private String reraLicenseNumber; // Optional, for agents
}
