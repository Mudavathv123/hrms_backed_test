    package com.hrms.hrm.security;

    import com.hrms.hrm.service.CustomUserDetailsService;
    import lombok.RequiredArgsConstructor;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.http.HttpMethod;
    import org.springframework.security.authentication.AuthenticationManager;
    import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
    import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
    import org.springframework.security.config.http.SessionCreationPolicy;
    import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.security.web.SecurityFilterChain;
    import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
    import org.springframework.web.cors.CorsConfiguration;
    import org.springframework.web.cors.CorsConfigurationSource;
    import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

    import java.util.List;

    @RequiredArgsConstructor
    @EnableWebSecurity
    @Configuration
    public class SecurityConfig {

        private final CustomUserDetailsService customUserDetailsService;
        private final JwtAuthenticationFilter jwtAuthenticationFilter;


        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
            DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
            provider.setUserDetailsService(customUserDetailsService);
            provider.setPasswordEncoder(passwordEncoder());
            return provider;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
            return authConfig.getAuthenticationManager();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowedOrigins(List.of(
                    "http://localhost:3000",
                    "http://localhost:5173",
                    "http://localhost:4200",
                "https://d1ujpx8cjlbvx.cloudfront.net"
            ));
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE","PATCH" ,"OPTIONS"));
            config.setAllowedHeaders(List.of("*"));
            config.setExposedHeaders(List.of("Authorization"));
            config.setAllowCredentials(true);

            UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
            source.registerCorsConfiguration("/**", config);
            return source;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(csrf -> csrf.disable())
                    .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                    .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()))
                    .authorizeHttpRequests(auth -> auth

                            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                            // WebSocket
                            .requestMatchers("/ws/**").permitAll()

                            // Auth
                            .requestMatchers("/auth/signup").hasRole("ADMIN")
                            .requestMatchers("/auth/**").permitAll()
                            .requestMatchers(HttpMethod.POST,"/auth/reset-password").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.POST, "/auth/forgot-password").hasRole("ADMIN")


                            // ---------------- EMPLOYEE ----------------
                            .requestMatchers(HttpMethod.GET, "/api/employees/get-all-employees")
                            .hasAnyRole("ADMIN", "HR", "MANAGER")

                            .requestMatchers(HttpMethod.GET, "/api/employees/{idt}")
                            .hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")

                            .requestMatchers(HttpMethod.GET, "/api/employees/department/**")
                            .hasAnyRole("HR", "ADMIN")

                            .requestMatchers(HttpMethod.POST, "/api/employees/*/avatar").authenticated()
                            .requestMatchers("/uploads/**").permitAll()


                            // ---------------- ATTENDANCE ----------------
                            .requestMatchers("/api/attendance/**")
                            .hasAnyRole("ADMIN", "HR", "EMPLOYEE")

                            // ---------------- TASKS ----------------
                            .requestMatchers(HttpMethod.POST, "/api/tasks/**")
                            .hasAnyRole("ADMIN", "HR", "MANAGER")

                            .requestMatchers(HttpMethod.PUT, "/api/tasks/**")
                            .hasAnyRole("ADMIN", "HR", "MANAGER")

                            .requestMatchers(HttpMethod.PATCH, "/api/tasks/*/status")
                            .hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")

                            .requestMatchers(HttpMethod.DELETE, "/api/tasks/**")
                            .hasAnyRole("ADMIN", "HR", "MANAGER")

                            .requestMatchers(HttpMethod.GET, "/api/tasks/employee/**")
                            .hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")

                            .requestMatchers(HttpMethod.GET, "/api/tasks")
                            .hasAnyRole("ADMIN", "HR", "MANAGER")

                            // ---------------- LEAVES ----------------
                            .requestMatchers(HttpMethod.POST, "/api/leaves")
                            .hasRole("EMPLOYEE")

                            .requestMatchers(HttpMethod.POST, "/api/leaves/*/action")
                            .hasAnyRole("ADMIN", "HR", "MANAGER")

                            .requestMatchers(HttpMethod.GET, "/api/leaves/employee/**")
                            .hasAnyRole("ADMIN", "HR", "MANAGER", "EMPLOYEE")

                            .requestMatchers(HttpMethod.GET, "/api/leaves/pending")
                            .hasAnyRole("ADMIN", "HR", "MANAGER")

                            .requestMatchers(HttpMethod.GET, "/api/leaves")
                            .hasAnyRole("ADMIN", "HR")

                            // ---------------- NOTIFICATIONS ----------------
                            .requestMatchers(HttpMethod.POST, "/api/notifications/**")
                            .hasRole("HR")

                            .requestMatchers(HttpMethod.DELETE, "/api/notifications/**")
                            .hasAnyRole("ADMIN","EMPLOYEE","HR")

                            .requestMatchers(HttpMethod.PATCH, "/api/notifications/**")
                            .hasAnyRole("ADMIN", "HR", "EMPLOYEE")

                            .requestMatchers(HttpMethod.GET, "/api/notifications/**")
                            .authenticated()

                            // ---------------- EOD ----------------
                            .requestMatchers(HttpMethod.POST, "/api/eod")
                            .hasRole("EMPLOYEE")

                            .requestMatchers(HttpMethod.POST, "/api/eod/**")
                            .hasAnyRole("HR", "ADMIN")

                            .requestMatchers(HttpMethod.PUT, "/api/eod/**")
                            .hasAnyRole("ADMIN")

                            .requestMatchers(HttpMethod.DELETE, "/api/eod/**")
                            .hasAnyRole("ADMIN")

                            .requestMatchers(HttpMethod.GET, "/api/eod/**")
                            .authenticated()

                            // DEFAULT
                            .anyRequest().authenticated()
                    )
                    .authenticationProvider(authenticationProvider())
                    .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                    .httpBasic(h -> h.disable())
                    .formLogin(f -> f.disable());


            return http.build();
        }
    }
