package com.cloudbasedb.config;

import com.cloudbasedb.entity.Product;
import com.cloudbasedb.repository.ProductRepository;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Runs once on startup to:
 *  1. Create Mango indexes on productCode, name, category
 *  2. Seed sample products if the database is empty
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final ProductRepository productRepository;
    private final RestTemplate couchDbRestTemplate;
    private final String couchDbBaseUrl;

    public DataInitializer(ProductRepository productRepository,
                           RestTemplate couchDbRestTemplate,
                           String couchDbBaseUrl) {
        this.productRepository = productRepository;
        this.couchDbRestTemplate = couchDbRestTemplate;
        this.couchDbBaseUrl = couchDbBaseUrl;
    }

    @Override
    public void run(ApplicationArguments args) {
        createIndexes();
        seedData();
    }

    // ── Index creation ────────────────────────────────────────────────────────

    private void createIndexes() {
        createIndex("productCode-index", List.of("productCode"));
        createIndex("name-index",        List.of("name"));
        createIndex("category-index",    List.of("category"));
    }

    private void createIndex(String name, List<String> fields) {
        try {
            Map<String, Object> body = Map.of(
                    "index", Map.of("fields", fields),
                    "name", name,
                    "type", "json");
            couchDbRestTemplate.postForEntity(couchDbBaseUrl + "/_index", body, JsonNode.class);
            log.info("CouchDB index '{}' ensured on fields {}", name, fields);
        } catch (Exception e) {
            log.warn("Could not create CouchDB index '{}': {}", name, e.getMessage());
        }
    }

    // ── Seed data ─────────────────────────────────────────────────────────────

    private void seedData() {
        if (!productRepository.findAll().isEmpty()) {
            log.info("CouchDB products database already contains data — skipping seed.");
            return;
        }
        log.info("Seeding initial product data into CouchDB...");
        List<Product> seeds = List.of(
                new Product("PROD-001", "Laptop Pro 15",       "Electronics", 1299.99, 50,  "High-performance laptop with 16GB RAM and 512GB SSD"),
                new Product("PROD-002", "Wireless Mouse",      "Accessories", 29.99,  200, "Ergonomic wireless mouse with long battery life"),
                new Product("PROD-003", "Mechanical Keyboard", "Accessories", 89.99,  150, "RGB mechanical keyboard with tactile switches"),
                new Product("PROD-004", "4K Monitor",          "Electronics", 499.99, 75,  "27-inch 4K UHD monitor with HDR support"),
                new Product("PROD-005", "USB-C Hub",           "Accessories", 49.99,  300, "7-in-1 USB-C hub with HDMI, USB 3.0, and PD charging")
        );
        seeds.forEach(p -> {
            productRepository.save(p);
            log.info("  Seeded: {} - {}", p.getProductCode(), p.getName());
        });
    }
}
