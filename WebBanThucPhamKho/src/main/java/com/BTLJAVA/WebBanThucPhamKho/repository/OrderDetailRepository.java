
 package com.BTLJAVA.WebBanThucPhamKho.repository;

 import com.BTLJAVA.WebBanThucPhamKho.entity.OrderDetail;
 import org.springframework.data.jpa.repository.JpaRepository;
 import org.springframework.data.jpa.repository.Query;
 import org.springframework.stereotype.Repository;
 import java.util.List;

 @Repository
 public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
  @Query("SELECT od.product.name, SUM(od.quantity) as totalSold FROM OrderDetail od GROUP BY od.product.name ORDER BY totalSold DESC LIMIT 5")
  List<Object[]> findTop5SoldProducts();
 }