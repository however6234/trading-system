package com.capital;

public enum AccountType {
	MERCHANT("Merchant"), User("User");

	private String value;

	AccountType(String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return String.valueOf(value);
	}
}
