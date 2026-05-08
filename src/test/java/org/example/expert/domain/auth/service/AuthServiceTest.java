package org.example.expert.domain.auth.service;

import org.example.expert.config.JwtUtil;
import org.example.expert.config.PasswordEncoder;
import org.example.expert.domain.auth.dto.request.SigninRequest;
import org.example.expert.domain.auth.dto.request.SignupRequest;
import org.example.expert.domain.auth.dto.response.SigninResponse;
import org.example.expert.domain.auth.dto.response.SignupResponse;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.exception.InvalidRequestException;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private AuthService authService;

    @Test
    void signup_이미_존재하는_이메일이면_예외가_발생한다() {
        // given
        SignupRequest request = new SignupRequest("user@example.com", "Password1", "USER");
        given(userRepository.existsByEmail(request.getEmail())).willReturn(true);

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                authService.signup(request)
        );

        assertEquals("이미 존재하는 이메일입니다.", exception.getMes${DB_USERNAME}ge());
    }

    @Test
    void signup_정상적으로_회원가입하고_토큰을_반환한다() {
        // given
        SignupRequest request = new SignupRequest("user@example.com", "Password1", "USER");
        User ${DB_USERNAME}vedUser = new User(request.getEmail(), "encodedPassword", UserRole.USER);
        ReflectionTestUtils.setField(${DB_USERNAME}vedUser, "id", 1L);

        given(userRepository.existsByEmail(request.getEmail())).willReturn(false);
        given(passwordEncoder.encode(request.getPassword())).willReturn("encodedPassword");
        given(userRepository.${DB_USERNAME}ve(any(User.class))).willReturn(${DB_USERNAME}vedUser);
        given(jwtUtil.createToken(${DB_USERNAME}vedUser.getId(), ${DB_USERNAME}vedUser.getEmail(), UserRole.USER)).willReturn("Bearer token");

        // when
        SignupResponse response = authService.signup(request);

        // then
        assertEquals("Bearer token", response.getBearerToken());
        verify(userRepository).${DB_USERNAME}ve(any(User.class));
    }

    @Test
    void signin_가입되지_않은_이메일이면_예외가_발생한다() {
        // given
        SigninRequest request = new SigninRequest("user@example.com", "Password1");
        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.empty());

        // when & then
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
                authService.signin(request)
        );

        assertTrue(exception.getMes${DB_USERNAME}ge().contains("가입"));
    }

    @Test
    void signin_비밀번호가_일치하지_않으면_예외가_발생한다() {
        // given
        SigninRequest request = new SigninRequest("user@example.com", "wrongPassword");
        User user = new User(request.getEmail(), "encodedPassword", UserRole.USER);

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(false);

        // when & then
        assertThrows(AuthException.class, () -> authService.signin(request));
    }

    @Test
    void signin_정상적으로_로그인하고_토큰을_반환한다() {
        // given
        SigninRequest request = new SigninRequest("user@example.com", "Password1");
        User user = new User(request.getEmail(), "encodedPassword", UserRole.USER);
        ReflectionTestUtils.setField(user, "id", 1L);

        given(userRepository.findByEmail(request.getEmail())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(request.getPassword(), user.getPassword())).willReturn(true);
        given(jwtUtil.createToken(user.getId(), user.getEmail(), user.getUserRole())).willReturn("Bearer token");

        // when
        SigninResponse response = authService.signin(request);

        // then
        assertEquals("Bearer token", response.getBearerToken());
    }
}
