package com.BTLJAVA.WebBanThucPhamKho;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

class Product {
    String name;
    String price;
    String imageUrl;
    String description;

    public Product(String name, String price, String imageUrl, String description) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
        this.description = description;
    }
}

public class WebCrawler {

    private static final String URL = "https://lanchi.vn/danh-muc/thuc-pham-kho-cac-loai/page/6/";
    private static final String DB_URL = "jdbc:mysql://localhost:3306/lanchi_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "123456"; // Thay bằng mật khẩu của bạn

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
                String price = product.select("span.woocommerce-Price-amount bdi").text();
                String imageUrl = product.select("img.wp-post-image").attr("src");
                String description = product.select("p.post-excerpt").text();

                productList.add(new Product(name, price, imageUrl, description));
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
            String sql = "INSERT INTO products (name, price, imageUrl, description) VALUES (?, ?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(sql);

            for (Product p : products) {
                statement.setString(1, p.name);
                statement.setString(2, p.price);
                statement.setString(3, p.imageUrl);
                statement.setString(4, p.description);
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

