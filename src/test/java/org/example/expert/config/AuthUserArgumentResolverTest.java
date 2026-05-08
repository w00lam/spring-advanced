package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.common.annotation.Auth;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class AuthUserArgumentResolverTest {

    private final AuthUserArgumentResolver resolver = new AuthUserArgumentResolver();

    @Test
    void supportsParameter_Auth와_AuthUser를_함께_사용하면_true를_반환한다() throws Exception {
        // given
        MethodParameter parameter = getMethodParameter("validAuthUser", 0);

        // when & then
        assertTrue(resolver.supportsParameter(parameter));
    }

    @Test
    void supportsParameter_Auth만_사용하면_예외가_발생한다() throws Exception {
        // given
        Method method = TestController.class.getDeclaredMethod("invalidAuthOnly", String.class);
        MethodParameter parameter = new MethodParameter(method, 0);

        // when & then
        assertThrows(AuthException.class, () -> resolver.supportsParameter(parameter));
    }

    @Test
    void supportsParameter_AuthUser만_사용하면_예외가_발생한다() throws Exception {
        // given
        MethodParameter parameter = getMethodParameter("invalidAuthUserOnly", 0);

        // when & then
        assertThrows(AuthException.class, () -> resolver.supportsParameter(parameter));
    }

    @Test
    void resolveArgument_request_attribute로_AuthUser를_생성한다() {
        // given
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        NativeWebRequest webRequest = mock(NativeWebRequest.class);

        given(webRequest.getNativeRequest()).willReturn(servletRequest);
        given(servletRequest.getAttribute("userId")).willReturn(1L);
        given(servletRequest.getAttribute("email")).willReturn("user@example.com");
        given(servletRequest.getAttribute("userRole")).willReturn("USER");

        // when
        AuthUser authUser = (AuthUser) resolver.resolveArgument(null, null, webRequest, null);

        // then
        assertEquals(1L, authUser.getId());
        assertEquals("user@example.com", authUser.getEmail());
        assertEquals(UserRole.USER, authUser.getUserRole());
    }

    private MethodParameter getMethodParameter(String methodName, int parameterIndex) throws NoSuchMethodException {
        Method method = TestController.class.getDeclaredMethod(methodName, AuthUser.class);
        return new MethodParameter(method, parameterIndex);
    }

    private static class TestController {

        void validAuthUser(@Auth AuthUser authUser) {
        }

        void invalidAuthOnly(@Auth String authUser) {
        }

        void invalidAuthUserOnly(AuthUser authUser) {
        }
    }
}
