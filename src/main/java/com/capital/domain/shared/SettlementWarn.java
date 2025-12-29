package com.capital.domain.shared;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
@Getter
@Setter
@Table(name = "settlement_warns")
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"}) 
public class SettlementWarn {
	
	@Id
    @GeneratedValue(generator = "local-id-generator")
    @GenericGenerator(
        name = "local-id-generator",
        strategy = "com.capital.util.LocalIdIdentifierGenerator"
    )
	private Long id;
	
	@Column(name = "merchant_id", unique = true)
	private Long merchantId;
	
	@Column(name = "pre_balance",nullable = false)
	private BigDecimal preBalance = BigDecimal.ZERO;
	
	@Column(name = "balance",nullable = false)
	private BigDecimal balance = BigDecimal.ZERO;

	@Column(name = "daily_sales")
	private BigDecimal dailySales = BigDecimal.ZERO;
	
	@Column(name = "created_at")
    private LocalDateTime createdAt;

}
