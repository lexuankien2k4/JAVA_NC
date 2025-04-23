package com.BTLJAVA.WebBanThucPhamKho.entity;

import org.springframework.security.core.userdetails.UserDetails;

public interface CustomUserDetails extends UserDetails {
    Integer getId();
}