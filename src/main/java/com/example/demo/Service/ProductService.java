package com.example.demo.Service;

import com.example.demo.Entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductService {
    Optional<Product> findById(Long id);

    List<Product> findByName(String name);

    Page<Product> findByActiveTrue(Pageable pageable);

    Page<Product> findByActiveFalse(Pageable pageable);


    Page<Product> getAllProducts(Pageable pageable);

    Page<Product> getProductsByCategory(Long categoryId, Pageable pageable);

    Product save(Product product);
    Page<Product> searchProducts(String keyword, String sortByPrice, Long categoryId, Pageable pageable);
    Optional<Product> update(Long id, Product updatedProduct);

    boolean delete(Long id);
    long count();
    Page<Product> searchProductsForAdmin(String keyword, boolean isActive, Pageable pageable);
    
}
