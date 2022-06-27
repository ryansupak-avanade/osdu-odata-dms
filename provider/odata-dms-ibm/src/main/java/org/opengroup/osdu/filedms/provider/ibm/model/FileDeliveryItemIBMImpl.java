/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.odatadms.provider.ibm.model;

import java.net.URI;
import java.time.Instant;
import java.util.Date;

import org.opengroup.osdu.odatadms.model.FileDeliveryItem;
import org.opengroup.osdu.odatadms.provider.ibm.model.TemporaryCredentials;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class FileDeliveryItemIBMImpl implements FileDeliveryItem {
	
	@JsonProperty("signedUrl")
    URI signedUrl;

    @JsonProperty("signedUrlExpiration")
    Date signedUrlExpiration;

    @JsonProperty("unsignedUrl")
    String unsignedUrl;

    @JsonProperty("createdAt")
    Instant createdAt;

    @JsonProperty("fileName")
    String fileName;

    @JsonProperty("connectionString")
    String connectionString;

    @JsonProperty("credentials")
    private TemporaryCredentials credentials;

    @JsonProperty("region")
    private String region;

}
