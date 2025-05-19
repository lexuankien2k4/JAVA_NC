package com.BTLJAVA.WebBanThucPhamKho.controller;

import com.BTLJAVA.WebBanThucPhamKho.dto.request.AuthenticationRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.request.RefreshTokenRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.request.UserCreateRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.AccessTokenResponse;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.AuthenticationResponse;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.ResponseData;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.UserResponse;
import com.BTLJAVA.WebBanThucPhamKho.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
// ResponseEntity không còn cần thiết cho các response thành công nếu tất cả đều dùng ResponseData
// import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    /**
     * Xác thực người dùng. Endpoint này có thể trùng lặp với /login.
     * Cân nhắc sử dụng một endpoint đăng nhập chính.
     *
     * @param authenticationRequest DTO chứa thông tin xác thực.
     * @return ResponseData chứa AuthenticationResponse.
     */
    @PostMapping // Ánh xạ tới POST /api/v1/auth
    public ResponseData<AuthenticationResponse> authenticateUser(@Valid @RequestBody AuthenticationRequest authenticationRequest) {
        log.info("API /api/v1/auth (authenticateUser) được gọi cho username: {}", authenticationRequest.getUsername());
        AuthenticationResponse authentication = authenticationService.authenticate(authenticationRequest);

        return ResponseData.<AuthenticationResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Xác thực thành công.") // "Authentication successful."
                .data(authentication)
                .build();
    }

    /**
     * Xử lý đăng nhập của người dùng.
     *
     * @param authenticationRequest DTO chứa thông tin đăng nhập.
     * @return ResponseData chứa AuthenticationResponse.
     */
    @PostMapping("/login")
    public ResponseData<AuthenticationResponse> loginUser(@Valid @RequestBody AuthenticationRequest authenticationRequest) {
        log.info("API /api/v1/auth/login được gọi cho username: {}", authenticationRequest.getUsername());
        AuthenticationResponse authenticationResponse = authenticationService.authenticate(authenticationRequest);

        // Bao bọc AuthenticationResponse trong ResponseData
        return ResponseData.<AuthenticationResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Đăng nhập thành công.") // "Login successful."
                .data(authenticationResponse)
                .build();
    }

    /**
     * Đăng ký người dùng mới.
     *
     * @param userCreateRequest DTO chứa thông tin người dùng mới.
     * @return ResponseData chứa UserResponse của người dùng mới.
     */
    @PostMapping("/register")
    public ResponseData<UserResponse> registerUser(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        log.info("API /api/v1/auth/register được gọi với username: {}", userCreateRequest.getUsername());
        UserResponse userResponse = authenticationService.register(userCreateRequest);

        // Bao bọc UserResponse trong ResponseData
        return ResponseData.<UserResponse>builder()
                .status(HttpStatus.CREATED.value()) // HTTP 201 Created
                .message("Đăng ký thành công.") // "Registration successful."
                .data(userResponse)
                .build();
    }

    /**
     * Làm mới access token.
     *
     * @param refreshTokenRequest DTO chứa refresh token.
     * @return ResponseData chứa AccessTokenResponse.
     */
    @PostMapping("/refresh-token")
    public ResponseData<AccessTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        log.info("API /api/v1/auth/refresh-token được gọi.");
        AccessTokenResponse accessToken = authenticationService.verifyRefreshToken(refreshTokenRequest.getRefreshToken());

        return ResponseData.<AccessTokenResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Làm mới token thành công.") // "Token refreshed successfully."
                .data(accessToken)
                .build();
    }
}
