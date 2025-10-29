package com.BTLJAVA.WebBanThucPhamKho.controller;

import com.BTLJAVA.WebBanThucPhamKho.dto.response.ResponseData;

import com.BTLJAVA.WebBanThucPhamKho.service.impl.StatisticalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
@Slf4j // Để sử dụng logger
public class StatisticalController {

    private final StatisticalService statisticalService; // Sử dụng final và @RequiredArgsConstructor

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/revenue/daily")
    public ResponseEntity<ResponseData<Map<String, Double>>> getDailyRevenue(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        try {
            Map<String, Double> data = statisticalService.getDailyRevenue(startDate, endDate);
            return ResponseEntity.ok(ResponseData.<Map<String, Double>>builder()
                    .status(HttpStatus.OK.value())
                    .message("Lấy dữ liệu doanh thu hàng ngày thành công.")
                    .data(data)
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseData.<Map<String, Double>>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Lỗi dữ liệu đầu vào: " + e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseData.<Map<String, Double>>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi hệ thống khi lấy dữ liệu doanh thu hàng ngày. Vui lòng thử lại sau.")
                    .error(e.getClass().getSimpleName())
                    .build());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/revenue/monthly")
    public ResponseEntity<ResponseData<Map<String, Double>>> getMonthlyRevenue(@RequestParam int year) {
        try {
            Map<String, Double> data = statisticalService.getMonthlyRevenue(year);
            return ResponseEntity.ok(ResponseData.<Map<String, Double>>builder()
                    .status(HttpStatus.OK.value())
                    .message("Lấy dữ liệu doanh thu hàng tháng thành công.")
                    .data(data)
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseData.<Map<String, Double>>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Lỗi dữ liệu đầu vào: " + e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseData.<Map<String, Double>>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi hệ thống khi lấy dữ liệu doanh thu hàng tháng. Vui lòng thử lại sau.")
                    .error(e.getClass().getSimpleName())
                    .build());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/revenue/yearly")
    public ResponseEntity<ResponseData<Map<String, Double>>> getYearlyRevenue() {
        try {
            Map<String, Double> data = statisticalService.getYearlyRevenue();
            return ResponseEntity.ok(ResponseData.<Map<String, Double>>builder()
                    .status(HttpStatus.OK.value())
                    .message("Lấy dữ liệu doanh thu hàng năm thành công.")
                    .data(data)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseData.<Map<String, Double>>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi hệ thống khi lấy dữ liệu doanh thu hàng năm. Vui lòng thử lại sau.")
                    .error(e.getClass().getSimpleName())
                    .build());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/revenue/weekly")
    public ResponseEntity<ResponseData<Map<String, Double>>> getWeeklyRevenue(@RequestParam int year) {
        try {
            Map<String, Double> data = statisticalService.getWeeklyRevenue(year);
            return ResponseEntity.ok(ResponseData.<Map<String, Double>>builder()
                    .status(HttpStatus.OK.value())
                    .message("Lấy dữ liệu doanh thu hàng tuần thành công.")
                    .data(data)
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseData.<Map<String, Double>>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .message("Lỗi dữ liệu đầu vào: " + e.getMessage())
                    .error(e.getClass().getSimpleName())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseData.<Map<String, Double>>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi hệ thống khi lấy dữ liệu doanh thu hàng tuần. Vui lòng thử lại sau.")
                    .error(e.getClass().getSimpleName())
                    .build());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/products/top5-sold")
    public ResponseEntity<ResponseData<Map<String, Long>>> getTop5SoldProducts() {
        try {
            Map<String, Long> data = statisticalService.getTop5SoldProducts();
            return ResponseEntity.ok(ResponseData.<Map<String, Long>>builder()
                    .status(HttpStatus.OK.value())
                    .message("Lấy dữ liệu top 5 sản phẩm bán chạy nhất thành công.")
                    .data(data)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ResponseData.<Map<String, Long>>builder()
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .message("Lỗi hệ thống khi lấy dữ liệu top 5 sản phẩm bán chạy nhất. Vui lòng thử lại sau.")
                    .error(e.getClass().getSimpleName())
                    .build());
        }
    }
}