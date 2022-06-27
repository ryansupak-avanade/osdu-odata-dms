/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.odatadms.provider.ibm.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.opengroup.osdu.core.common.entitlements.EntitlementsAPIConfig;
import org.opengroup.osdu.core.common.entitlements.EntitlementsFactory;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsFactory;
import org.opengroup.osdu.core.common.http.json.HttpResponseBodyMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.stereotype.Component;

@Component
public class EntitlementsClientFactory extends AbstractFactoryBean<IEntitlementsFactory> {

	@Value("${AUTHORIZE_API}")
	private String AUTHORIZE_API;

	@Value("${AUTHORIZE_API_KEY:}")
	private String AUTHORIZE_API_KEY;

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final HttpResponseBodyMapper bodyMapper = new HttpResponseBodyMapper(objectMapper);

	@Override
	protected IEntitlementsFactory createInstance() throws Exception {

		return new EntitlementsFactory(EntitlementsAPIConfig
				.builder()
				.rootUrl(AUTHORIZE_API)
				.apiKey(AUTHORIZE_API_KEY)
				.build(),
				bodyMapper);
	}

	@Override
	public Class<?> getObjectType() {
		return IEntitlementsFactory.class;
	}
}