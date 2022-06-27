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

package org.opengroup.osdu.odatadms.service;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.opengroup.osdu.core.common.model.storage.MultiRecordInfo;
import org.opengroup.osdu.core.common.model.storage.Record;
import org.opengroup.osdu.core.common.model.storage.StorageException;
import org.opengroup.osdu.core.common.storage.IStorageFactory;
import org.opengroup.osdu.core.common.storage.IStorageService;
import org.opengroup.osdu.odatadms.model.ODataDeliveryItem;
import org.opengroup.osdu.odatadms.model.ODataUploadLocation;
import org.opengroup.osdu.odatadms.model.request.GetDatasetRegistryRequest;
import org.opengroup.osdu.odatadms.model.response.DatasetRetrievalDeliveryItem;
import org.opengroup.osdu.odatadms.model.response.GetDatasetRetrievalInstructionsResponse;
import org.opengroup.osdu.odatadms.model.response.GetDatasetStorageInstructionsResponse;
import org.opengroup.osdu.odatadms.provider.interfaces.IODataLocationProvider;

@RunWith(MockitoJUnitRunner.class)
public class ODataDmsServiceImplTest {

    @Mock
    private IODataLocationProvider oDataLocationProvider;

    @Mock
    private org.opengroup.osdu.odatadms.provider.interfaces.IODataStorageService oDataStorageService;

    @Mock
    private IStorageService storageService;

    @Mock
    private IStorageFactory storageFactory;

    @InjectMocks
    ODataDmsServiceImpl sut;

    @Before
    public void setup() {

        Mockito.when(storageFactory.create(Mockito.any())).thenReturn(storageService);

    }

    @Test
    public void TestGetStorageInstructions() {

        ODataUploadLocation testODataUploadLocation = new ODataUploadLocation() {
            public String locationData = "test";
        };

        String providerKey = "DUMMY_KEY";

        Mockito.when(oDataLocationProvider.getUploadLocation()).thenReturn(testODataUploadLocation);
        Mockito.when(oDataLocationProvider.getProviderKey()).thenReturn(providerKey);

        GetDatasetStorageInstructionsResponse<ODataUploadLocation> ful = sut.getStorageInstructions();

        Assert.assertEquals(testODataUploadLocation, ful.getStorageLocation());
        Assert.assertEquals(providerKey, ful.getProviderKey());
    }

    @Test
    public void TestGetRetrievalInstructions() throws StorageException {

        String providerKey = "DUMMY_KEY";

        String datasetRegistryId = "test-id";
        GetDatasetRegistryRequest request = new GetDatasetRegistryRequest();
        request.datasetRegistryIds = new ArrayList<>();
        request.datasetRegistryIds.add(datasetRegistryId);        

        ODataDeliveryItem mockDeliveryItem = new ODataDeliveryItem(){};

        Record datasetRegistryRecord = new Record();
        datasetRegistryRecord.setId(datasetRegistryId);
        HashMap<String, Object> datasetProperties = new HashMap<>();
        HashMap<String, Object> fileSourceInfo = new HashMap<>();
        fileSourceInfo.put("PreLoadFilePath", "s3://test-path/some-file.txt");        
        datasetProperties.put("FileSourceInfo", fileSourceInfo);
        HashMap<String, Object> data = new HashMap<>();
        data.put("DatasetProperties", datasetProperties);
        data.put("ResourceName", "some-file.txt");
        datasetRegistryRecord.setData(data);

        ArrayList<Record> datasetRegistryRecords = new ArrayList<>();
        datasetRegistryRecords.add(datasetRegistryRecord);
        MultiRecordInfo getRecordsResponse = new MultiRecordInfo();
        getRecordsResponse.setRecords(datasetRegistryRecords);

        Mockito.when(oDataStorageService.createDeliveryItem(Mockito.anyString(), Mockito.anyString())).thenReturn(mockDeliveryItem);
        Mockito.when(oDataStorageService.getProviderKey()).thenReturn(providerKey);
        Mockito.when(storageService.getRecords(Mockito.anyCollection())).thenReturn(getRecordsResponse);

        GetDatasetRetrievalInstructionsResponse response = sut.getRetrievalInstructions(request);

        Assert.assertEquals(response.getDelivery().size(), 1); //Should get back 1 delivery item
        DatasetRetrievalDeliveryItem deliveryItem = response.getDelivery().get(0);
        Assert.assertEquals(datasetRegistryId, deliveryItem.getDatasetRegistryId());
        Assert.assertEquals(mockDeliveryItem, deliveryItem.getRetrievalProperties());
        Assert.assertEquals(providerKey, deliveryItem.getProviderKey());


    }
    
}
