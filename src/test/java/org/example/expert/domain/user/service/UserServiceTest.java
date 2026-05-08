package org.example.expert.domain.user.service;

import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.user.dto.request.UserChangePasswordRequest;
import org.example.expert.domain.user.dto.response.UserResponse;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    @Test
    void getUser_사용자를_조회한다() {
        // given
        User user = new User("user@example.com", "encodedPassword", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        // when
        UserResponse response = userService.getUser(1L);

        // then
        assertEquals(1L, response.getId());
        assertEquals("user@example.com", response.getEmail());
    }

    @Test
    void getUser_사용자가_없으면_예외가_발생한다() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                userService.getUser(1L)
        );

        assertEquals("User not found", exception.getMes${DB_USERNAME}ge());
    }

    @Test
    void changePassword_사용자가_없으면_예외가_발생한다() {
        // given
        UserChangePasswordRequest request = new UserChangePasswordRequest("oldPassword", "NewPassword1");
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                userService.changePassword(1L, request)
        );

        assertEquals("User not found", exception.getMes${DB_USERNAME}ge());
    }

    @Test
    void changePassword_새_비밀번호가_기존_비밀번호와_같으면_예외가_발생한다() {
        // given
        User user = new User("user@example.com", "encodedPassword", UserRole.USER);
        UserChangePasswordRequest request = new UserChangePasswordRequest("oldPassword", "NewPassword1");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getNewPassword(), user.getPassword())).willReturn(true);

        // when & then
        assertThrows(InvalidRequestException.class, () -> userService.changePassword(1L, request));
    }

    @Test
    void changePassword_기존_비밀번호가_일치하지_않으면_예외가_발생한다() {
        // given
        User user = new User("user@example.com", "encodedPassword", UserRole.USER);
        UserChangePasswordRequest request = new UserChangePasswordRequest("wrongPassword", "NewPassword1");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getNewPassword(), user.getPassword())).willReturn(false);
        given(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).willReturn(false);

        // when & then
        assertThrows(InvalidRequestException.class, () -> userService.changePassword(1L, request));
    }

    @Test
    void changePassword_비밀번호를_변경한다() {
        // given
        User user = new User("user@example.com", "encodedPassword", UserRole.USER);
        UserChangePasswordRequest request = new UserChangePasswordRequest("oldPassword", "NewPassword1");

        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getNewPassword(), user.getPassword())).willReturn(false);
        given(passwordEncoder.matches(request.getOldPassword(), user.getPassword())).willReturn(true);
        given(passwordEncoder.encode(request.getNewPassword())).willReturn("newEncodedPassword");

        // when
        userService.changePassword(1L, request);

        // then
        assertEquals("newEncodedPassword", user.getPassword());
        verify(passwordEncoder).encode(request.getNewPassword());
    }
}
