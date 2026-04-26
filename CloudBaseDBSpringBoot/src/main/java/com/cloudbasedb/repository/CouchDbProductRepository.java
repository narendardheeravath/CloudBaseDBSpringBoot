package com.cloudbasedb.repository;

import com.cloudbasedb.entity.Product;
import com.cloudbasedb.exception.ProductNotFoundException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CouchDB implementation of ProductRepository.
 *
 * Uses CouchDB's REST HTTP API:
 *   POST  /{db}          → create document (CouchDB assigns UUID)
 *   PUT   /{db}/{id}     → update document (requires _rev)
 *   GET   /{db}/{id}     → fetch by ID
 *   GET   /{db}/_all_docs?include_docs=true → fetch all
 *   POST  /{db}/_find    → Mango query
 *   DELETE/{db}/{id}?rev → delete document
 */
@Repository
public class CouchDbProductRepository implements ProductRepository {

    private final RestTemplate couchDbRestTemplate;
    private final String couchDbBaseUrl;
    private final ObjectMapper objectMapper;

    public CouchDbProductRepository(RestTemplate couchDbRestTemplate,
                                    String couchDbBaseUrl,
                                    ObjectMapper objectMapper) {
        this.couchDbRestTemplate = couchDbRestTemplate;
        this.couchDbBaseUrl = couchDbBaseUrl;
        this.objectMapper = objectMapper;
    }

    // ── Write operations ──────────────────────────────────────────────────────

    @Override
    public Product save(Product product) {
        if (product.getId() == null || product.getId().isBlank()) {
            // New document — POST and let CouchDB assign a UUID
            ResponseEntity<JsonNode> response = couchDbRestTemplate.postForEntity(
                    couchDbBaseUrl, product, JsonNode.class);
            JsonNode body = response.getBody();
            product.setId(body.get("id").asText());
            product.setRev(body.get("rev").asText());
        } else {
            // Existing document — PUT with current _rev
            ResponseEntity<JsonNode> response = couchDbRestTemplate.exchange(
                    couchDbBaseUrl + "/" + product.getId(),
                    HttpMethod.PUT,
                    new HttpEntity<>(product),
                    JsonNode.class);
            product.setRev(response.getBody().get("rev").asText());
        }
        return product;
    }

    @Override
    public void deleteById(String id) {
        // CouchDB DELETE requires the current _rev to prevent conflicts
        Product product = findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
        couchDbRestTemplate.exchange(
                couchDbBaseUrl + "/" + id + "?rev=" + product.getRev(),
                HttpMethod.DELETE,
                null,
                JsonNode.class);
    }

    // ── Read operations ───────────────────────────────────────────────────────

    @Override
    public Optional<Product> findById(String id) {
        try {
            ResponseEntity<Product> response = couchDbRestTemplate.getForEntity(
                    couchDbBaseUrl + "/" + id, Product.class);
            return Optional.ofNullable(response.getBody());
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Product> findAll() {
        String url = couchDbBaseUrl + "/_all_docs?include_docs=true";
        ResponseEntity<JsonNode> response = couchDbRestTemplate.getForEntity(url, JsonNode.class);
        List<Product> products = new ArrayList<>();
        JsonNode rows = response.getBody().get("rows");
        if (rows != null) {
            for (JsonNode row : rows) {
                JsonNode doc = row.get("doc");
                if (doc == null) continue;
                String docId = doc.path("_id").asText("");
                if (docId.startsWith("_design/")) continue;   // skip design docs
                try {
                    products.add(objectMapper.treeToValue(doc, Product.class));
                } catch (Exception ignored) { /* skip malformed documents */ }
            }
        }
        return products;
    }

    @Override
    public Optional<Product> findByProductCode(String productCode) {
        List<Product> results = mangoQuery(Map.of("productCode", productCode));
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public boolean existsByProductCode(String productCode) {
        return !mangoQuery(Map.of("productCode", productCode)).isEmpty();
    }

    @Override
    public List<Product> findByNameContainingIgnoreCase(String name) {
        // $regex with (?i) flag for case-insensitive partial match
        return mangoQuery(Map.of("name", Map.of("$regex", "(?i)" + escapeRegex(name))));
    }

    @Override
    public List<Product> findByCategoryIgnoreCase(String category) {
        // Exact match, case-insensitive
        return mangoQuery(Map.of("category", Map.of("$regex", "(?i)^" + escapeRegex(category) + "$")));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Executes a CouchDB Mango query (POST /_find).
     * Indexes for queried fields must exist — see DataInitializer.
     */
    private List<Product> mangoQuery(Map<String, Object> selector) {
        Map<String, Object> request = Map.of("selector", selector);
        ResponseEntity<JsonNode> response = couchDbRestTemplate.postForEntity(
                couchDbBaseUrl + "/_find", request, JsonNode.class);
        List<Product> results = new ArrayList<>();
        JsonNode docs = response.getBody().get("docs");
        if (docs != null) {
            for (JsonNode doc : docs) {
                try {
                    results.add(objectMapper.treeToValue(doc, Product.class));
                } catch (Exception ignored) { /* skip malformed documents */ }
            }
        }
        return results;
    }

    /** Escapes regex special characters in user-supplied input to prevent ReDoS. */
    private String escapeRegex(String input) {
        return input.replaceAll("[.\\[\\]{}()*+?^$|\\\\]", "\\\\$0");
    }
}
