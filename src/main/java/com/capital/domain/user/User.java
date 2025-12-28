package com.capital.domain.user;

import java.math.BigDecimal;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

import com.capital.domain.shared.Account;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) 
public class User {
    @Id
    @GeneratedValue(generator = "local-id-generator")
    @GenericGenerator(
        name = "local-id-generator",
        strategy = "com.capital.util.LocalIdIdentifierGenerator"
    )
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String userName;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(name = "account_id", insertable = false, updatable = false, unique = true)
    private Long accountId;
    
    @Column(name = "is_active")
    private boolean active = true;
    
    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true,fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    private Account account;
    
    public void recharge(BigDecimal amount) {
        if (this.account == null) {
            throw new IllegalStateException("User has no associated account");
        }
        this.account.recharge(amount);
    }
    
    public boolean deduct(BigDecimal amount) {
        if (this.account == null) {
            throw new IllegalStateException("User has no associated account");
        }
        return this.account.deduct(amount);
    }
}
