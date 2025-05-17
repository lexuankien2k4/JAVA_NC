package com.BTLJAVA.WebBanThucPhamKho.service.impl;


import com.BTLJAVA.WebBanThucPhamKho.dto.request.AuthenticationRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.request.RegistrationRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.request.UserCreateRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.AccessTokenResponse;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.AuthenticationResponse;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.UserResponse;
import com.BTLJAVA.WebBanThucPhamKho.entity.User;
import com.BTLJAVA.WebBanThucPhamKho.mapper.UserMapper;
import com.BTLJAVA.WebBanThucPhamKho.repository.RoleRepository;
import com.BTLJAVA.WebBanThucPhamKho.service.AuthenticationService;
import com.BTLJAVA.WebBanThucPhamKho.service.JwtService;
import com.BTLJAVA.WebBanThucPhamKho.service.UserService;
import com.BTLJAVA.WebBanThucPhamKho.util.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationImpl implements AuthenticationService {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder; // Added for registration
    private final RoleRepository roleRepository;     // Added for assigning default role

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword()));

        User user = userService.findByUsername(authenticationRequest.getUsername());
        log.info("Authenticated userId {}", user.getId());

        String accessToken = jwtService.generateToken(user, TypeToken.ACCESS);
        String refreshToken = jwtService.generateToken(user, TypeToken.REFRESH);

        UserResponse userResponse = userMapper.toDTO(user);

        return AuthenticationResponse.builder()
                .userResponse(userResponse)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AccessTokenResponse verifyRefreshToken(String refreshToken) {
        String username = jwtService.verifyTokenAndExtractUserName(refreshToken, TypeToken.REFRESH);

        User user = userService.findByUsername(username);

        String accessToken = jwtService.generateToken(user, TypeToken.ACCESS);

        return AccessTokenResponse.builder()
                .accessToken(accessToken)
                .build();
    }
    @Override
    public UserResponse register(UserCreateRequest userCreateRequest) {
        log.info("Registration attempt for username: {}", userCreateRequest.getUsername());

        UserResponse registeredUserResponse = userService.createUser(userCreateRequest);

        log.info("User {} registered successfully with ID {}.",
                registeredUserResponse.getUserName(),
                registeredUserResponse.getId());

        return registeredUserResponse;
    }


}
