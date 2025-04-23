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
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Not found"));
    }
}
