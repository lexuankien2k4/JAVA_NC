package com.BTLJAVA.WebBanThucPhamKho.service;

import com.BTLJAVA.WebBanThucPhamKho.dto.request.AddressRequest;
import com.BTLJAVA.WebBanThucPhamKho.dto.response.AddressResponse;
import com.BTLJAVA.WebBanThucPhamKho.entity.Address;

public interface AddressService {
    /**
     * Creates and saves an Address entity from an AddressRequest.
     * This method is primarily used by OrderService when placing an order.
     *
     * @param addressRequest The DTO containing address information.
     * @return The saved Address entity.
     */
    Address createAddress(AddressRequest addressRequest);

    /**
     * Retrieves an address by its ID and converts it to AddressResponse.
     *
     * @param addressId The ID of the address to retrieve.
     * @return AddressResponse containing the address details.
     * @throws jakarta.persistence.EntityNotFoundException if address is not found.
     */
    AddressResponse getAddressResponseById(Integer addressId);

    /**
     * Retrieves an Address entity by its ID.
     *
     * @param addressId The ID of the address.
     * @return The Address entity.
     * @throws jakarta.persistence.EntityNotFoundException if address is not found.
     */
    Address getAddressEntityById(Integer addressId);

    // You can add update and delete methods if addresses are managed independently
    // AddressResponse updateAddress(Integer addressId, AddressRequest addressRequest);
    // void deleteAddress(Integer addressId);
}