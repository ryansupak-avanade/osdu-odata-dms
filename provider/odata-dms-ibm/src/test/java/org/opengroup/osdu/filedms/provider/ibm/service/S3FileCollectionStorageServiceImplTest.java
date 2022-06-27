package org.opengroup.osdu.odatadms.provider.ibm.service;

import static org.junit.Assert.*;

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
import org.opengroup.osdu.odatadms.provider.ibm.service.STSHelper;
import org.opengroup.osdu.odatadms.provider.ibm.service.S3Helper;
import org.opengroup.osdu.odatadms.provider.ibm.model.TemporaryCredentials;
import org.opengroup.osdu.odatadms.provider.ibm.model.S3Location;
import org.opengroup.osdu.odatadms.model.FileDeliveryItem;
import org.opengroup.osdu.odatadms.provider.ibm.model.ExpirationDateHelper;
import org.opengroup.osdu.odatadms.provider.ibm.model.FileCollectionDeliveryItemIBMImpl;

@RunWith(MockitoJUnitRunner.class)
public class S3FileCollectionStorageServiceImplTest {
	
	@Mock
	private DpsHeaders headers;
	
	@Mock
	private STSHelper stsHelper;
	
	@Mock
	private ExpirationDateHelper expirationDateHelper;
	
	@Mock
    private S3Helper s3Helper;
	
	@Mock
	private CloudObjectStorageFactory cosFactory;
	
	@Mock 
	private TemporaryCredentials tempCreds;
	 
	
			
	@InjectMocks
	S3FileCollectionStorageServiceImpl s3Coll;
	

	@Before
	public void setUp() throws Exception {
		Date expirationDate = new Date();
		 Mockito.when(expirationDateHelper.getExpiration(Mockito.any(Instant.class),Mockito.any(Duration.class))).thenReturn(expirationDate); 
		 
		 Mockito.when(headers.getUserEmail()).thenReturn("user@some-email.com");
		 
		 s3Coll.init();
		 
		 
	}

	@Test
	public void testCreateDeliveryItem() {
		String roleArn="some-arn";
		String unsignedUrl = "s3://test-bucket/some-path/";
        String indexFilePath = "s3://test-bucket/some-path/some.index";
        Duration expirationDuration=Duration.ofMinutes(60);
        
        //TemporaryCredentials tempCreds = new TemporaryCredentials();
        tempCreds.setAccessKeyId("DUMMY_ACCESS_KEY");
        tempCreds.setSecretAccessKey("DUMMY_SECRET");
        tempCreds.setSessionToken("DUMMY_SESSION");
        tempCreds.setExpiration(expirationDateHelper.getExpiration(new Date().toInstant(), expirationDuration));
       
        Mockito.when(stsHelper.getRetrievalCredentials(Mockito.any(S3Location.class), Mockito.anyString(), Mockito.anyString(), Mockito.any(Date.class)))
        .thenReturn(tempCreds);
        
        FileDeliveryItem deliveryItem = s3Coll.createDeliveryItem(unsignedUrl, indexFilePath);
        FileCollectionDeliveryItemIBMImpl ibmDeliveryItem =(FileCollectionDeliveryItemIBMImpl)deliveryItem;
        
        Assert.assertEquals(ibmDeliveryItem.getCredentials(), tempCreds);
        Assert.assertEquals(ibmDeliveryItem.getUnsignedUrl(), unsignedUrl);
        Assert.assertEquals(ibmDeliveryItem.getIndexFilePath(), indexFilePath);  
       
		
	}

}
