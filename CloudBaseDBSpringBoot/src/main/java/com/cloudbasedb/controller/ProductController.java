package com.cloudbasedb.controller;

import com.cloudbasedb.dto.ProductRequest;
import com.cloudbasedb.dto.ProductResponse;
import com.cloudbasedb.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Product Management", description = "Create, retrieve, update and delete product records stored in the cloud database")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    @PostMapping
    @Operation(
            summary = "Create a new product",
            description = "Insert a new product record. The productCode must be unique.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Product created successfully",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error — missing or invalid fields",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "A product with the given productCode already exists",
                    content = @Content)
    })
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    // ── READ by ID ────────────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(
            summary = "Get product by numeric ID",
            description = "Retrieve a single product record by its auto-generated numeric primary key.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "No product found for the given ID",
                    content = @Content)
    })
    public ResponseEntity<ProductResponse> getProductById(
            @Parameter(description = "CouchDB UUID of the product", example = "3a1b2c3d-...")
            @PathVariable String id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // ── READ all ──────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(
            summary = "Get all products",
            description = "Retrieve every product record currently stored in the database.")
    @ApiResponse(responseCode = "200", description = "Full list of products (may be empty)")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // ── SEARCH by name ────────────────────────────────────────────────────────

    @GetMapping("/search")
    @Operation(
            summary = "Search products by name",
            description = "Retrieve all products whose name contains the given search string (case-insensitive, partial match supported).")
    @ApiResponse(responseCode = "200", description = "List of matching products (may be empty)")
    public ResponseEntity<List<ProductResponse>> getProductsByName(
            @Parameter(description = "Full or partial name to search for", example = "Laptop")
            @RequestParam String name) {
        return ResponseEntity.ok(productService.getProductsByName(name));
    }

    // ── FILTER by category ────────────────────────────────────────────────────

    @GetMapping("/category/{category}")
    @Operation(
            summary = "Get products by category",
            description = "Retrieve all products belonging to a specific category (case-insensitive).")
    @ApiResponse(responseCode = "200", description = "List of products in the category (may be empty)")
    public ResponseEntity<List<ProductResponse>> getProductsByCategory(
            @Parameter(description = "Category name", example = "Electronics")
            @PathVariable String category) {
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @Operation(
            summary = "Update product by numeric ID",
            description = "Replace all fields of an existing product identified by its numeric primary key.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product updated successfully",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error — missing or invalid fields",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "No product found for the given ID",
                    content = @Content),
            @ApiResponse(responseCode = "409", description = "The new productCode conflicts with an existing record",
                    content = @Content)
    })
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "CouchDB UUID of the product", example = "3a1b2c3d-...")
            @PathVariable String id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete product by numeric ID",
            description = "Permanently remove a product record identified by its numeric primary key.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            @ApiResponse(responseCode = "404", description = "No product found for the given ID",
                    content = @Content)
    })
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "CouchDB UUID of the product", example = "3a1b2c3d-...")
            @PathVariable String id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
