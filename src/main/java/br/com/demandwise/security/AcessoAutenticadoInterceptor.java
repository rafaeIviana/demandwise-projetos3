package br.com.demandwise.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AcessoAutenticadoInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getSession(false) != null && Boolean.TRUE.equals(request.getSession(false).getAttribute("autenticado"))) return true;
        if (request.getRequestURI().startsWith("/api/")) response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        else response.sendRedirect(request.getContextPath() + "/login");
        return false;
    }
}
