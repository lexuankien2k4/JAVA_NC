package com.BTLJAVA.WebBanThucPhamKho.service;

import com.BTLJAVA.WebBanThucPhamKho.dto.request.AuthenticationRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.request.UserCreateRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.AccessTokenResponse;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.AuthenticationResponse;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.UserResponse;

public interface AuthenticationService {

    AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest);

    AccessTokenResponse verifyRefreshToken(String refreshToken);
    UserResponse register(UserCreateRequest userCreateRequest);
}

