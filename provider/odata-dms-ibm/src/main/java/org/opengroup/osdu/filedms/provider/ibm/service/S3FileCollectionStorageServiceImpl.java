/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.odatadms.provider.ibm.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsFactory;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.ibm.objectstorage.CloudObjectStorageFactory;
import org.opengroup.osdu.odatadms.model.FileDeliveryItem;
import org.opengroup.osdu.odatadms.provider.ibm.model.ExpirationDateHelper;
import org.opengroup.osdu.odatadms.provider.ibm.model.FileCollectionDeliveryItemIBMImpl;
import org.opengroup.osdu.odatadms.provider.ibm.model.S3Location;
import org.opengroup.osdu.odatadms.provider.ibm.model.TemporaryCredentials;
import org.opengroup.osdu.odatadms.provider.interfaces.IFileCollectionStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import com.ibm.cloud.objectstorage.services.s3.AmazonS3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@RequestScope
public class S3FileCollectionStorageServiceImpl implements IFileCollectionStorageService {

	
	@Value("${ibm.cos.signed-url.expiration-days:1}")
	private int s3SignedUrlExpirationTimeInDays;
	
	@Value("${ibm.cos.endpoint_url}")
	private String endpointurl;
	
	@Value("${ibm.cos.region:us-south}")
	private String region;
	
	@Value("${ibm.cos.subuser}")
	private String subuser;
	
	@Value("${ibm.cos.subpassword}")
	private String subpassword;
	
	@Inject
	private CloudObjectStorageFactory cosFactory;
	
	@Inject
	private IEntitlementsFactory factory;
	
	@Inject
	private DpsHeaders headers;
	
	@Inject
	private ExpirationDateHelper expirationDateHelper;
	
	@Inject
	private STSHelper stsHelper;
	
	 private String roleArn;
	
	private AmazonS3 s3Client;
	
    private InstantHelper instantHelper;
    private Duration expirationDuration;
    
    @Value("${PROVIDER_KEY}")
    private String providerKey;
	
	private final static String AWS_SDK_EXCEPTION_MSG = "There was an error communicating with the Amazon S3 SDK request for S3 URL signing.";
	private final static String URI_EXCEPTION_REASON = "Exception creating signed url";
	private final static String INERNAL_SERVER_ERROR = "Internal Server Error Exception";
	private final static String INVALID_S3_PATH_REASON = "Unsigned url invalid, needs to be full S3 path";
	private final static String UNSUPPORTED_EXCEPTION_REASON = "Unsupported operation exception";
	
	@PostConstruct
	public void init() {
		s3Client = cosFactory.getClient();
		roleArn="arn:123:456:789:1234";
		expirationDateHelper = new ExpirationDateHelper();
		instantHelper = new InstantHelper();
		expirationDuration = Duration.ofMinutes(60);
	}
	
	@Override
	public FileDeliveryItem createDeliveryItem(String unsignedUrl, String indexFilePath) {
		S3Location fileCollectionLocation = new S3Location(unsignedUrl);
		
		if (!fileCollectionLocation.isValid()){
		      throw new AppException(HttpStatus.SC_BAD_REQUEST, "Malformed URL", INVALID_S3_PATH_REASON);
		    }
		
		Instant currentTime = Instant.now();
		Date expiration = expirationDateHelper.getExpiration(currentTime, expirationDuration);
		
		TemporaryCredentials credentials = stsHelper.getRetrievalCredentials(fileCollectionLocation, roleArn, this.headers.getUserEmail(), expiration);
		
		FileCollectionDeliveryItemIBMImpl fileCollectionDeliveryItem=new FileCollectionDeliveryItemIBMImpl();
		 	fileCollectionDeliveryItem.setUnsignedUrl(unsignedUrl);    
		    fileCollectionDeliveryItem.setIndexFilePath(indexFilePath);
		    fileCollectionDeliveryItem.setConnectionString(credentials.toConnectionString());
		    fileCollectionDeliveryItem.setCredentials(credentials);
		    fileCollectionDeliveryItem.setCreatedAt(currentTime);
		    fileCollectionDeliveryItem.setRegion(region);
		
		return fileCollectionDeliveryItem;	
	}

	@Override
	public String getProviderKey() {
		return providerKey;
	}
	
	
	
	
	
}
