package com.capital.util;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LocalIdIdentifierGenerator implements IdentifierGenerator {

	@Autowired
	private LocalIdGenerator localIdGenerator;

	@Override
	public Serializable generate(SharedSessionContractImplementor session, Object object) throws HibernateException {

		return localIdGenerator.nextId();
	}

	@Override
	public boolean supportsJdbcBatchInserts() {
		return true;
	}
}
