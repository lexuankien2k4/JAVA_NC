package com.BTLJAVA.WebBanThucPhamKho.service;


import com.BTLJAVA.WebBanThucPhamKho.dto.request.AddressRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.AddressResponse;

public interface AddressService {

    AddressResponse createAddress(AddressRequest addressRequest);

    AddressResponse updateAddress(Integer id, AddressRequest addressRequest);
}
