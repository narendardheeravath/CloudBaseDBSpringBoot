package com.cloudbasedb.dto;

public class ProductResponse {

    private String id;
    private String productCode;
    private String name;
    private String category;
    private Double price;
    private Integer stock;
    private String description;

    public ProductResponse() {}

    public ProductResponse(String id, String productCode, String name, String category,
                           Double price, Integer stock, String description) {
        this.id = id;
        this.productCode = productCode;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.description = description;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
