package com.example.testdeploycrud.service;

import com.example.testdeploycrud.dto.UserCreateRequest;
import com.example.testdeploycrud.dto.UserResponse;
import com.example.testdeploycrud.dto.UserUpdateRequest;
import com.example.testdeploycrud.entity.UserEntity;
import com.example.testdeploycrud.exception.BadRequestException;
import com.example.testdeploycrud.exception.ResourceNotFoundException;
import com.example.testdeploycrud.repository.UserRepository;
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
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests untuk UserServiceImpl.
 *
 * Menggunakan Mockito untuk mock UserRepository,
 * sehingga test ini TIDAK butuh database dan berjalan sangat cepat.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private UserEntity sampleUser;
    private UserCreateRequest createRequest;
    private UserUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        // Data sample yang akan dipakai berulang di setiap test
        sampleUser = UserEntity.builder()
                .id(1L)
                .username("johndoe")
                .email("john@example.com")
                .fullName("John Doe")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = UserCreateRequest.builder()
                .username("johndoe")
                .email("john@example.com")
                .fullName("John Doe")
                .build();

        updateRequest = UserUpdateRequest.builder()
                .username("janedoe")
                .email("jane@example.com")
                .fullName("Jane Doe")
                .build();
    }

    // =========================================================================
    // CREATE USER TESTS
    // =========================================================================
    @Nested
    @DisplayName("createUser()")
    class CreateUserTests {

        @Test
        @DisplayName("Harus berhasil membuat user baru")
        void createUser_Success() {
            // Arrange: setup mock behavior
            when(userRepository.existsByUsername("johndoe")).thenReturn(false);
            when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
            when(userRepository.save(any(UserEntity.class))).thenReturn(sampleUser);

            // Act: panggil method yang ditest
            UserResponse result = userService.createUser(createRequest);

            // Assert: verifikasi hasilnya
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("johndoe", result.getUsername());
            assertEquals("john@example.com", result.getEmail());
            assertEquals("John Doe", result.getFullName());

            // Verify: pastikan repository dipanggil dengan benar
            verify(userRepository).existsByUsername("johndoe");
            verify(userRepository).existsByEmail("john@example.com");
            verify(userRepository).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("Harus throw BadRequestException jika username sudah dipakai")
        void createUser_DuplicateUsername_ThrowsException() {
            // Arrange
            when(userRepository.existsByUsername("johndoe")).thenReturn(true);

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> userService.createUser(createRequest)
            );

            assertEquals("Username is already in use", exception.getMessage());

            // Verify: save TIDAK boleh dipanggil
            verify(userRepository, never()).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("Harus throw BadRequestException jika email sudah dipakai")
        void createUser_DuplicateEmail_ThrowsException() {
            // Arrange
            when(userRepository.existsByUsername("johndoe")).thenReturn(false);
            when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

            // Act & Assert
            BadRequestException exception = assertThrows(
                    BadRequestException.class,
                    () -> userService.createUser(createRequest)
            );

            assertEquals("Email is already in use", exception.getMessage());
            verify(userRepository, never()).save(any(UserEntity.class));
        }
    }

    // =========================================================================
    // UPDATE USER TESTS
    // =========================================================================
    @Nested
    @DisplayName("updateUser()")
    class UpdateUserTests {

        @Test
        @DisplayName("Harus berhasil update semua field")
        void updateUser_AllFields_Success() {
            // Arrange
            UserEntity updatedUser = UserEntity.builder()
                    .id(1L)
                    .username("janedoe")
                    .email("jane@example.com")
                    .fullName("Jane Doe")
                    .createdAt(sampleUser.getCreatedAt())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(userRepository.existsByUsername("janedoe")).thenReturn(false);
            when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
            when(userRepository.save(any(UserEntity.class))).thenReturn(updatedUser);

            // Act
            UserResponse result = userService.updateUser(updateRequest, 1L);

            // Assert
            assertNotNull(result);
            assertEquals("janedoe", result.getUsername());
            assertEquals("jane@example.com", result.getEmail());
            assertEquals("Jane Doe", result.getFullName());

            verify(userRepository).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("Harus berhasil update hanya username")
        void updateUser_OnlyUsername_Success() {
            // Arrange: hanya isi username, email dan fullName null
            UserUpdateRequest partialUpdate = UserUpdateRequest.builder()
                    .username("newusername")
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(userRepository.existsByUsername("newusername")).thenReturn(false);
            when(userRepository.save(any(UserEntity.class))).thenReturn(sampleUser);

            // Act
            userService.updateUser(partialUpdate, 1L);

            // Assert: pastikan hanya username yang di-set
            verify(userRepository).existsByUsername("newusername");
            verify(userRepository, never()).existsByEmail(anyString());
            verify(userRepository).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("Harus berhasil update hanya email")
        void updateUser_OnlyEmail_Success() {
            UserUpdateRequest partialUpdate = UserUpdateRequest.builder()
                    .email("newemail@example.com")
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(userRepository.existsByEmail("newemail@example.com")).thenReturn(false);
            when(userRepository.save(any(UserEntity.class))).thenReturn(sampleUser);

            userService.updateUser(partialUpdate, 1L);

            verify(userRepository, never()).existsByUsername(anyString());
            verify(userRepository).existsByEmail("newemail@example.com");
        }

        @Test
        @DisplayName("Harus berhasil update hanya fullName")
        void updateUser_OnlyFullName_Success() {
            UserUpdateRequest partialUpdate = UserUpdateRequest.builder()
                    .fullName("New Name")
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(userRepository.save(any(UserEntity.class))).thenReturn(sampleUser);

            userService.updateUser(partialUpdate, 1L);

            // Tidak ada pengecekan existsByUsername/existsByEmail
            verify(userRepository, never()).existsByUsername(anyString());
            verify(userRepository, never()).existsByEmail(anyString());
        }

        @Test
        @DisplayName("Harus throw ResourceNotFoundException jika user tidak ditemukan")
        void updateUser_UserNotFound_ThrowsException() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> userService.updateUser(updateRequest, 99L)
            );

            assertEquals("User with id 99 not found", exception.getMessage());
            verify(userRepository, never()).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("Harus throw BadRequestException jika username baru sudah dipakai")
        void updateUser_DuplicateUsername_ThrowsException() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(userRepository.existsByUsername("janedoe")).thenReturn(true);

            assertThrows(
                    BadRequestException.class,
                    () -> userService.updateUser(updateRequest, 1L)
            );

            verify(userRepository, never()).save(any(UserEntity.class));
        }

        @Test
        @DisplayName("Harus throw BadRequestException jika email baru sudah dipakai")
        void updateUser_DuplicateEmail_ThrowsException() {
            // Arrange: username ok, tapi email sudah ada
            UserUpdateRequest emailUpdate = UserUpdateRequest.builder()
                    .email("taken@example.com")
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

            assertThrows(
                    BadRequestException.class,
                    () -> userService.updateUser(emailUpdate, 1L)
            );

            verify(userRepository, never()).save(any(UserEntity.class));
        }
    }

    // =========================================================================
    // DELETE USER TESTS
    // =========================================================================
    @Nested
    @DisplayName("deleteUser()")
    class DeleteUserTests {

        @Test
        @DisplayName("Harus berhasil menghapus user")
        void deleteUser_Success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

            // Act: tidak throw exception = sukses
            assertDoesNotThrow(() -> userService.deleteUser(1L));

            // Verify: pastikan delete dipanggil
            verify(userRepository).delete(sampleUser);
        }

        @Test
        @DisplayName("Harus throw ResourceNotFoundException jika user tidak ditemukan")
        void deleteUser_UserNotFound_ThrowsException() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> userService.deleteUser(99L)
            );

            verify(userRepository, never()).delete(any(UserEntity.class));
        }
    }

    // =========================================================================
    // GET ONE USER TESTS
    // =========================================================================
    @Nested
    @DisplayName("getOneUser()")
    class GetOneUserTests {

        @Test
        @DisplayName("Harus berhasil mengambil satu user")
        void getOneUser_Success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

            UserResponse result = userService.getOneUser(1L);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("johndoe", result.getUsername());
            assertEquals("john@example.com", result.getEmail());
            assertEquals("John Doe", result.getFullName());
            assertNotNull(result.getCreatedAt());
            assertNotNull(result.getUpdatedAt());
        }

        @Test
        @DisplayName("Harus throw ResourceNotFoundException jika user tidak ditemukan")
        void getOneUser_UserNotFound_ThrowsException() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            ResourceNotFoundException exception = assertThrows(
                    ResourceNotFoundException.class,
                    () -> userService.getOneUser(99L)
            );

            assertEquals("User with id 99 not found", exception.getMessage());
        }
    }

    // =========================================================================
    // GET ALL USERS TESTS
    // =========================================================================
    @Nested
    @DisplayName("getAllUsers()")
    class GetAllUsersTests {

        @Test
        @DisplayName("Harus berhasil mengambil semua user dengan pagination")
        void getAllUsers_Success() {
            // Arrange: buat page berisi 2 user
            UserEntity user2 = UserEntity.builder()
                    .id(2L)
                    .username("janedoe")
                    .email("jane@example.com")
                    .fullName("Jane Doe")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            List<UserEntity> users = List.of(sampleUser, user2);
            Pageable pageable = PageRequest.of(0, 10);
            Page<UserEntity> userPage = new PageImpl<>(users, pageable, users.size());

            when(userRepository.findAll(pageable)).thenReturn(userPage);

            // Act
            Page<UserResponse> result = userService.getAllUsers(pageable);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
            assertEquals(1, result.getTotalPages());
            assertEquals("johndoe", result.getContent().get(0).getUsername());
            assertEquals("janedoe", result.getContent().get(1).getUsername());
        }

        @Test
        @DisplayName("Harus return page kosong jika tidak ada user")
        void getAllUsers_Empty() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<UserEntity> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(userRepository.findAll(pageable)).thenReturn(emptyPage);

            Page<UserResponse> result = userService.getAllUsers(pageable);

            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
            assertTrue(result.getContent().isEmpty());
        }
    }
}
