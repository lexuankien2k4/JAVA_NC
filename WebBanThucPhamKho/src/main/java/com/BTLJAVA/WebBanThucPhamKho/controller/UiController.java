package com.BTLJAVA.WebBanThucPhamKho.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UiController {

    @GetMapping("/admin/manage-products")
    public String showProductManagementPage() {
        return "Quanlysanpham";

    }
    @GetMapping("/login")
    public String login() {
        return "login";

    }
    @GetMapping("/admin/manage-customer")
    public String showCustomerManagementPage() {
        return "Quanlykhachhang";

    }
    @GetMapping("/admin/dashboard")
    public String showManagementPage() {
        return "Dashboard";

    }
    @GetMapping("/home")
    public String showHomePage() {
        return "Trangchu";

    }
    @GetMapping("/cart")
    public String showCartPage() {
        return "cart";

    }
    @GetMapping("/checkout")
    public String showCheckoutPage() {
        return "checkout";

    }
    @GetMapping("/order-confirmation")
    public String showConfirmationPage() {
        return "order-confirmation";

    }
    @GetMapping("/admin/order-management")
    public String showOrderManagementPage() {
        return "quanlydonhang";

    }
    @GetMapping("/admin/statistics")
    public String showStatisticstPage() {
        return "thongke";

    }





}
