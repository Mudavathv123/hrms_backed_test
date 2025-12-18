package com.hrms.hrm.config;

import com.hrms.hrm.repository.EmployeeRepository;
import com.hrms.hrm.util.JwtUtil;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URLDecoder;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final EmployeeRepository employeeRepository;

    public JwtHandshakeInterceptor(JwtUtil jwtUtil, EmployeeRepository employeeRepository) {
        this.jwtUtil = jwtUtil;
        this.employeeRepository = employeeRepository;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        if (request.getURI().getPath().endsWith("/info")) return true;

        String token = null;


        List<String> authHeaders = request.getHeaders().get("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String raw = authHeaders.get(0);
            if (raw.startsWith("Bearer ")) token = raw.substring(7);
        }

        if (token == null && request.getURI().getQuery() != null) {
            for (String pair : request.getURI().getQuery().split("&")) {
                String[] kv = pair.split("=");
                if (kv.length == 2 && "access_token".equals(kv[0])) {
                    token = URLDecoder.decode(kv[1], "UTF-8");
                }
            }
        }

        if (token == null || !jwtUtil.validateToken(token, jwtUtil.extractUsername(token))) {
            return false; // Reject handshake
        }

        String userEmail = jwtUtil.extractUsername(token);
        Principal principal = new StompPrincipal(userEmail);
        attributes.put("principal", principal);

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // No-op
    }
}
