package ae.dar360.user.controller;

import ae.dar360.user.dto.*;
import ae.dar360.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void testSearchUser() throws Exception {
        SearchUserForm searchUserForm = new SearchUserForm();
        searchUserForm.setPageNumber(0);
        searchUserForm.setPageSize(10);

        Page<SearchUserResponseDto> page = new PageImpl<>(Collections.emptyList());
        when(userService.searchUser(any(SearchUserForm.class))).thenReturn(page);

        mockMvc.perform(post("/users/searchUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchUserForm)))
                .andExpect(status().isOk());

        Mockito.verify(userService).searchUser(any(SearchUserForm.class));
    }

    @Test
    void testGetAllUserExport() throws Exception {
        List<SearchUserResponseDto> users = Collections.emptyList();
        when(userService.getAllUserExport()).thenReturn(users);

        mockMvc.perform(get("/users/export"))
                .andExpect(status().isOk());

        Mockito.verify(userService).getAllUserExport();
    }

    @Test
    void testCreateUser() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("test@example.com");
        request.setEmployeeId("EMP001");
        request.setLineManagerEmail("manager@example.com");
        request.setMobile("+971501234567");
        request.setDepartmentId(UUID.randomUUID().toString());
        request.setApprovalLevels(Collections.singletonList("L1"));
        request.setProductTypes(Collections.singletonList("P1"));

        UserResponse response = new UserResponse();
        response.setId(UUID.randomUUID());
        response.setEmail("test@example.com");

        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        Mockito.verify(userService).createUser(any(CreateUserRequest.class));
    }

    @Test
    void testForgotPassword() throws Exception {
        ForgotPasswordDto dto = new ForgotPasswordDto();
        dto.setEmail("test@example.com");

        Mockito.doNothing().when(userService).forgotPassword(any(String.class));

        mockMvc.perform(post("/users/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        Mockito.verify(userService).forgotPassword(any(String.class));
    }

    @Test
    void testValidateTokenSession() throws Exception {
        String tokenSession = "test-token";

        when(userService.validateTokenSession(any(String.class))).thenReturn(null);

        mockMvc.perform(post("/users/password/validate-token-session/" + tokenSession))
                .andExpect(status().isOk());

        Mockito.verify(userService).validateTokenSession(any(String.class));
    }

    @Test
    void testIncreaseTokenVerifyTimes() throws Exception {
        String tokenSession = "test-token";

        Mockito.doNothing().when(userService).increaseTokenVerifyTimes(any(String.class));

        mockMvc.perform(post("/users/password/increase-token-verify-times/" + tokenSession))
                .andExpect(status().isOk());

        Mockito.verify(userService).increaseTokenVerifyTimes(any(String.class));
    }

    @Test
    void testRegenerateTokenSession() throws Exception {
        String tokenSession = "test-token";

        Mockito.doNothing().when(userService).regenerateTokenSession(any(String.class));

        mockMvc.perform(post("/users/password/regenerate-token-session/" + tokenSession))
                .andExpect(status().isOk());

        Mockito.verify(userService).regenerateTokenSession(any(String.class));
    }
}
