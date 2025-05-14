package com.BTLJAVA.WebBanThucPhamKho.service.impl;


import com.BTLJAVA.WebBanThucPhamKho.dto.request.AddressRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.AddressResponse;
import com.BTLJAVA.WebBanThucPhamKho.entity.Address;
import com.BTLJAVA.WebBanThucPhamKho.mapper.AddressMapper;
import com.BTLJAVA.WebBanThucPhamKho.repository.AddressRepository;
import com.BTLJAVA.WebBanThucPhamKho.service.AddressService;
import com.BTLJAVA.WebBanThucPhamKho.util.TypeStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;

    @Override
    public AddressResponse createAddress(AddressRequest addressRequest) {

        Address address = addressMapper.toEntity(addressRequest);

        address = addressRepository.save(address);

        return addressMapper.toDTO(address);
    }

    @Override
    public AddressResponse updateAddress(Integer id, AddressRequest addressRequest) {
        Address address = addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Address not found"));

        if (!address.getOrder().getStatus().equals(TypeStatus.PENDING.name())) throw new RuntimeException("The order is being shipped");

        address.setNumber(addressRequest.getNumber());
        address.setStreet(addressRequest.getStreet());
        address.setWard(addressRequest.getWard());
        address.setDistrict(addressRequest.getDistrict());
        address.setCity(addressRequest.getCity());

        address = addressRepository.save(address);

        return addressMapper.toDTO(address);
    }
}
