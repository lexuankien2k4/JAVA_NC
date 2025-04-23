package com.BTLJAVA.WebBanThucPhamKho.service;


import com.BTLJAVA.WebBanThucPhamKho.entity.User;
import com.BTLJAVA.WebBanThucPhamKho.util.TypeToken;

public interface JwtService {
    String generateToken(User user, TypeToken typeToken);

    String verifyTokenAndExtractUserName(String token, TypeToken typeToken);
}
