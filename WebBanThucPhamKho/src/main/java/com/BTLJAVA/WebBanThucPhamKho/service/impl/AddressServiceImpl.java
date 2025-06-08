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
    @Transactional
    public Address createAddress(AddressRequest addressRequest) {
        if (addressRequest == null) {
            log.error("SERVICE - createAddress - AddressRequest cannot be null.");
            throw new IllegalArgumentException("Thông tin địa chỉ không được để trống.");
        }
        log.info("SERVICE - createAddress - Creating address from request: {}", addressRequest);
        Address addressEntity = addressMapper.requestToEntity(addressRequest);

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

}