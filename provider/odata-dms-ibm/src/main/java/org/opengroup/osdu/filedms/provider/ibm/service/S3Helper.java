/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.odatadms.provider.ibm.service;

import java.net.URL;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.opengroup.osdu.odatadms.provider.ibm.model.S3Location;
import org.opengroup.osdu.odatadms.provider.ibm.model.TemporaryCredentials;
import org.opengroup.osdu.odatadms.provider.ibm.model.TemporaryCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
//import org.opengroup.osdu.filedms.provider.ibm.model.TemporaryCredentialsProvider;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;

@Component
public class S3Helper {

	@Value("${ibm.cos.region:us-south}")
	private String region;
  


  @PostConstruct
  public void init() {
   
  }

  /**
   * Generates a presignedurl for the S3location
   * @param location
   * @param httpMethod
   * @param expiration
   * @return
   * @throws SdkClientException
   */
  public URL generatePresignedUrl(S3Location location, HttpMethod httpMethod, Date expiration, TemporaryCredentials credentials) throws SdkClientException {

    AmazonS3 s3 = generateS3ClientWithCredentials(credentials);

    GeneratePresignedUrlRequest generatePresignedUrlRequest =
            new GeneratePresignedUrlRequest(location.getBucket(), location.getKey(), httpMethod)
                    .withExpiration(expiration);

    return s3.generatePresignedUrl(generatePresignedUrlRequest);
  }

  public boolean doesObjectExist(S3Location location, TemporaryCredentials credentials) {
    
    AmazonS3 s3 = generateS3ClientWithCredentials(credentials);

    try {
            
      return s3.doesObjectExist(location.bucket, location.key);      
      
    } catch (AmazonServiceException ase) {
      return false;
    }
  }

  private AmazonS3 generateS3ClientWithCredentials(TemporaryCredentials credentials) {
      return AmazonS3ClientBuilder
      .standard()
      .withRegion(Regions.fromName(region))
      .withCredentials(new TemporaryCredentialsProvider(credentials))
      .build();
  }
}
