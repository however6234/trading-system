package com.capital.domain.merchant;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.capital.domain.product.Product;
import com.capital.domain.shared.Account;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "merchants")
@Getter
@Setter
public class Merchant {
    @Id
    @GeneratedValue(generator = "local-id-generator")
    @GenericGenerator(
        name = "local-id-generator",
        strategy = "com.capital.util.LocalIdIdentifierGenerator"
    )
	private Long id;
    
    @Column(name = "name")
    private String name;
    @Column(name = "code")
    private String code;
    
    @Column(name = "account_id", insertable = false, updatable = false, unique = true)
    private Long accountId;
    
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private Account account;
    
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "merchant_id")
    private List<Product> products = new ArrayList<>();
    
    public void addProduct(Product product) {
        this.products.add(product);
    }
    
    public Product findProductBySku(String sku) {
        return this.products.stream()
                .filter(p -> p.getSku().equals(sku))
                .findFirst()
                .orElse(null);
    }
    public void addBalance(BigDecimal amount) {
        this.account.addBalance(amount);
    }
    
    public void resetDailySales() {
        this.account.resetDailySales();
    }
}