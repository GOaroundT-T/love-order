package com.love.order.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

/**
 * Bearer Token 过滤器：将前端发来的 Authorization: Bearer xxx
 * 转换为 SaToken 能识别的 header 格式
 */
@Configuration
public class TokenFilterConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public Filter bearerTokenFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(jakarta.servlet.http.HttpServletRequest request,
                                            jakarta.servlet.http.HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                String auth = request.getHeader("Authorization");
                if (auth != null && auth.startsWith("Bearer ")) {
                    String token = auth.substring(7);
                    request = new HeaderModifyingRequest(request, "satoken", token);
                }
                filterChain.doFilter(request, response);
            }
        };
    }

    /** 包装 HttpServletRequest，动态添加 header */
    private static class HeaderModifyingRequest extends HttpServletRequestWrapper {
        private final Map<String, String> customHeaders;

        public HeaderModifyingRequest(HttpServletRequest request, String name, String value) {
            super(request);
            this.customHeaders = new HashMap<>();
            this.customHeaders.put(name, value);
        }

        @Override
        public String getHeader(String name) {
            if (customHeaders.containsKey(name)) return customHeaders.get(name);
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (customHeaders.containsKey(name)) {
                return Collections.enumeration(List.of(customHeaders.get(name)));
            }
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            Set<String> names = new LinkedHashSet<>();
            Enumeration<String> original = super.getHeaderNames();
            while (original.hasMoreElements()) names.add(original.nextElement());
            names.addAll(customHeaders.keySet());
            return Collections.enumeration(names);
        }
    }
}
