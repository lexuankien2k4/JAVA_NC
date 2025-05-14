package com.BTLJAVA.WebBanThucPhamKho;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

class Product {
    String name;
    Integer price;
    String imageUrl;
    String description;
    Date manufactureDate;
    Date expiryDate;
    int stockQuantity;
    int soldQuantity;

    public Product(String name, Integer price, String imageUrl, String description,
                   Date manufactureDate, Date expiryDate, int stockQuantity, int soldQuantity) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.description = description;
        this.manufactureDate = manufactureDate;
        this.expiryDate = expiryDate;
        this.stockQuantity = stockQuantity;
        this.soldQuantity = soldQuantity;
    }
}

public class WebCrawler {

    private static final String URL = "https://lanchi.vn/danh-muc/thuc-pham-kho-cac-loai/page/6/";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/thucphamkho?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "123456"; // Thay bằng mật khẩu thật

    public static void main(String[] args) {
        List<Product> products = crawlData();
        saveToDatabase(products);
    }

    public static List<Product> crawlData() {
        List<Product> productList = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(URL).get();
            Elements products = doc.select("ul.products li.product-col");

            for (Element product : products) {
                String name = product.select("h3.woocommerce-loop-product__title").text();

                // Xử lý giá
                String priceText = product.select("span.woocommerce-Price-amount bdi").text()
                        .replace("₫", "").replace(",", "").trim();
                Integer price = Integer.parseInt(priceText);

                String imageUrl = product.select("img.wp-post-image").attr("src");
                String description = product.select("p.post-excerpt").text();

                // Ngày sản xuất và hết hạn
                Date manufactureDate = null;
                Date expiryDate = null;
                Elements timeElements = product.select("div.khuyenmai, div.post-content, p");
                for (Element el : timeElements) {
                    String text = el.text();
                    if (text.contains("Thời gian áp dụng")) {
                        String[] dates = text.replace("Thời gian áp dụng", "").trim().split("–");
                        if (dates.length == 2) {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                            manufactureDate = sdf.parse(dates[0].trim());
                            expiryDate = sdf.parse(dates[1].trim());
                        }
                        break;
                    }
                }

                // Giá trị mặc định cho tồn kho và đã bán
                int stockQuantity = 100;
                int soldQuantity = 0;

                productList.add(new Product(name, price, imageUrl, description,
                        manufactureDate, expiryDate, stockQuantity, soldQuantity));
            }

            System.out.println("Crawled " + productList.size() + " products successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error crawling data: " + e.getMessage());
        }
        return productList;
    }

    public static void saveToDatabase(List<Product> products) {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            String sql = "INSERT INTO products (name, price, image_url, description, manufacture_date, expiry_date, stock_quantity, sold_quantity) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(sql);

            for (Product p : products) {
                statement.setString(1, p.name);
                statement.setDouble(2, p.price);
                statement.setString(3, p.imageUrl);
                statement.setString(4, p.description);
                if (p.manufactureDate != null) {
                    statement.setDate(5, new java.sql.Date(p.manufactureDate.getTime()));
                } else {
                    statement.setNull(5, Types.DATE);
                }
                if (p.expiryDate != null) {
                    statement.setDate(6, new java.sql.Date(p.expiryDate.getTime()));
                } else {
                    statement.setNull(6, Types.DATE);
                }
                statement.setInt(7, p.stockQuantity);
                statement.setInt(8, p.soldQuantity);

                statement.addBatch();
            }

            statement.executeBatch();
            System.out.println("Saved " + products.size() + " products to database!");

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error saving data: " + e.getMessage());
        }
    }
}