package com.BTLJAVA.WebBanThucPhamKho.controller;

import com.BTLJAVA.WebBanThucPhamKho.dto.request.UserCreateRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.ResponseData;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.UserResponse;
import com.BTLJAVA.WebBanThucPhamKho.dto.request.UserUpdateRequest;
import com.BTLJAVA.WebBanThucPhamKho.entity.CustomUserDetails;
import com.BTLJAVA.WebBanThucPhamKho.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@EnableMethodSecurity
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public ResponseData<List<UserResponse>> getUsers(
            @RequestParam(value = "page", defaultValue = "0") Integer pageNumber) {

        List<UserResponse> users = userService.getUsers(pageNumber);

        return ResponseData.<List<UserResponse>>builder()
                .status(HttpStatus.OK.value())
                .message("Success")
                .data(users)
                .build();
    }

    @PostMapping
    public ResponseData<UserResponse> createUser(@RequestBody UserCreateRequest userCreate) {
        UserResponse user = userService.createUser(userCreate);

        return ResponseData.<UserResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("Created successful")
                .data(user)
                .build();
    }

    @PreAuthorize("hasRole('USER')")
    @PatchMapping
    public ResponseData<UserResponse> updateUser(Authentication authentication, @RequestBody UserUpdateRequest userUpdate) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        UserResponse userResponse = userService.updateUser(userDetails.getId(), userUpdate);

        return ResponseData.<UserResponse>builder()
                .status(HttpStatus.OK.value())
                .message("Updated successful")
                .data(userResponse)
                .build();
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseData<String> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseData.<String>builder()
                .status(HttpStatus.OK.value())
                .message("Deleted successful")
                .data("User with ID " + id + " has been deleted")
                .build();
    }
}
