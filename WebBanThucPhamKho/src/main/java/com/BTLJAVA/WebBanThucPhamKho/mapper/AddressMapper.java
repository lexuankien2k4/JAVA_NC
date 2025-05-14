package com.BTLJAVA.WebBanThucPhamKho.mapper;


import com.BTLJAVA.WebBanThucPhamKho.dto.request.AddressRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.AddressResponse;
import com.BTLJAVA.WebBanThucPhamKho.entity.Address;
import org.springframework.stereotype.Service;

@Service
public class AddressMapper {

    public AddressResponse toDTO(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .number(address.getNumber())
                .street(address.getStreet())
                .ward(address.getWard())
                .district(address.getDistrict())
                .city(address.getCity())
                .build();
    }

    public Address toEntity(AddressRequest addressDTO) {
        return Address.builder()
                .number(addressDTO.getNumber())
                .street(addressDTO.getStreet())
                .ward(addressDTO.getWard())
                .district(addressDTO.getDistrict())
                .city(addressDTO.getCity())
                .build();
    }
}
