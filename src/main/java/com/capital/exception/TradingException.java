package com.capital.exception;

import lombok.Getter;

@Getter
public class TradingException extends RuntimeException{
	 
    private final String code;
    private final Object[] args;
    
    public TradingException(String code, String message) {
        super(message);
        this.code = code;
        this.args = null;
    }
    
    public TradingException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.args = null;
    }
    
    public TradingException(String code, String message, Object[] args) {
        super(message);
        this.code = code;
        this.args = args;
    }
    
    public TradingException(String code, String message, Object[] args, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.args = args;
    }
    
    public String getFullErrorCode() {
        return "TRADING_" + this.code;
    }
    
    public StatusCode toStatusCode() {
        return StatusCode.convert(code, super.getMessage());
    }
}
