package com.BTLJAVA.WebBanThucPhamKho.service.impl;

import com.BTLJAVA.WebBanThucPhamKho.dto.request.AddressRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.AddressResponse;
import com.BTLJAVA.WebBanThucPhamKho.entity.Address;
import com.BTLJAVA.WebBanThucPhamKho.mapper.AddressMapper;
import com.BTLJAVA.WebBanThucPhamKho.repository.AddressRepository;
import com.BTLJAVA.WebBanThucPhamKho.service.AddressService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;

    @Override
    @Transactional // Ensure atomicity when saving
    public Address createAddress(AddressRequest addressRequest) {
        if (addressRequest == null) {
            log.error("SERVICE - createAddress - AddressRequest cannot be null.");
            throw new IllegalArgumentException("Thông tin địa chỉ không được để trống.");
        }
        log.info("SERVICE - createAddress - Creating address from request: {}", addressRequest);
        Address addressEntity = addressMapper.requestToEntity(addressRequest);

        // The 'order' field in Address entity is not set here by default,
        // as this service is generic. The Order entity is responsible for linking
        // via its 'address' field and CascadeType.ALL.
        // If an Address could be linked to an Order upon creation via AddressService,
        // you would need to pass Order information or ID here.

        Address savedAddress = addressRepository.save(addressEntity);
        log.info("SERVICE - createAddress - Address saved successfully with ID: {}", savedAddress.getId());
        return savedAddress;
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getAddressResponseById(Integer addressId) {
        log.info("SERVICE - getAddressResponseById - Fetching address with ID: {}", addressId);
        if (addressId == null) {
            log.warn("SERVICE - getAddressResponseById - Address ID is null.");
            throw new IllegalArgumentException("ID địa chỉ không được để trống.");
        }
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> {
                    log.warn("SERVICE - getAddressResponseById - Address not found with ID: {}", addressId);
                    return new EntityNotFoundException("Không tìm thấy địa chỉ với ID: " + addressId);
                });
        return addressMapper.entityToResponse(address);
    }

    @Override
    @Transactional(readOnly = true)
    public Address getAddressEntityById(Integer addressId) {
        log.info("SERVICE - getAddressEntityById - Fetching address entity with ID: {}", addressId);
        if (addressId == null) {
            log.warn("SERVICE - getAddressEntityById - Address ID is null.");
            throw new IllegalArgumentException("ID địa chỉ không được để trống.");
        }
        return addressRepository.findById(addressId)
                .orElseThrow(() -> {
                    log.warn("SERVICE - getAddressEntityById - Address entity not found with ID: {}", addressId);
                    return new EntityNotFoundException("Không tìm thấy địa chỉ với ID: " + addressId);
                });
    }

    // TODO: Implement updateAddress if needed
    // @Override
    // @Transactional
    // public AddressResponse updateAddress(Integer addressId, AddressRequest addressRequest) {
    //     log.info("SERVICE - updateAddress - Updating address with ID: {}", addressId);
    //     Address existingAddress = getAddressEntityById(addressId); // Reuse to get entity or throw not found
    //
    //     // Update fields from request
    //     existingAddress.setNumber(addressRequest.getNumber());
    //     existingAddress.setStreet(addressRequest.getStreet());
    //     existingAddress.setWard(addressRequest.getWard());
    //     existingAddress.setDistrict(addressRequest.getDistrict());
    //     existingAddress.setCity(addressRequest.getCity());
    //
    //     Address updatedAddress = addressRepository.save(existingAddress);
    //     log.info("SERVICE - updateAddress - Address updated successfully with ID: {}", updatedAddress.getId());
    //     return addressMapper.entityToResponse(updatedAddress);
    // }

    // TODO: Implement deleteAddress if needed, consider constraints
    // @Override
    // @Transactional
    // public void deleteAddress(Integer addressId) {
    //     log.info("SERVICE - deleteAddress - Deleting address with ID: {}", addressId);
    //     Address addressToDelete = getAddressEntityById(addressId);
    //     // Check if this address is associated with any order
    //     if (addressToDelete.getOrder() != null) {
    //         log.warn("SERVICE - deleteAddress - Attempt to delete address ID {} which is associated with order ID {}", addressId, addressToDelete.getOrder().getId());
    //         throw new IllegalStateException("Không thể xóa địa chỉ đang được sử dụng bởi một đơn hàng.");
    //     }
    //     addressRepository.delete(addressToDelete);
    //     log.info("SERVICE - deleteAddress - Address deleted successfully with ID: {}", addressId);
    // }
}