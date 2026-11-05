package br.com.demandwise.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AcessoAutenticadoInterceptor interceptor;
    private final boolean habilitado;
    public WebConfig(AcessoAutenticadoInterceptor interceptor, @Value("${demandwise.auth.enabled:true}") boolean habilitado) { this.interceptor = interceptor; this.habilitado = habilitado; }
    @Override public void addInterceptors(InterceptorRegistry registry) {
        if (!habilitado) return;
        registry.addInterceptor(interceptor).addPathPatterns("/**")
                .excludePathPatterns("/login", "/logout", "/css/**", "/js/**", "/favicon.ico");
    }
}
