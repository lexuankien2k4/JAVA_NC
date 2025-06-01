package com.BTLJAVA.WebBanThucPhamKho.controller;

import com.BTLJAVA.WebBanThucPhamKho.dto.response.AddressResponse;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.ResponseData;
import com.BTLJAVA.WebBanThucPhamKho.service.AddressService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize; // Import nếu dùng
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
@Slf4j
public class AddressController {

    private final AddressService addressService;

    /**
     * Lấy thông tin chi tiết của một địa chỉ bằng ID.
     * Cần được bảo vệ nếu thông tin địa chỉ là nhạy cảm.
     *
     * @param addressId ID của địa chỉ cần lấy.
     * @return ResponseData chứa AddressResponse.
     */
    @GetMapping("/{addressId}")
    // Ví dụ phân quyền: Chỉ Admin hoặc người dùng liên quan (thông qua một service kiểm tra phức tạp hơn) mới được xem
    // @PreAuthorize("hasAuthority('ADMIN') or @customSecurityService.canAccessAddress(authentication, #addressId)")
    public ResponseData<AddressResponse> getAddressById(@PathVariable Integer addressId) {
        log.info("CONTROLLER - API GET /addresses/{} - getAddressById called", addressId);
        try {
            AddressResponse addressResponse = addressService.getAddressResponseById(addressId);
            // Service nên ném EntityNotFoundException nếu không tìm thấy
            // Việc kiểm tra null ở đây là một lớp phòng thủ bổ sung.
            if (addressResponse == null) {
                log.warn("CONTROLLER - getAddressById - Service returned null for ID: {}. This should ideally be an exception from service.", addressId);
                return ResponseData.<AddressResponse>builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .message("Không tìm thấy địa chỉ với ID: " + addressId)
                        .error("ResourceNotFound")
                        .build();
            }
            return ResponseData.<AddressResponse>builder()
                    .status(HttpStatus.OK.value())
                    .message("Lấy thông tin địa chỉ thành công.")
                    .data(addressResponse)
                    .build();
        } catch (EntityNotFoundException enfe) {
            log.warn("CONTROLLER - getAddressById - EntityNotFoundException for ID {}: {}", addressId, enfe.getMessage());
            return ResponseData.<AddressResponse>builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .message(enfe.getMessage())
                    .error(enfe.getClass().getSimpleName())
                    .build();
        } catch (AccessDeniedException ade) { // Nếu bạn có logic phân quyền trong service
            log.warn("CONTROLLER - getAddressById - AccessDeniedException for ID {}: {}", addressId, ade.getMessage());
            return ResponseData.<AddressResponse>builder()
                    .status(HttpStatus.FORBIDDEN.value())
                    .message("Bạn không có quyền truy cập tài nguyên này.")
                    .error(ade.getClass().getSimpleName())
                    .build();
        } catch (Exception e) {
            log.error("CONTROLLER - API GET /addresses/{} - Unexpected Exception: {}", addressId, e.getMessage(), e);
            return ResponseData.<AddressResponse>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi server khi lấy thông tin địa chỉ: " + e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build();
        }
    }
}