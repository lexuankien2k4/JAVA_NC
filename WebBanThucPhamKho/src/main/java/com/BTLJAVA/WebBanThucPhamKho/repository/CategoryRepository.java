package com.BTLJAVA.WebBanThucPhamKho.repository;


import com.BTLJAVA.WebBanThucPhamKho.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
}
