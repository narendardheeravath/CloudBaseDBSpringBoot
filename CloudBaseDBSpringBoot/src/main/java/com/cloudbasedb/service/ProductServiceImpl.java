package com.cloudbasedb.service;

import com.cloudbasedb.dto.ProductRequest;
import com.cloudbasedb.dto.ProductResponse;
import com.cloudbasedb.entity.Product;
import com.cloudbasedb.exception.ProductNotFoundException;
import com.cloudbasedb.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsByProductCode(request.getProductCode())) {
            throw new IllegalArgumentException(
                    "A product with code '" + request.getProductCode() + "' already exists");
        }
        Product saved = productRepository.save(
                new Product(request.getProductCode(), request.getName(), request.getCategory(),
                        request.getPrice(), request.getStock(), request.getDescription()));
        return toResponse(saved);
    }

    @Override
    public ProductResponse getProductById(String id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    public List<ProductResponse> getProductsByName(String name) {
        return productRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getProductsByCategory(String category) {
        return productRepository.findByCategoryIgnoreCase(category)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public ProductResponse updateProduct(String id, ProductRequest request) {
        Product product = findOrThrow(id);

        // If productCode is changing, ensure the new one is not taken by another document
        if (!product.getProductCode().equals(request.getProductCode())
                && productRepository.existsByProductCode(request.getProductCode())) {
            throw new IllegalArgumentException(
                    "A product with code '" + request.getProductCode() + "' already exists");
        }

        product.setProductCode(request.getProductCode());
        product.setName(request.getName());
        product.setCategory(request.getCategory());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setDescription(request.getDescription());

        return toResponse(productRepository.save(product));
    }

    @Override
    public void deleteProduct(String id) {
        productRepository.deleteById(id);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Product findOrThrow(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getProductCode(),
                product.getName(),
                product.getCategory(),
                product.getPrice(),
                product.getStock(),
                product.getDescription());
    }
}

