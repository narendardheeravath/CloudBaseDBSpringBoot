package com.cloudbasedb.service;

import com.cloudbasedb.dto.ProductRequest;
import com.cloudbasedb.dto.ProductResponse;

import java.util.List;

public interface ProductService {

    /** Create and persist a new product. Throws IllegalArgumentException if productCode already exists. */
    ProductResponse createProduct(ProductRequest request);

    /** Retrieve a product by its CouchDB UUID. Throws ProductNotFoundException if absent. */
    ProductResponse getProductById(String id);

    /** Retrieve all products whose name contains the given search string (case-insensitive). */
    List<ProductResponse> getProductsByName(String name);

    /** Retrieve all products belonging to the given category (case-insensitive). */
    List<ProductResponse> getProductsByCategory(String category);

    /** Retrieve all products. */
    List<ProductResponse> getAllProducts();

    /** Update an existing product identified by its CouchDB UUID. Throws ProductNotFoundException if absent. */
    ProductResponse updateProduct(String id, ProductRequest request);

    /** Delete a product by its CouchDB UUID. Throws ProductNotFoundException if absent. */
    void deleteProduct(String id);
}
