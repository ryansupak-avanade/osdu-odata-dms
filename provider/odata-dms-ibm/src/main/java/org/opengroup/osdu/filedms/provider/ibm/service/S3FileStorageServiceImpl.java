/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.odatadms.provider.ibm.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
import org.opengroup.osdu.odatadms.provider.ibm.model.FileDeliveryItemIBMImpl;
import org.opengroup.osdu.odatadms.provider.ibm.model.S3Location;
import org.opengroup.osdu.odatadms.provider.ibm.model.TemporaryCredentials;
import org.opengroup.osdu.odatadms.provider.interfaces.IFileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.ibm.cloud.objectstorage.services.s3.AmazonS3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
@RequestScope
public class S3FileStorageServiceImpl implements IFileStorageService {

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
	
	@Inject
	private S3Helper s3Helper;

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
		roleArn = "arn:123:456:789:1234";
		expirationDateHelper = new ExpirationDateHelper();
		instantHelper = new InstantHelper();
		expirationDuration = Duration.ofMinutes(60);
	}

	@Override
	public FileDeliveryItem createDeliveryItem(String unsignedUrl, String fileName) {
		S3Location fileLocation = new S3Location(unsignedUrl);
		
		 URL s3SignedUrl;

		if (!fileLocation.isValid()) {
			throw new AppException(HttpStatus.SC_BAD_REQUEST, "Malformed URL", INVALID_S3_PATH_REASON);
		}
		if (fileLocation.getKey().trim().endsWith("/")) {
			throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Invalid S3 Object Key",
					"Invalid S3 Object Key - Object key cannot contain trailing '/'");
		}

		Instant currentTime = Instant.now();
		Date expiration = expirationDateHelper.getExpiration(currentTime, expirationDuration);

		TemporaryCredentials credentials = stsHelper.getRetrievalCredentials(fileLocation, roleArn,
				this.headers.getUserEmail(), expiration);

		if (!s3Helper.doesObjectExist(fileLocation, credentials)) {
			throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Invalid File Path",
					"Invalid File Path - File not found at specified S3 path");
		}

		try {
			s3SignedUrl = s3Helper.generatePresignedUrl(fileLocation, HttpMethod.GET, expiration, credentials);
		} catch (SdkClientException e) {
			throw new AppException(HttpStatus.SC_SERVICE_UNAVAILABLE, "S3 Error", URI_EXCEPTION_REASON, e);
		}

		URI signedUrl = null;

		try {
			signedUrl = new URI(s3SignedUrl.toString());

		} catch (URISyntaxException e) {
			log.error("There was an error generating the URI.", e);
			throw new AppException(HttpStatus.SC_BAD_REQUEST, "Malformed S3 URL", URI_EXCEPTION_REASON, e);
		}
		
		FileDeliveryItemIBMImpl fileDeliveryItem = new FileDeliveryItemIBMImpl();
		fileDeliveryItem.setFileName(fileName);
	    fileDeliveryItem.setUnsignedUrl(unsignedUrl);
	    fileDeliveryItem.setSignedUrl(signedUrl);
	    fileDeliveryItem.setSignedUrlExpiration(expiration);
	    fileDeliveryItem.setConnectionString(credentials.toConnectionString());
	    fileDeliveryItem.setCredentials(credentials);
	    fileDeliveryItem.setCreatedAt(currentTime);
	    fileDeliveryItem.setRegion(region);

		return fileDeliveryItem;
	}

	@Override
	public String getProviderKey() {
		return providerKey;
	}

}
