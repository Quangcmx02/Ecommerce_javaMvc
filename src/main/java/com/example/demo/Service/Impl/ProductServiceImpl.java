package com.example.demo.Service.Impl;

import com.example.demo.Entity.Product;
import com.example.demo.Repository.ProductRepository;
import com.example.demo.Service.LoggerService;
import com.example.demo.Service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private LoggerService loggerService;
    @Override
    public long count() {
        try {
            return productRepository.count();
        } catch (Exception e) {
            loggerService.logError("Lỗi khi đếm tổng số sản phẩm", e);
            return 0;
        }
    }
    @Override
    public Optional<Product> findById(Long id) {
        try {
            return productRepository.findByProductId(id);
        } catch (Exception e) {
            loggerService.logError("Lỗi khi tìm sản phẩm theo ID: " + id, e);
            return Optional.empty();
        }
    }
    @Override
    public List<Product> findByName(String name) {
        try {
            return productRepository.findByName(name);
        } catch (Exception e) {
            loggerService.logError("Lỗi khi tìm sản phẩm theo tên: " + name, e);
            return null;
        }
    }

    @Override
    public Page<Product> findByActiveTrue(Pageable pageable) {
        try {
            return productRepository.findByIsActiveTrue(pageable);
        } catch (Exception e) {
            loggerService.logError("Lỗi khi tìm sản phẩm đang hoạt động", e);
            return null;
        }
    }

    @Override
    public Page<Product> findByActiveFalse(Pageable pageable) {
        try {
            return productRepository.findByIsActiveFalse(pageable);
        } catch (Exception e) {
            loggerService.logError("Lỗi khi tìm sản phẩm không hoạt động", e);
            return Page.empty();
        }
    }


    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        try {
            return productRepository.findAll(pageable);
        } catch (Exception e) {
            loggerService.logError("Lỗi khi lấy danh sách sản phẩm có phân trang", e);
            return Page.empty();
        }
    }

    @Override
    public Page<Product> getProductsByCategory(Long categoryId, Pageable pageable) {
        try {
            return productRepository.findByCategory_CategoryId(categoryId, pageable);
        } catch (Exception e) {
            loggerService.logError("Lỗi khi tìm sản phẩm theo danh mục ID: " + categoryId, e);
            return Page.empty();
        }
    }

    @Override
    public Product save(Product product) {
        try {
            return productRepository.save(product);
        } catch (Exception e) {
            loggerService.logError("Lỗi khi thêm sản phẩm mới", e);
            return null;
        }
    }

    @Override
    public Optional<Product> update(Long id, Product updatedProduct) {
        try {
            return productRepository.findById(id).map(existingProduct -> {
                existingProduct.setName(updatedProduct.getName());
                existingProduct.setDescription(updatedProduct.getDescription());
                existingProduct.setPrice(updatedProduct.getPrice());
                existingProduct.setQuantity(updatedProduct.getQuantity());
                existingProduct.setImageLink(updatedProduct.getImageLink());
                existingProduct.setSize(updatedProduct.getSize());
                existingProduct.setStatus(updatedProduct.getStatus());
                existingProduct.setActive(updatedProduct.isActive());
                existingProduct.setCategory(updatedProduct.getCategory());

                return productRepository.save(existingProduct);
            });
        } catch (Exception e) {
            loggerService.logError("Lỗi khi cập nhật sản phẩm ID: " + id, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean delete(Long id) {
        try {
            if (productRepository.existsById(id)) {
                productRepository.deleteById(id);
                return true;
            } else {
                loggerService.logWarning("Không tìm thấy sản phẩm để xóa với ID: " + id);
                return false;
            }
        } catch (Exception e) {
            loggerService.logError("Lỗi khi xóa sản phẩm ID: " + id, e);
            return false;
        }
    }
    @Override
    public Page<Product> searchProducts(String keyword, String sortByPrice, Long categoryId, Pageable pageable) {
        try {
            Sort sort = Sort.unsorted();
            if (sortByPrice != null && !sortByPrice.isEmpty()) {
                sort = sortByPrice.equals("highToLow") ?
                        Sort.by(Sort.Direction.DESC, "price") :
                        Sort.by(Sort.Direction.ASC, "price");
            }

            Pageable sortedPageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    sort
            );

            if (categoryId != null) {
                // Lọc theo danh mục
                if (keyword == null || keyword.trim().isEmpty()) {
                    return productRepository.findByCategory_CategoryId(categoryId, sortedPageable);
                } else {
                    return productRepository.findByCategory_CategoryIdAndNameContainingIgnoreCase(
                            categoryId, keyword, sortedPageable);
                }
            } else {
                // Không có danh mục
                if (keyword == null || keyword.trim().isEmpty()) {
                    return productRepository.findByIsActiveTrue(sortedPageable);
                } else {
                    return productRepository.findByNameContainingIgnoreCaseAndIsActiveTrue(keyword, sortedPageable);
                }
            }
        } catch (Exception e) {
            loggerService.logError("Lỗi khi tìm kiếm sản phẩm: keyword=" + keyword + ", sortByPrice=" + sortByPrice + ", categoryId=" + categoryId, e);
            return Page.empty();
        }
    }@Override
    public Page<Product> searchProductsForAdmin(String keyword, boolean isActive, Pageable pageable) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return isActive ? productRepository.findByIsActiveTrue(pageable) :
                        productRepository.findByIsActiveFalse(pageable);
            }
            return productRepository.findByNameContainingIgnoreCaseAndIsActive(keyword, isActive, pageable);
        } catch (Exception e) {
            loggerService.logError("Lỗi khi tìm kiếm sản phẩm cho admin: keyword=" + keyword + ", isActive=" + isActive, e);
            return Page.empty();
        }
    }
    }


