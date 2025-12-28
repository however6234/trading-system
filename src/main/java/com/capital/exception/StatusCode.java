package com.capital.exception;

public enum StatusCode {
	// ========== 通用错误码 ==========
    SUCCESS("0000", "Successfully completed"),
    SYSTEM_ERROR("9999", "System error"),
    PARAM_VALIDATION_ERROR("1000", "Param validation error"),
    RESOURCE_NOT_FOUND("1001", "Resource not found"),
    OPERATION_FAILED("1002", "Operation Failed"),
    
    // ========== 用户模块 ==========
    USER_NOT_FOUND("2001", "User not found or inactive"),
    USER_ALREADY_EXISTS("2002", "User already exists"),
    USERNAME_EXISTS("2003", "User name already exists"),
    EMAIL_EXISTS("2004", "Email already exists"),
    USER_INACTIVE("2005", "User is inactive"),
    
    // ========== 账户模块 ==========
    ACCOUNT_NOT_FOUND("3001", "Account not found or inactive"),
    ACCOUNT_INACTIVE("3002", "Account is inactive"),
    INSUFFICIENT_BALANCE("3003", "Account insufficient balance"),
    ACCOUNT_ALREADY_EXISTS("3004", "Account already exists"),
    INVALID_AMOUNT("3005", "Invaid amount"),
    CURRENCY_NOT_SUPPORTED("3006", "Currency is not supported"),
    
    // ========== 商家模块 ==========
    MERCHANT_NOT_FOUND("4001", "Merchant not found"),
    MERCHANT_CODE_ALREADY_EXISTS("4002", "Merchant code already exists"),
    MERCHANT_NAME_ALREADY_EXISTS("4003", "Merchant name already exists"),
    MERCHANT_INACTIVE("4004", "Merchant is inactive"),
    
    // ========== 商品模块 ==========
    PRODUCT_NOT_FOUND("5001", "Product not found"),
    PRODUCT_SKU_EXISTS("5002", "Product sku already exists"),
    INSUFFICIENT_STOCK("5003", "Insufficient stock"),
    PRODUCT_INACTIVE("5004", "Product is inactive"),
    FAILED_TO_REDUCE_STOCK("5004", "Failed to reduce stock"),
    
    // ========== 订单模块 ==========
    ORDER_NOT_FOUND("6001", "订单不存在"),
    ORDER_CREATION_FAILED("6002", "订单创建失败"),
    ORDER_UPDATE_FAILED("6003", "订单更新失败"),
    ORDER_CANCELLED("6004", "订单已取消"),
    ORDER_COMPLETED("6005", "订单已完成"),
    ORDER_STATUS_INVALID("6006", "订单状态无效"),
    
    // ========== 交易模块 ==========
    TRANSACTION_FAILED("7001", "交易失败"),
    PAYMENT_FAILED("7002", "支付失败"),
    INVALID_TRANSACTION("7003", "无效的交易"),
    TRANSACTION_ROLLBACK("7004", "交易回滚");
    
    private final String code;
    private final String message;
    
    StatusCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

	public String getCode() {
		return this.code;
	}

	public String getMessage() {
		return this.message;
	}
	
	public static StatusCode convert(String code, String message) {
		for (StatusCode value : values()) {
			if (value.getCode().equals(code) && value.getMessage().equals(message)) {
				return value;
			}
		}
		return null;
	}
    
    /**
     * 创建异常
     */
    public TradingException toException() {
        return new TradingException(this.code, this.message);
    }
    
    /**
     * 创建带参数的异常
     */
    public TradingException toException(Object... args) {
        return new TradingException(this.code, this.message, args);
    }
    
    /**
     * 创建带原因和参数的异常
     */
    public TradingException toException(Throwable cause, Object... args) {
        return new TradingException(this.code, this.message, args, cause);
    }
}
