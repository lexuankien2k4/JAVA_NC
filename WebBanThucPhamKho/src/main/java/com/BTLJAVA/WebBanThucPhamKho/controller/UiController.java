package com.BTLJAVA.WebBanThucPhamKho.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UiController {

    @GetMapping("/admin/manage-products")
    public String showProductManagementPage() {
        return "Quanlysanpham";

    }


}
