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
            "/",                     // Trang chủ (ví dụ: index.html)
            "/login",                // Trang hiển thị form đăng nhập (GET -> index.html)
            "/register",             // Trang hiển thị form đăng ký (GET -> index.html, nếu có)

            // Authentication APIs (handled by AuthController)
            "/api/v1/auth/**",       // Bao gồm /api/v1/auth/login, /api/v1/auth/register

            // Static Resources (CSS, JS, Images)
            "/css/**",
            "/js/**",
            "/images/**",
            "/static/**",            // Nếu bạn có thư mục static cấp cao nhất khác

            // Public API examples (điều chỉnh theo nhu cầu)
            // Ví dụ: cho phép GET request tới product, các method khác (POST, PUT, DELETE)
            // sẽ được bảo vệ bằng @PreAuthorize ở tầng controller/service.
            // Nếu bạn muốn tất cả các method của product API đều public ở đây, giữ nguyên như cũ.
            // Hoặc bạn có thể chỉ định HttpMethod.GET cho "/api/v1/product/**"
            "/api/v1/product/**",

            // Admin UI (LƯU Ý: "/admin/**" sẽ cho phép tất cả các trang admin công khai)
            // Nếu bạn muốn bảo vệ các trang admin, hãy xóa dòng "/admin/**" khỏi đây
            // và chúng sẽ được bảo vệ bởi .anyRequest().authenticated()
            // Sau đó, sử dụng @PreAuthorize trên các Admin Controller.
            "/admin/**" // Ví dụ: "/admin/products/list", "/admin/dashboard"
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
                // Cấu hình formLogin để Spring Security biết trang đăng nhập tùy chỉnh của bạn là "/login".
                // Khi người dùng chưa xác thực cố gắng truy cập một tài nguyên được bảo vệ qua trình duyệt,
                // họ sẽ được chuyển hướng đến "/login".
                // Việc xử lý submit form đăng nhập thực tế vẫn do JavaScript và API /api/v1/auth/login đảm nhận.
                .formLogin(form -> form
                        .loginPage("/login") // Đường dẫn đến trang đăng nhập tùy chỉnh của bạn
                        .permitAll() // Cho phép tất cả mọi người truy cập trang /login
                )
                .logout(logout -> logout // Cấu hình logout (tùy chọn, có thể xử lý logout phía client bằng cách xóa token)
                        .logoutUrl("/api/v1/auth/logout") // Endpoint để thực hiện logout (nếu bạn có logic server-side cho logout)
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
