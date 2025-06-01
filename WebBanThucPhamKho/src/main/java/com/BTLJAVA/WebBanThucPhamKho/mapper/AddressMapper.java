package com.BTLJAVA.WebBanThucPhamKho.mapper;

import com.BTLJAVA.WebBanThucPhamKho.dto.request.AddressRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.AddressResponse;
import com.BTLJAVA.WebBanThucPhamKho.entity.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public Address requestToEntity(AddressRequest req) {
        if (req == null) return null;
        return Address.builder()
                .number(req.getNumber())
                .street(req.getStreet())
                .ward(req.getWard())
                .district(req.getDistrict())
                .city(req.getCity())
                .build();
    }

    public AddressResponse entityToResponse(Address address) {
        if (address == null) return null;

        String fullAddress = String.join(", ",
                address.getNumber() != null ? address.getNumber() : "",
                address.getStreet() != null ? address.getStreet() : "",
                address.getWard() != null ? address.getWard() : "",
                address.getDistrict() != null ? address.getDistrict() : "",
                address.getCity() != null ? address.getCity() : ""
        ).replaceAll(", , ", ", ").replaceAll("^, |,$", "").trim(); // Dọn dẹp dấu phẩy thừa

        return AddressResponse.builder()
                .id(address.getId())
                .number(address.getNumber())
                .street(address.getStreet())
                .ward(address.getWard())
                .district(address.getDistrict())
                .city(address.getCity())
                .fullAddress(fullAddress)
                // Không còn receiverName, phone, email ở đây
                .build();
    }
}
