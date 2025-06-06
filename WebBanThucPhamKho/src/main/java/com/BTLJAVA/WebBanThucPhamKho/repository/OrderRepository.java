package com.BTLJAVA.WebBanThucPhamKho.repository;

import com.BTLJAVA.WebBanThucPhamKho.entity.Order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.stereotype.Repository;


import java.util.Date;
import java.util.List;


@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUserIdOrderByCreateAtDesc(Integer userId);

    // Thống kê doanh thu theo ngày
    @Query("SELECT FUNCTION('DATE', o.createAt), SUM(o.totalPrice) FROM Order o WHERE FUNCTION('DATE', o.createAt) BETWEEN :startDate AND :endDate GROUP BY FUNCTION('DATE', o.createAt)")
    List<Object[]> findDailyRevenue(Date startDate, Date endDate);

    // Thống kê doanh thu theo tháng
    @Query("SELECT FUNCTION('YEAR', o.createAt), FUNCTION('MONTH', o.createAt), SUM(o.totalPrice) FROM Order o WHERE FUNCTION('YEAR', o.createAt) = :year GROUP BY FUNCTION('YEAR', o.createAt), FUNCTION('MONTH', o.createAt) ORDER BY FUNCTION('MONTH', o.createAt) ASC")
    List<Object[]> findMonthlyRevenue(int year);

    // Thống kê doanh thu theo năm
    @Query("SELECT FUNCTION('YEAR', o.createAt), SUM(o.totalPrice) FROM Order o GROUP BY FUNCTION('YEAR', o.createAt) ORDER BY FUNCTION('YEAR', o.createAt) ASC")
    List<Object[]> findYearlyRevenue();

    // Thống kê doanh thu theo tuần (trong năm) - Cần điều chỉnh tùy thuộc vào cách bạn định nghĩa tuần
    // Đây là ví dụ cho tuần của năm, bạn có thể cần hàm phức tạp hơn nếu muốn tuần theo lịch
    @Query("SELECT FUNCTION('YEAR', o.createAt), FUNCTION('WEEK', o.createAt), SUM(o.totalPrice) FROM Order o WHERE FUNCTION('YEAR', o.createAt) = :year GROUP BY FUNCTION('YEAR', o.createAt), FUNCTION('WEEK', o.createAt) ORDER BY FUNCTION('WEEK', o.createAt) ASC")
    List<Object[]> findWeeklyRevenue(int year);
}
