package com.cloudbasedb.repository;

import com.cloudbasedb.entity.Product;

import java.util.List;
import java.util.Optional;

/**
 * CouchDB-backed product repository.
 * IDs are CouchDB UUIDs (String), not auto-incremented longs.
 */
public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(String id);

    Optional<Product> findByProductCode(String productCode);

    boolean existsByProductCode(String productCode);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategoryIgnoreCase(String category);

    List<Product> findAll();

    void deleteById(String id);
}

