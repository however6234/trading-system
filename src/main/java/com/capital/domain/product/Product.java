package com.capital.domain.product;
import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) 
public class Product {
    @Id
    @GeneratedValue(generator = "local-id-generator")
    @GenericGenerator(
        name = "local-id-generator",
        strategy = "com.capital.util.LocalIdIdentifierGenerator"
    )
	private Long id;
    
    @Column(name = "sku")
    private String sku;
    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    @Column(name = "price")
    private BigDecimal price;
    @Column(name = "stock_quantity")
    private Integer stockQuantity;
    @Column(name = "merchant_id")
    private Long merchantId;
    
    public boolean reduceStock(Integer quantity) {
        if (this.stockQuantity >= quantity) {
            this.stockQuantity -= quantity;
            return true;
        }
        return false;
    }
    
    public void increaseStock(Integer quantity) {
        this.stockQuantity += quantity;
    }
    
    public BigDecimal calculateTotalPrice(Integer quantity) {
        return this.price.multiply(BigDecimal.valueOf(quantity));
    }
    public boolean isAvailable(Integer quantity) {
        return this.stockQuantity >= quantity && quantity > 0;
    }
}