package com.BTLJAVA.WebBanThucPhamKho.controller;

import com.BTLJAVA.WebBanThucPhamKho.dto.request.UserCreateRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.request.UserUpdateRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.ProductResponse;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.ResponseData;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.UserResponse;
import com.BTLJAVA.WebBanThucPhamKho.entity.CustomUserDetails; // Assuming this is your UserDetails implementation
import com.BTLJAVA.WebBanThucPhamKho.service.UserService;
import jakarta.validation.Valid; // For validating request bodies
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // Enable for @PreAuthorize
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@EnableMethodSecurity // Uncomment this to enable @PreAuthorize annotations
public class UserController {
    private final UserService userService;

//    @PreAuthorize("hasRole('ADMIN')") // Example: Restrict to ADMIN
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

//    @PreAuthorize("hasRole('ADMIN')") // Example: Admin creates user
    @PostMapping
    public ResponseData<UserResponse> createUser(@Valid @RequestBody UserCreateRequest userCreate) {
        UserResponse user = userService.createUser(userCreate);

        return ResponseData.<UserResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("User created successfully.")
                .data(user)
                .build();
    }
//    @PreAuthorize("isAuthenticated()") // Any authenticated user can update their own info
    @PatchMapping // No ID needed, updates the currently authenticated user
    public ResponseData<UserResponse> updateUser(Authentication authentication, @Valid @RequestBody UserUpdateRequest userUpdate) {
        // Ensure Principal is an instance of your CustomUserDetails
        if (!(authentication.getPrincipal() instanceof CustomUserDetails)) {
            // Handle this case appropriately, perhaps throw an exception or return an error response
            return ResponseData.<UserResponse>builder()
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .message("Invalid user details in authentication token.")
                    .build();
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UserResponse userResponse = userService.updateUser(userDetails.getId(), userUpdate);

        return ResponseData.<UserResponse>builder()
                .status(HttpStatus.OK.value())
                .message("User profile updated successfully.")
                .data(userResponse)
                .build();
    }

//    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    public ResponseData<UserResponse> updateUserByAdmin(
            @PathVariable Integer id,
            @Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        UserResponse updatedUser = userService.updateUserByAdmin(id, userUpdateRequest);
        return ResponseData.<UserResponse>builder()
                .status(HttpStatus.OK.value())
                .message("User updated successfully by admin.")
                .data(updatedUser)
                .build();
    }

//    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseData<String> deleteUser(@PathVariable Integer id) {
        userService.deleteUser(id);
        return ResponseData.<String>builder()
                .status(HttpStatus.OK.value())
                .message("User deleted successfully.")
                .data("User with ID " + id + " has been deleted.")
                .build();
    }

    // Example: Allow ADMIN or the user themselves to get their details
//    @PreAuthorize("hasRole('ADMIN') or @userService.isSelf(authentication, #id)")
    @GetMapping("/{id}")
    public ResponseData<UserResponse> getUserById(@PathVariable Integer id) {
        UserResponse user = userService.getUserResponseById(id);
        return ResponseData.<UserResponse>builder()
                .status(HttpStatus.OK.value())
                .message("User details fetched successfully.")
                .data(user)
                .build();
    }

//    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/toggle-lock")
    public ResponseData<UserResponse> toggleUserLockStatus(@PathVariable Integer id) {
        UserResponse updatedUser = userService.toggleUserLockStatus(id);
        return ResponseData.<UserResponse>builder()
                .status(HttpStatus.OK.value())
                .message("User lock status toggled successfully.")
                .data(updatedUser)
                .build();
    }
}