/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.odatadms.provider.ibm.model;

import java.time.Instant;

import org.opengroup.osdu.odatadms.model.FileCollectionUploadLocation;
import org.opengroup.osdu.odatadms.model.FileUploadLocation;

import lombok.Data;

@Data
public class FileUploadLocationIBMImpl implements FileUploadLocation, FileCollectionUploadLocation {
	
	private String unsignedUrl;

    private Instant createdAt;    

    private String connectionString;

    private TemporaryCredentials credentials;

    private String region;
    
    public FileUploadLocationIBMImpl() {
    	
    }
    
	public FileUploadLocationIBMImpl(String unsignedUrl, Instant createdAt, TemporaryCredentials credentials, String region) {
		super();
		this.unsignedUrl = unsignedUrl;
		this.createdAt = createdAt;
		this.connectionString = credentials.toConnectionString();
		this.credentials = credentials;
		this.region = region;
	}
    
    
    

}
