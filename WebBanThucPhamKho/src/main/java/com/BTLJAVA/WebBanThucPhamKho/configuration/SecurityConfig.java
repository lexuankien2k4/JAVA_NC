package com.BTLJAVA.WebBanThucPhamKho.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // QUAN TRỌNG nếu dùng @PreAuthorize
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity // Kích hoạt @PreAuthorize trên controller và service methods
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Danh sách các đường dẫn được phép truy cập công khai
    private static final String[] WHITE_LIST_URLS = {
            // UI Pages (served by PageController or static resources)
            "/",
            "/login",
            "/register",
            "/home",
            "/cart",
            "/checkout",
            "/order-management",
            "/order-confirmation",
            // Authentication APIs (handled by AuthController)
            "/api/v1/auth/**",
            "/api/v1/addresses/**",



            // Static Resources (CSS, JS, Images)
            "/css/**",
            "/js/**",
            "/images/**",
            "/static/**",
            "/api/statistics/**",

            "/api/v1/product/**",
            "/api/v1/categories/**",
            "/api/v1/cart/**",

            // Admin UI (LƯU Ý: "/admin/**" sẽ cho phép tất cả các trang admin công khai)
            "/admin/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Vô hiệu hóa CSRF (phổ biến cho API stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // API không dùng session
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // Thêm bộ lọc JWT
                .authenticationProvider(authenticationProvider()) // Cung cấp provider xác thực

                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(WHITE_LIST_URLS).permitAll() // Cho phép các URL trong WHITE_LIST_URLS
                        .anyRequest().authenticated() // Mọi yêu cầu khác đều cần xác thực
                )
                .formLogin(form -> form
                        .loginPage("/login") // Đường dẫn đến trang đăng nhập tùy chỉnh của bạn
                        .permitAll() // Cho phép tất cả mọi người truy cập trang /login
                )
                .logout(logout -> logout // Cấu hình logout
                        .logoutUrl("/api/v1/auth/logout") // Endpoint để thực hiện logout
                        .logoutSuccessUrl("/login?logout") // Chuyển hướng sau khi logout thành công
                        .permitAll()
                );
        // .httpBasic(AbstractHttpConfigurer::disable); // Vô hiệu hóa HTTP Basic auth nếu chỉ dùng JWT

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        return daoAuthenticationProvider;
    }
}
