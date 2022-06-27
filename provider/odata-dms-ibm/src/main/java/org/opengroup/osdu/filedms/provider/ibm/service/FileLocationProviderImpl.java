/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.odatadms.provider.ibm.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.odatadms.model.FileUploadLocation;
import org.opengroup.osdu.odatadms.provider.ibm.model.ExpirationDateHelper;
import org.opengroup.osdu.odatadms.provider.ibm.model.FileUploadLocationIBMImpl;
import org.opengroup.osdu.odatadms.provider.ibm.model.S3Location;
import org.opengroup.osdu.odatadms.provider.ibm.model.TemporaryCredentials;
import org.opengroup.osdu.odatadms.provider.interfaces.IFileLocationProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequestScope
public class FileLocationProviderImpl implements IFileLocationProvider {

	private static final String s3UploadLocationUriFormat = "s3://%s/%s/%s/";
	private static final SecureRandom random = new SecureRandom();
    private static final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    
    @Inject
    private DpsHeaders headers;
    
    @Inject
    private InstantHelper instantHelper;
    
    @Inject
    private ExpirationDateHelper expirationDateHelper;
    
    @Inject
    private STSHelper stsHelper;
    
    private Duration expirationDuration;
    
    private String roleArn;
    
    @Value("${PROVIDER_KEY}")
    private String providerKey;
    
    @Value("${ibm.cos.region:us-south}")
	private String region;
    
    @PostConstruct
    public void init() {
    	roleArn="arn:123:456:789:1234";
    	expirationDateHelper = new ExpirationDateHelper();
		instantHelper = new InstantHelper();
		expirationDuration = Duration.ofMinutes(60);
    }
	
	/**
     * Generates a unique subpath in the specified data-partition to allow unique secure upload locations to be provided
     */
   
	@Override
	public FileUploadLocation getUploadLocation() {
		
		// s3://{bucket-name}/{data-partition}/{key-path}/
		String unsignedUrl = String.format(s3UploadLocationUriFormat, "upload-bucket", headers.getPartitionId(), generateUniqueKey());
		S3Location s3Location = new S3Location(unsignedUrl);
		Instant now = instantHelper.getCurrentInstant();
		Date expiration = expirationDateHelper.getExpiration(now, expirationDuration);
		TemporaryCredentials credentials = stsHelper.getUploadCredentials(s3Location, roleArn, this.headers.getUserEmail(), expiration);
		FileUploadLocation uploadLocation = new FileUploadLocationIBMImpl(unsignedUrl,Instant.now(),credentials,region);
		
		return uploadLocation;
	}

	@Override
	public String getProviderKey() {
		return providerKey;
	}
	
	private static String generateUniqueKey() {
        byte[] buffer = new byte[20];
        random.nextBytes(buffer);
        return encoder.encodeToString(buffer);
    }

}
