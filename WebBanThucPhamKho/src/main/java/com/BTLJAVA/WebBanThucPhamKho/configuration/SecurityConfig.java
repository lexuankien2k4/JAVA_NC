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
@EnableMethodSecurity // QUAN TRỌNG: Kích hoạt @PreAuthorize trên controller
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // Danh sách các đường dẫn được phép truy cập công khai
    private static final String[] WHITE_LIST_URLS = {
            // API xác thực (nếu có)
            "/",
            "/api/v1/auth/**",

            // API sản phẩm (GET sẽ được phép, POST/PATCH/DELETE sẽ bị chặn bởi @PreAuthorize nếu không có token đúng)
            "/api/v1/product/**",
            // "/api/v1/productVariant/**", // Nếu có

            // ---- CHO PHÉP CÁC ĐƯỜNG DẪN UI ----
            "/admin/products/list",      // Trang danh sách sản phẩm
            "/admin/products/add",       // Trang form thêm sản phẩm
            "/admin/products/edit/**",   // Trang form sửa sản phẩm (cho phép mọi ID)
            // Nếu bạn có các trang admin khác, thêm vào đây, ví dụ: "/admin/dashboard"
            // Hoặc dùng một mẫu rộng hơn nếu tất cả các trang admin đều công khai (không khuyến khích cho tất cả):
             "/admin/**",

            // ---- CHO PHÉP CÁC TÀI NGUYÊN TĨNH (CSS, JS, IMAGES) ----
            // Dựa trên cấu trúc thư mục của bạn: src/main/resources/static/
            "/css/**",        // Cho phép tất cả file trong static/css/
            "/js/**",         // Cho phép tất cả file trong static/js/
            "/images/**",     // Cho phép tất cả file trong static/images/ (nếu có)
             "/static/**",  // Không cần thiết nếu đã có /css/**, /js/** trừ khi có thư mục con khác
            "/favicon.ico",

            // Các trang HTML tĩnh khác nếu có (ví dụ: trang chủ, trang lỗi tùy chỉnh)
            // "/login",
            // "/home.html",
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Vô hiệu hóa CSRF
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // API không dùng session
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // Thêm bộ lọc JWT
                .authenticationProvider(authenticationProvider()) // Cung cấp provider xác thực
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(WHITE_LIST_URLS).permitAll() // Cho phép các URL trong WHITE_LIST_URLS
//                        .anyRequest().authenticated() // Mọi yêu cầu khác đều cần xác thực
                       .anyRequest().permitAll()
                );
        // .formLogin(AbstractHttpConfigurer::disable) // Vô hiệu hóa form login mặc định nếu chỉ dùng JWT
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
