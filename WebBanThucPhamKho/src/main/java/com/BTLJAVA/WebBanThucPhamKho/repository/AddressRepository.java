package com.BTLJAVA.WebBanThucPhamKho.repository;


import com.BTLJAVA.WebBanThucPhamKho.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Integer> {
}
