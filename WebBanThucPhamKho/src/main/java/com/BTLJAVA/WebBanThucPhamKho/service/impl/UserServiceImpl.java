package com.BTLJAVA.WebBanThucPhamKho.service.impl;
import com.BTLJAVA.WebBanThucPhamKho.dto.request.UserCreateRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.request.UserUpdateRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.UserResponse;
import com.BTLJAVA.WebBanThucPhamKho.entity.Role;
import com.BTLJAVA.WebBanThucPhamKho.entity.User;
import com.BTLJAVA.WebBanThucPhamKho.entity.UserHasRole;
import com.BTLJAVA.WebBanThucPhamKho.mapper.UserMapper;
import com.BTLJAVA.WebBanThucPhamKho.repository.UserRepository;
import com.BTLJAVA.WebBanThucPhamKho.service.RoleService;
import com.BTLJAVA.WebBanThucPhamKho.service.UserService;
import com.BTLJAVA.WebBanThucPhamKho.util.TypeUser;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final UserMapper userMapper;

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new EntityNotFoundException("Not found"));
    }

    @Override
    public List<UserResponse> getUsers(Integer pageNumber) {
        Pageable page = PageRequest.of(pageNumber, 10);
        List<User> users = userRepository.findAll(page).getContent();
        return users.stream().map(userMapper::toDTO).toList();
    }

    @Override
    public UserResponse createUser(UserCreateRequest userCreate) {

        if (userRepository.existsByUsername(userCreate.getUsername())) throw new EntityExistsException("Username existed");

        Role role = roleService.findByName(TypeUser.USER.name());

        User user = User.builder()
                .firstName(userCreate.getFirstName())
                .lastName(userCreate.getLastName())
                .username(userCreate.getUsername())
                .password(passwordEncoder.encode(userCreate.getPassword()))
                .email(userCreate.getEmail())
                .phone(userCreate.getPhone())
                .isLock(Boolean.FALSE)
                .build();

        UserHasRole userHasRole = UserHasRole.builder()
                .user(user)
                .role(role)
                .build();

        user.setRoles(Set.of(userHasRole));

        user = userRepository.save(user);

        return userMapper.toDTO(user);
    }

    @Override
    public UserResponse updateUser(Integer id, UserUpdateRequest userUpdate) {

        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));

        user.setFirstName(userUpdate.getFirstName());
        user.setLastName(userUpdate.getLastName());
        user.setEmail(userUpdate.getEmail());
        user.setPhone(userUpdate.getPhone());

        user = userRepository.save(user);

        return userMapper.toDTO(user);
    }

    @Override
    public void deleteUser(Integer id) {
        User  user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        userRepository.delete(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Attempting to load user by username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("USER_NOT_FOUND_WITH_USERNAME: {}", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });

        // Giả sử User entity của bạn đã implement UserDetails
        log.info("User found: {}", user.getUsername());
        log.info("Encoded Password from DB for user {}: {}", user.getUsername(), user.getPassword());
        log.info("Authorities for user {}: {}", user.getUsername(), user.getAuthorities());
        log.info("Is account non-expired for user {}: {}", user.getUsername(), user.isAccountNonExpired());
        log.info("Is account non-locked for user {}: {}", user.getUsername(), user.isAccountNonLocked());
        log.info("Is credentials non-expired for user {}: {}", user.getUsername(), user.isCredentialsNonExpired());
        log.info("Is account enabled for user {}: {}", user.getUsername(), user.isEnabled());

        return user;
    }
     @Override
 public UserResponse getUserResponseById(Integer id) {
     User user = userRepository.findById(id)
             .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
     // Đảm bảo UserMapper map cả isLock
     return userMapper.toDTO(user);
 }
     @Override
 public UserResponse updateUserByAdmin(Integer id, UserUpdateRequest userUpdateRequest) {
     User user = userRepository.findById(id)
             .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

     user.setFirstName(userUpdateRequest.getFirstName());
     user.setLastName(userUpdateRequest.getLastName());
     user.setEmail(userUpdateRequest.getEmail());
     user.setPhone(userUpdateRequest.getPhone());

//      Nếu UserUpdateRequest có isLock và bạn muốn cập nhật qua đây:
      if (userUpdateRequest.getIsLock() != null) {
          user.setIsLock(userUpdateRequest.getIsLock());
      }

     User savedUser = userRepository.save(user);
     return userMapper.toDTO(savedUser);
 }
     @Override
 public UserResponse toggleUserLockStatus(Integer id) {
     User user = userRepository.findById(id)
             .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
     user.setIsLock(!user.getIsLock()); // Đảo ngược trạng thái khóa
     User savedUser = userRepository.save(user);
     // Đảm bảo UserMapper map cả isLock
     return userMapper.toDTO(savedUser);
 }


}
