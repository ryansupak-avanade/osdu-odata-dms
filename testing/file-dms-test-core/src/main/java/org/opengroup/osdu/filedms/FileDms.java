// Copyright 2017-2019, Schlumberger
// Copyright © 2021 Amazon Web Services
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.odatadms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;
import org.opengroup.osdu.core.common.model.entitlements.Acl;
import org.opengroup.osdu.core.common.model.legal.Legal;
import org.opengroup.osdu.core.common.model.legal.LegalCompliance;
import org.opengroup.osdu.core.common.model.storage.Record;
import org.opengroup.osdu.odatadms.model.IntTestFileDeliveryItem;
import org.opengroup.osdu.odatadms.model.request.IntTestGetDatasetRegistryRequest;
import org.opengroup.osdu.odatadms.model.response.IntTestDatasetRetrievalDeliveryItem;
import org.opengroup.osdu.odatadms.model.response.IntTestGetDatasetRetrievalInstructionsResponse;
import org.opengroup.osdu.odatadms.model.response.IntTestGetDatasetStorageInstructionsResponse;
import org.opengroup.osdu.odatadms.model.shared.TestGetCreateUpdateDatasetRegistryRequest;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public abstract class FileDms extends TestBase {

	protected ObjectMapper jsonMapper = new ObjectMapper();

	String recordId;

	protected static CloudStorageUtil cloudStorageUtil;

	protected static ArrayList<String> uploadedCloudFileUnsignedUrls = new ArrayList<>();
	protected static ArrayList<String> registeredDatasetRegistryIds = new ArrayList<>();

	public static void classSetup(String token) throws Exception {
		// make sure schema is created
		// String datasetRegistrySchema = "{\n" +
		// 		"    \"kind\": \"opendes:osdu:dataset-registry:0.0.1\",\n" +
		// 		"    \"schema\": [{\"path\":\"ResourceTypeID\",\"kind\":\"string\",\"ext\":{}},\n" +
		// 		"        {\"path\":\"ResourceID\",\"kind\":\"string\",\"ext\":{}},\n" +
		// 		"        {\"path\":\"ResourceSecurityClassification\",\"kind\":\"string\",\"ext\":{}},\n" +
		// 		"        {\"path\":\"ResourceName\",\"kind\":\"string\",\"ext\":{}},\n" +
		// 		"        {\"path\":\"ResourceDescription\",\"kind\":\"string\",\"ext\":{}},\n" +
		// 		"        {\"path\":\"ResourceSource\",\"kind\":\"string\",\"ext\":{}}]\n" +
		// 		"}";
		// ClientResponse response = TestUtils.send(TestUtils.storageBaseUrl, "schemas", "POST",
		// 		HeaderUtils.getHeaders(TenantUtils.getTenantName(), token),
		// 		datasetRegistrySchema, "");

		// Assert.assertTrue(response.getStatus() == 201 || response.getStatus() == 409);

		// make sure legaltag is created
		String legalBody = "{\t\n" +
				"\t\"name\": \"public-usa-dataset-1\",\t\n" +
				"\t\"properties\": {\t\t\n" +
				"\t\t\"countryOfOrigin\":[\"US\"],        \n" +
				"\t\t\"contractId\":\"A1234\",\n" +
				"\t\t\"expirationDate\":2222222222222,        \n" +
				"\t\t\"originator\":\"Default\",        \n" +
				"\t\t\"dataType\":\"Public Domain Data\",        \n" +
				"\t\t\"securityClassification\":\"Public\",        \n" +
				"\t\t\"personalData\":\"No Personal Data\",        \n" +
				"\t\t\"exportClassification\":\"EAR99\"\t\n" +
				"\t\t},\t\n" +
				"\t\"description\": \"A default legal tag\"\n" +
				"}\n" +
				"\n";

		ClientResponse legalResponse = TestUtils.send(TestUtils.legalBaseUrl, "legaltags", "POST",
				HeaderUtils.getHeaders(TenantUtils.getTenantName(), token),
				legalBody, "");

		Assert.assertTrue(legalResponse.getStatus() == 201 || legalResponse.getStatus() == 409);

		// // make sure acl groups are created
		// String viewerBody = "{\n" +
		// 		"    \"name\": \"data.default.viewers\",\n" +
		// 		"    \"description\": \"Meant for testing\"\n" +
		// 		"}";
		// String ownerBody = "{\n" +
		// 		"    \"name\": \"data.default.owners\",\n" +
		// 		"    \"description\": \"Meant for testing\"\n" +
		// 		"}";

		// ClientResponse viewersResponse = TestUtils.send(TestUtils.entitlementsBaseUrl, "groups", "POST",
		// 		HeaderUtils.getHeaders(TenantUtils.getTenantName(), token),
		// 		viewerBody, "");

		// String r = response.getEntity(String.class);

		// Assert.assertTrue(viewersResponse.getStatus() == 200);

		// ClientResponse ownersResponse = TestUtils.send(TestUtils.entitlementsBaseUrl, "groups", "POST",
		// 		HeaderUtils.getHeaders(TenantUtils.getTenantName(), token),
		// 		ownerBody, "");

		// Assert.assertTrue(ownersResponse.getStatus() == 200);
	}

	public static void classTearDown(String token) throws Exception {
		
		for(String unsignedUrl : uploadedCloudFileUnsignedUrls) {
			cloudStorageUtil.deleteCloudFile(unsignedUrl);
		}

		for(String datasetRegistryId : registeredDatasetRegistryIds) {

			System.out.println(String.format("Deleting Dataset Registry: %s", datasetRegistryId));
			
			ClientResponse storageResponse = TestUtils.send(TestUtils.storageBaseUrl, 
			String.format("records/%s", datasetRegistryId), 
			"DELETE",
			HeaderUtils.getHeaders(TenantUtils.getTenantName(), token),
			"", "");

			System.out.println(String.format("Deleting Dataset Registry Response Code: %s", storageResponse.getStatus()));
			
		}

		
	}

	@Test
	public void should_getUploadLocation() throws Exception {
		ClientResponse response = TestUtils.send("file/getStorageInstructions", "GET",
				HeaderUtils.getHeaders(TenantUtils.getTenantName(), testUtils.getToken()), "", "");
		Assert.assertEquals(200, response.getStatus());
		
		// JsonObject json = new JsonParser().parse(response.getEntity(String.class)).getAsJsonObject();

		String respStr = response.getEntity(String.class);		

		IntTestGetDatasetStorageInstructionsResponse<Object> resp = jsonMapper.readValue(respStr, IntTestGetDatasetStorageInstructionsResponse.class);

		Assert.assertEquals(TestUtils.getProviderKey(), resp.getProviderKey());

		Assert.assertNotNull(resp.getStorageLocation());
		validate_storageLocation(resp.getStorageLocation());
	}

	@Test
	public void upload_file_register_it_and_retrieve_it() throws Exception {
		
		//Step 1: Get Storage Instructions for File
		ClientResponse getStorageInstClientResp = TestUtils.send("file/getStorageInstructions", "GET",
				HeaderUtils.getHeaders(TenantUtils.getTenantName(), testUtils.getToken()), "", "");
		
		Assert.assertEquals(200, getStorageInstClientResp.getStatus());

		String getStorageRespStr = getStorageInstClientResp.getEntity(String.class);		

		IntTestGetDatasetStorageInstructionsResponse<Object> getStorageInstResponse = jsonMapper.readValue(getStorageRespStr, IntTestGetDatasetStorageInstructionsResponse.class);

		Assert.assertEquals(TestUtils.getProviderKey(), getStorageInstResponse.getProviderKey());

		//Step 2: Upload File
		String fileName = "testFile.txt";
		String fileContents = "Hello World!";
		String unsignedUploadUrl = cloudStorageUtil.uploadCloudFileUsingProvidedCredentials(fileName, getStorageInstResponse.getStorageLocation(), fileContents);
		uploadedCloudFileUnsignedUrls.add(unsignedUploadUrl);

		//Step 3: Register File
		String uuid = UUID.randomUUID().toString().replace("-", "");
		String datasetRegistryId = String.format("%s:dataset--File.Generic:%s", TenantUtils.getTenantName(), uuid);
		Record datasetRegistry = createDatasetRegistry(datasetRegistryId, fileName, unsignedUploadUrl);

		TestGetCreateUpdateDatasetRegistryRequest datasetRegistryRequest = new TestGetCreateUpdateDatasetRegistryRequest(new ArrayList<>());
		datasetRegistryRequest.getDatasetRegistries().add(datasetRegistry);

		ClientResponse datasetRegistryResponse = TestUtils.send(TestUtils.datasetBaseUrl, "registerDataset", "PUT",
				HeaderUtils.getHeaders(TenantUtils.getTenantName(), testUtils.getToken()),
				jsonMapper.writeValueAsString(datasetRegistryRequest), "");

		Assert.assertTrue(datasetRegistryResponse.getStatus() == 201);

		registeredDatasetRegistryIds.add(datasetRegistryId);


		//Step 4: Retrieve File and validate contents
		IntTestGetDatasetRegistryRequest getDatasetRequest = new IntTestGetDatasetRegistryRequest(new ArrayList<>());
		getDatasetRequest.getDatasetRegistryIds().add(datasetRegistryId);

		ClientResponse retrievalClientResponse = TestUtils.send("file/getRetrievalInstructions", "POST",
				HeaderUtils.getHeaders(TenantUtils.getTenantName(), testUtils.getToken()), 
				jsonMapper.writeValueAsString(getDatasetRequest),
				 "");
		
		Assert.assertEquals(200, retrievalClientResponse.getStatus());

		String getRetrievalRespStr = retrievalClientResponse.getEntity(String.class);	

		IntTestGetDatasetRetrievalInstructionsResponse getRetrievalInstResponse = jsonMapper.readValue(getRetrievalRespStr, IntTestGetDatasetRetrievalInstructionsResponse.class);
		
		IntTestDatasetRetrievalDeliveryItem datasetRetrievalItem = getRetrievalInstResponse.getDelivery().get(0);

		validate_dataset_retrieval_delivery_item(datasetRetrievalItem);
		
		String downloadedContent = cloudStorageUtil.downloadCloudFileUsingDeliveryItem(datasetRetrievalItem.getRetrievalProperties());		

		Assert.assertEquals(fileContents, downloadedContent);
		
		
	}

	public abstract void validate_dataset_retrieval_delivery_item(IntTestDatasetRetrievalDeliveryItem deliveryItem);

	public abstract void validate_storageLocation(Object storageLocation);

	private static Record createDatasetRegistry(String id, String filename, String unsignedUrl) {


		TestGetCreateUpdateDatasetRegistryRequest request = new TestGetCreateUpdateDatasetRegistryRequest();

		Record datasetRegistry = new Record();

		datasetRegistry.setId(id);
		
		datasetRegistry.setKind(String.format("%s:wks:dataset--File.Generic:1.0.0",TestUtils.getSchemaAuthority()));	
		
		//set legal
		Legal legal = new Legal();
		HashSet<String> legalTags = new HashSet<>();
		legalTags.add(String.format("%s-public-usa-dataset-1", TenantUtils.getTenantName()));
		legal.setLegaltags(legalTags);
		HashSet<String> otherRelevantDataCountries = new HashSet<>();
		otherRelevantDataCountries.add("US");
		legal.setOtherRelevantDataCountries(otherRelevantDataCountries);
		legal.setStatus(LegalCompliance.compliant);
		datasetRegistry.setLegal(legal);

		//set acl
		Acl acl = new Acl();
		String[] viewers = new String[] { String.format("data.default.viewers@%s.example.com", TenantUtils.getTenantName()) };
		acl.setViewers(viewers);
		String[] owners = new String[] { String.format("data.default.owners@%s.example.com", TenantUtils.getTenantName()) };
		acl.setOwners(owners);
		datasetRegistry.setAcl(acl);

		HashMap<String, Object> data = new HashMap<>();
		data.put("ResourceID", id);
		data.put("ResourceTypeID", "srn:type:file/txt:");
		data.put("ResourceSecurityClassification", "srn:reference-data/ResourceSecurityClassification:RESTRICTED:");
		data.put("ResourceSource", "FileDMS Int Test");
		data.put("ResourceName", filename);
		data.put("ResourceDescription", "Test File");
		HashMap<String, Object> datasetProperties = new HashMap<>();
		HashMap<String, Object> fileSourceInfo = new HashMap<>();
		fileSourceInfo.put("FileSource", "");
		fileSourceInfo.put("PreloadFilePath", unsignedUrl);
		datasetProperties.put("FileSourceInfo", fileSourceInfo);
		data.put("DatasetProperties", datasetProperties);
		datasetRegistry.setData(data);
		
		return datasetRegistry;
	}
}