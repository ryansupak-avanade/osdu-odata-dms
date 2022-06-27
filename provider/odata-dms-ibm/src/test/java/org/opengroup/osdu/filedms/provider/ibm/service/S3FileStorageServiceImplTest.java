package org.opengroup.osdu.odatadms.provider.ibm.service;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.ibm.objectstorage.CloudObjectStorageFactory;
import org.opengroup.osdu.odatadms.provider.ibm.model.S3Location;
import org.opengroup.osdu.odatadms.model.FileDeliveryItem;
import org.opengroup.osdu.odatadms.provider.ibm.model.FileDeliveryItemIBMImpl;
import org.opengroup.osdu.odatadms.provider.ibm.model.ExpirationDateHelper;
import org.opengroup.osdu.odatadms.provider.ibm.model.TemporaryCredentials;

import com.amazonaws.HttpMethod;

@RunWith(MockitoJUnitRunner.class)
public class S3FileStorageServiceImplTest {
	
	@Mock
	private CloudObjectStorageFactory cosFactory;
	
	@Mock
	private DpsHeaders headers;
	
	@Mock
	private ExpirationDateHelper expirationDateHelper;
	
	@Mock
	private STSHelper stsHelper;
	
	@Mock
	private S3Helper s3Helper;
	
	@Mock
	private InstantHelper instantHelper;
	
	@Mock
	private TemporaryCredentials tempCredentials; 
	
	@InjectMocks
	S3FileStorageServiceImpl s3storage;

	@Before
	public void setUp() throws Exception {
		
		Date expirationDate = new Date();
		Mockito.when(expirationDateHelper.getExpiration(Mockito.any(Instant.class),Mockito.any(Duration.class))).thenReturn(expirationDate); 
		Mockito.when(headers.getUserEmail()).thenReturn("user@some-email.com");
		
		Mockito.when(s3Helper.doesObjectExist(Mockito.any(S3Location.class), Mockito.any(TemporaryCredentials.class))).thenReturn(true);
		
		Mockito.when(s3Helper.generatePresignedUrl(
	            Mockito.any(S3Location.class), 
	            Mockito.any(HttpMethod.class), 
	            Mockito.any(Date.class), 
	            Mockito.any(TemporaryCredentials.class)))
	            .thenReturn(new URL("https://some-presigned-url/test.txt"));
		
		s3storage.init();
	}

	@Test
	public void testCreateDeliveryItem() {
		String unsignedUrl = "s3://test-bucket/some-path/test.txt"; 
		String roleArn="some-arn";
		Duration expirationDuration=Duration.ofMinutes(60);
		
		tempCredentials.setAccessKeyId("DUMMY_ACCESS_KEY");
		tempCredentials.setSecretAccessKey("DUMMY_SECRET");
		tempCredentials.setSessionToken("DUMMY_SESSION");
		tempCredentials.setExpiration(expirationDateHelper.getExpiration(new Date().toInstant(), expirationDuration));
		
		 Mockito.when(stsHelper.getRetrievalCredentials(Mockito.any(S3Location.class), Mockito.anyString(), Mockito.anyString(), Mockito.any(Date.class)))
         .thenReturn(tempCredentials);
		 
		 FileDeliveryItem deliveryItem = s3storage.createDeliveryItem(unsignedUrl, "test.txt");
		 
		  FileDeliveryItemIBMImpl ibmDeliveryItem = (FileDeliveryItemIBMImpl)deliveryItem;
		  
		  Assert.assertEquals(ibmDeliveryItem.getCredentials(), tempCredentials);
	      Assert.assertEquals(ibmDeliveryItem.getUnsignedUrl(), unsignedUrl);  
		
	}

}
