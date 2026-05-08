package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.expert.domain.auth.exception.AuthException;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Slf4j
@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String userRole = (String) request.getAttribute("userRole");

        if (!UserRole.ADMIN.name().equals(userRole)) {
            throw new AuthException("관리자 권한이 없습니다.");
        }

        log.info("Admin API access time={}, url={}", LocalDateTime.now(), request.getRequestURI());
        return true;
    }
}
