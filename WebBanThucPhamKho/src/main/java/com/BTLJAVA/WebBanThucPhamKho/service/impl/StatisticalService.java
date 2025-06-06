package com.BTLJAVA.WebBanThucPhamKho.service.impl;

import com.BTLJAVA.WebBanThucPhamKho.repository.OrderDetailRepository;
import com.BTLJAVA.WebBanThucPhamKho.repository.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
@Slf4j
@Service
public class StatisticalService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    public Map<String, Double> getDailyRevenue(Date startDate, Date endDate) {
        List<Object[]> results = orderRepository.findDailyRevenue(startDate, endDate);
        Map<String, Double> dailyRevenue = new LinkedHashMap<>();
        for (Object[] row : results) {
            dailyRevenue.put(row[0].toString(), ((Number) row[1]).doubleValue());
        }
        return dailyRevenue;
    }

    public Map<String, Double> getMonthlyRevenue(int year) {
        List<Object[]> results = orderRepository.findMonthlyRevenue(year);
        Map<String, Double> monthlyRevenue = new LinkedHashMap<>();
        for (Object[] row : results) {
            monthlyRevenue.put(String.format("%d-%02d", (Integer) row[0], (Integer) row[1]), ((Number) row[2]).doubleValue());
        }
        return monthlyRevenue;
    }

    public Map<String, Double> getYearlyRevenue() {
        log.info("SERVICE - Attempting to fetch yearly revenue.");
        List<Object[]> results = orderRepository.findYearlyRevenue();
        log.info("SERVICE - Raw results from findYearlyRevenue: {}", results); // In ra dữ liệu thô
        Map<String, Double> yearlyRevenue = new LinkedHashMap<>();
        for (Object[] row : results) {
            log.info("SERVICE - Processing row: Year={}, TotalPrice={}", row[0], row[1]); // In từng hàng
            // Đảm bảo kiểu dữ liệu hợp lệ trước khi ép kiểu
            if (row[0] instanceof Number && row[1] instanceof Number) {
                yearlyRevenue.put(row[0].toString(), ((Number) row[1]).doubleValue());
            } else {
                log.error("SERVICE - Invalid data type in yearly revenue result: Year type {}, Price type {}",
                        row[0] != null ? row[0].getClass().getName() : "null",
                        row[1] != null ? row[1].getClass().getName() : "null");
                // Có thể ném một ngoại lệ hoặc xử lý khác ở đây
            }
        }
        log.info("SERVICE - Yearly revenue data prepared: {}", yearlyRevenue);
        return yearlyRevenue;
    }

    public Map<String, Double> getWeeklyRevenue(int year) {
        List<Object[]> results = orderRepository.findWeeklyRevenue(year);
        Map<String, Double> weeklyRevenue = new LinkedHashMap<>();
        for (Object[] row : results) {
            weeklyRevenue.put(String.format("Tuần %d, năm %d", (Integer) row[1], (Integer) row[0]), ((Number) row[2]).doubleValue());
        }
        return weeklyRevenue;
    }

    public Map<String, Long> getTop5SoldProducts() {
        List<Object[]> results = orderDetailRepository.findTop5SoldProducts();
        Map<String, Long> topProducts = new LinkedHashMap<>();
        for (Object[] row : results) {
            topProducts.put(row[0].toString(), ((Number) row[1]).longValue());
        }
        return topProducts;
    }
}