package com.example.testdeploycrud.controller;

import com.example.testdeploycrud.dto.UserCreateRequest;
import com.example.testdeploycrud.dto.UserResponse;
import com.example.testdeploycrud.dto.UserUpdateRequest;
import com.example.testdeploycrud.exception.BadRequestException;
import com.example.testdeploycrud.exception.GlobalExceptionHandler;
import com.example.testdeploycrud.exception.ResourceNotFoundException;
import com.example.testdeploycrud.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private UserResponse sampleResponse;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        sampleResponse = UserResponse.builder()
                .id(1L)
                .username("johndoe")
                .email("john@example.com")
                .fullName("John Doe")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/users")
    class CreateUserEndpoint {

        @Test
        @DisplayName("201 Created — berhasil membuat user")
        void create_Success() throws Exception {
            when(userService.createUser(any(UserCreateRequest.class))).thenReturn(sampleResponse);

            String requestJson = """
                    {
                        "username": "johndoe",
                        "email": "john@example.com",
                        "fullName": "John Doe"
                    }
                    """;

            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.status").value(201))
                    .andExpect(jsonPath("$.message").value("User created successfully"))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.username").value("johndoe"))
                    .andExpect(jsonPath("$.data.email").value("john@example.com"));

            verify(userService).createUser(any(UserCreateRequest.class));
        }

        @Test
        @DisplayName("400 Bad Request — username kosong")
        void create_BlankUsername_ValidationError() throws Exception {
            String requestJson = """
                    {
                        "username": "",
                        "email": "john@example.com",
                        "fullName": "John Doe"
                    }
                    """;

            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.status").value(400));

            verify(userService, never()).createUser(any());
        }

        @Test
        @DisplayName("400 Bad Request — email format salah")
        void create_InvalidEmail_ValidationError() throws Exception {
            String requestJson = """
                    {
                        "username": "johndoe",
                        "email": "bukan-email",
                        "fullName": "John Doe"
                    }
                    """;

            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(any());
        }

        @Test
        @DisplayName("400 Bad Request — username mengandung karakter spesial")
        void create_InvalidUsernamePattern_ValidationError() throws Exception {
            String requestJson = """
                    {
                        "username": "john_doe!@#",
                        "email": "john@example.com",
                        "fullName": "John Doe"
                    }
                    """;

            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(any());
        }

        @Test
        @DisplayName("400 Bad Request — username terlalu pendek")
        void create_UsernameTooShort_ValidationError() throws Exception {
            String requestJson = """
                    {
                        "username": "ab",
                        "email": "john@example.com",
                        "fullName": "John Doe"
                    }
                    """;

            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest());

            verify(userService, never()).createUser(any());
        }

        @Test
        @DisplayName("400 Bad Request — duplicate username (dari service)")
        void create_DuplicateUsername_BadRequest() throws Exception {
            when(userService.createUser(any())).thenThrow(
                    new BadRequestException("Username is already in use")
            );

            String requestJson = """
                    {
                        "username": "johndoe",
                        "email": "john@example.com",
                        "fullName": "John Doe"
                    }
                    """;

            mockMvc.perform(post("/api/v1/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Username is already in use"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/users/{userId}")
    class UpdateUserEndpoint {

        @Test
        @DisplayName("200 OK — berhasil update user")
        void update_Success() throws Exception {
            UserResponse updatedResponse = UserResponse.builder()
                    .id(1L)
                    .username("janedoe")
                    .email("john@example.com")
                    .fullName("John Doe")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(userService.updateUser(any(UserUpdateRequest.class), eq(1L)))
                    .thenReturn(updatedResponse);

            String requestJson = """
                    {
                        "username": "janedoe"
                    }
                    """;

            mockMvc.perform(patch("/api/v1/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User updated successfully"))
                    .andExpect(jsonPath("$.data.username").value("janedoe"));
        }

        @Test
        @DisplayName("404 Not Found — user tidak ditemukan")
        void update_UserNotFound() throws Exception {
            when(userService.updateUser(any(), eq(99L)))
                    .thenThrow(new ResourceNotFoundException("User with id 99 not found"));

            String requestJson = """
                    {
                        "username": "janedoe"
                    }
                    """;

            mockMvc.perform(patch("/api/v1/users/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("User with id 99 not found"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/users/{userId}")
    class DeleteUserEndpoint {

        @Test
        @DisplayName("200 OK — berhasil hapus user")
        void delete_Success() throws Exception {
            doNothing().when(userService).deleteUser(1L);

            mockMvc.perform(delete("/api/v1/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User deleted successfully"));

            verify(userService).deleteUser(1L);
        }

        @Test
        @DisplayName("404 Not Found — user tidak ditemukan")
        void delete_UserNotFound() throws Exception {
            doThrow(new ResourceNotFoundException("User with id 99 not found"))
                    .when(userService).deleteUser(99L);

            mockMvc.perform(delete("/api/v1/users/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("User with id 99 not found"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users/{userId}")
    class GetOneUserEndpoint {

        @Test
        @DisplayName("200 OK — berhasil ambil satu user")
        void getOne_Success() throws Exception {
            when(userService.getOneUser(1L)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/v1/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User retrieved successfully"))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.username").value("johndoe"))
                    .andExpect(jsonPath("$.data.email").value("john@example.com"))
                    .andExpect(jsonPath("$.data.fullName").value("John Doe"));
        }

        @Test
        @DisplayName("404 Not Found — user tidak ditemukan")
        void getOne_NotFound() throws Exception {
            when(userService.getOneUser(99L))
                    .thenThrow(new ResourceNotFoundException("User with id 99 not found"));

            mockMvc.perform(get("/api/v1/users/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.status").value(404));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users")
    class GetAllUsersEndpoint {

        @Test
        @DisplayName("200 OK — berhasil ambil semua user dengan pagination")
        void getAll_Success() throws Exception {
            UserResponse user2 = UserResponse.builder()
                    .id(2L)
                    .username("janedoe")
                    .email("jane@example.com")
                    .fullName("Jane Doe")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            List<UserResponse> users = List.of(sampleResponse, user2);
            Page<UserResponse> page = new PageImpl<>(users, PageRequest.of(0, 10), 2);

            when(userService.getAllUsers(any())).thenReturn(page);

            mockMvc.perform(get("/api/v1/users")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Users retrieved successfully"))
                    .andExpect(jsonPath("$.data.content", hasSize(2)))
                    .andExpect(jsonPath("$.data.content[0].username").value("johndoe"))
                    .andExpect(jsonPath("$.data.content[1].username").value("janedoe"))
                    .andExpect(jsonPath("$.data.totalElements").value(2));
        }

        @Test
        @DisplayName("200 OK — empty page jika tidak ada data")
        void getAll_Empty() throws Exception {
            Page<UserResponse> emptyPage = new PageImpl<>(
                    List.of(), PageRequest.of(0, 10), 0
            );

            when(userService.getAllUsers(any())).thenReturn(emptyPage);

            mockMvc.perform(get("/api/v1/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content", hasSize(0)))
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }
    }
}
