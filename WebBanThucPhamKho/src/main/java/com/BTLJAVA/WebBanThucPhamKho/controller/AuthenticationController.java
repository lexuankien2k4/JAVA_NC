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

    @PostMapping // Ánh xạ tới POST /api/v1/auth
    public ResponseData<AuthenticationResponse> authenticateUser(@Valid @RequestBody AuthenticationRequest authenticationRequest) {
        log.info("API /api/v1/auth (authenticateUser) được gọi cho username: {}", authenticationRequest.getUsername());
        AuthenticationResponse authentication = authenticationService.authenticate(authenticationRequest);

        return ResponseData.<AuthenticationResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Xác thực thành công.")
                .data(authentication)
                .build();
    }

    @PostMapping("/login")
    public ResponseData<AuthenticationResponse> loginUser(@Valid @RequestBody AuthenticationRequest authenticationRequest) {
        log.info("API /api/v1/auth/login được gọi cho username: {}", authenticationRequest.getUsername());
        AuthenticationResponse authenticationResponse = authenticationService.authenticate(authenticationRequest);

        // Bao bọc AuthenticationResponse trong ResponseData
        return ResponseData.<AuthenticationResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Đăng nhập thành công.")
                .data(authenticationResponse)
                .build();
    }

    @PostMapping("/register")
    public ResponseData<UserResponse> registerUser(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        log.info("API /api/v1/auth/register được gọi với username: {}", userCreateRequest.getUsername());
        UserResponse userResponse = authenticationService.register(userCreateRequest);

        // Bao bọc UserResponse trong ResponseData
        return ResponseData.<UserResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Đăng ký thành công.")
                .data(userResponse)
                .build();
    }
    @PostMapping("/refresh-token")
    public ResponseData<AccessTokenResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest) {
        log.info("API /api/v1/auth/refresh-token được gọi.");
        AccessTokenResponse accessToken = authenticationService.verifyRefreshToken(refreshTokenRequest.getRefreshToken());

        return ResponseData.<AccessTokenResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Làm mới token thành công.")
                .data(accessToken)
                .build();
    }
}
