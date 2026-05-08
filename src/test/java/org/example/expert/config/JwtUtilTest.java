package org.example.expert.config;

import io.jsonwebtoken.Claims;
import org.example.expert.domain.common.exception.ServerException;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        String secretKey = Base64.getEncoder()
                .encodeToString("test-secret-key-for-hs256-must-be-long".getBytes());
        ReflectionTestUtils.setField(jwtUtil, "secretKey", secretKey);
        jwtUtil.init();
    }

    @Test
    void createToken_토큰을_생성하고_claim을_추출한다() {
        // when
        String bearerToken = jwtUtil.createToken(1L, "user@example.com", UserRole.USER);
        String token = jwtUtil.substringToken(bearerToken);
        Claims claims = jwtUtil.extractClaims(token);

        // then
        assertTrue(bearerToken.startsWith("Bearer "));
        assertEquals("1", claims.getSubject());
        assertEquals("user@example.com", claims.get("email"));
        assertEquals(UserRole.USER.name(), claims.get("userRole"));
    }

    @Test
    void substringToken_Bearer_토큰이면_토큰만_반환한다() {
        // when
        String token = jwtUtil.substringToken("Bearer abc.def.ghi");

        // then
        assertEquals("abc.def.ghi", token);
    }

    @Test
    void substringToken_값이_없으면_예외가_발생한다() {
        // when & then
        assertThrows(ServerException.class, () -> jwtUtil.substringToken(null));
    }

    @Test
    void substringToken_Bearer_형식이_아니면_예외가_발생한다() {
        // when & then
        assertThrows(ServerException.class, () -> jwtUtil.substringToken("abc.def.ghi"));
    }
}
