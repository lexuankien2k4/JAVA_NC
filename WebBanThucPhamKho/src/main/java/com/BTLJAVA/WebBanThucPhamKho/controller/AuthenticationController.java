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
import org.springframework.http.ResponseEntity;
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

    @PostMapping
    public ResponseData<AuthenticationResponse> authentication(@RequestBody AuthenticationRequest authenticationRequest) {

        AuthenticationResponse authentication = authenticationService.authenticate(authenticationRequest);

        return ResponseData.<AuthenticationResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(authentication)
                .build();
    }

    @PostMapping("/refresh-token")
    public ResponseData<AccessTokenResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {

        AccessTokenResponse accessToken = authenticationService.verifyRefreshToken(refreshTokenRequest.getRefreshToken());


        return ResponseData.<AccessTokenResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(accessToken)
                .build();
    }
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        log.info("API /api/auth/register called with username: {}", userCreateRequest.getUsername());
        UserResponse userResponse = authenticationService.register(userCreateRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }


    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> loginUser(@Valid @RequestBody AuthenticationRequest authenticationRequest) {
        log.info("API /api/auth/login called for username: {}", authenticationRequest.getUsername());
        AuthenticationResponse authenticationResponse = authenticationService.authenticate(authenticationRequest);
        return ResponseEntity.ok(authenticationResponse);
    }

}
