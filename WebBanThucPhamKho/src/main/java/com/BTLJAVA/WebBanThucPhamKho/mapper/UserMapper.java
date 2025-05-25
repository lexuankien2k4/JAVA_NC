package com.BTLJAVA.WebBanThucPhamKho.mapper;

import com.BTLJAVA.WebBanThucPhamKho.dto.response.UserResponse;
import com.BTLJAVA.WebBanThucPhamKho.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {
    public UserResponse toDTO(User user) {
        if (user == null) {
            return null;
        }

        // Lấy danh sách tên vai trò từ Set<UserHasRole>
        Set<String> roleNames = null;
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            roleNames = user.getRoles().stream()
                    .map(userHasRole -> userHasRole.getRole().getName().toUpperCase())
                    .collect(Collectors.toSet());
        }

        return UserResponse.builder()
                .id(user.getId())
                .userName(user.getUsername()) // Entity User của bạn có getUsername()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .isLock(user.getIsLock())
                .roles(roleNames) // <<--- GÁN DANH SÁCH VAI TRÒ
                .build();
    }
}

