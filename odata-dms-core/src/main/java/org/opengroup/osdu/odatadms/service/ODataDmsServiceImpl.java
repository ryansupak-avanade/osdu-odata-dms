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
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.opengroup.osdu.core.common.http.json.HttpResponseBodyMapper;
import org.opengroup.osdu.core.common.http.json.HttpResponseBodyParsingException;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.storage.MultiRecordInfo;
import org.opengroup.osdu.core.common.model.storage.Record;
import org.opengroup.osdu.core.common.model.storage.StorageException;
import org.opengroup.osdu.core.common.storage.IStorageFactory;
import org.opengroup.osdu.core.common.storage.IStorageService;
import org.opengroup.osdu.odatadms.model.ODataDeliveryItem;
import org.opengroup.osdu.odatadms.model.ODataUploadLocation;
import org.opengroup.osdu.odatadms.model.request.GetDatasetRegistryRequest;
import org.opengroup.osdu.odatadms.model.request.StorageExceptionResponse;
import org.opengroup.osdu.odatadms.model.response.DatasetRetrievalDeliveryItem;
import org.opengroup.osdu.odatadms.model.response.GetDatasetRetrievalInstructionsResponse;
import org.opengroup.osdu.odatadms.model.response.GetDatasetStorageInstructionsResponse;
import org.opengroup.osdu.odatadms.provider.interfaces.IODataLocationProvider;
import org.opengroup.osdu.odatadms.provider.interfaces.IODataStorageService;
import org.opengroup.osdu.odatadms.service.interfaces.IODataDmsService;
import org.springframework.stereotype.Service;



@Service
public class ODataDmsServiceImpl implements IODataDmsService {

    @Inject
    private IODataLocationProvider oDataLocationProvider;

    @Inject
    private IODataStorageService oDataStorageService;

    @Inject
    private IStorageFactory storageFactory;

    @Inject
    private DpsHeaders headers;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpResponseBodyMapper bodyMapper = new HttpResponseBodyMapper(objectMapper);

    @Override
    public GetDatasetStorageInstructionsResponse<ODataUploadLocation> getStorageInstructions() {

        return new GetDatasetStorageInstructionsResponse<ODataUploadLocation>(oDataLocationProvider.getUploadLocation(),
                oDataLocationProvider.getProviderKey());
    }

    @Override
    public GetDatasetRetrievalInstructionsResponse getRetrievalInstructions(
            GetDatasetRegistryRequest getDatasetRegistryRequest) {

        IStorageService storageService = this.storageFactory.create(headers);

        MultiRecordInfo getRecordsResponse = null;

        try {
            getRecordsResponse = storageService.getRecords(getDatasetRegistryRequest.getDatasetRegistryIds());
        } catch (StorageException e) {
            try {
                StorageExceptionResponse body = bodyMapper.parseBody(e.getHttpResponse(), StorageExceptionResponse.class);
                throw new AppException(body.getCode(), "Storage Service: " + body.getReason(), body.getMessage());
            } catch (HttpResponseBodyParsingException e1) {
                throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR,
                        "Internal Server Error",
                        "Failed to parse error from Storage Service");
            }
        }
        
        List<DatasetRetrievalDeliveryItem> delivery = getDelivery(getRecordsResponse.getRecords());

        GetDatasetRetrievalInstructionsResponse response = new GetDatasetRetrievalInstructionsResponse(delivery);
        // response.setDelivery(delivery);

        return response;
    }




    private List<DatasetRetrievalDeliveryItem> getDelivery(List<Record> datasetRegistryRecords){
        List<DatasetRetrievalDeliveryItem> delivery = new ArrayList<>();
        for(Record datasetRegistryRecord : datasetRegistryRecords){
            String cloudStorageFilePath = getStorageFilePath(datasetRegistryRecord);
            
            //reject paths that are not files
            if (cloudStorageFilePath.trim().endsWith("/")) {
                throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Invalid File Path",
                "Invalid File Path - Filename cannot contain trailing '/'");
            }
            
            String fileName = getFileName(datasetRegistryRecord);
            ODataDeliveryItem oDataDeliveryItem = oDataStorageService.createDeliveryItem(cloudStorageFilePath, fileName);
            String providerKey = oDataStorageService.getProviderKey();

            DatasetRetrievalDeliveryItem resp = new DatasetRetrievalDeliveryItem(datasetRegistryRecord.getId(),oDataDeliveryItem, providerKey);

            delivery.add(resp);
        }
        return delivery;
    }

    private String getFileName(Record datasetRegistryRecord){
        String fileName;
        try {
            fileName = (String) datasetRegistryRecord.getData().get("ResourceName");
        } catch (Exception e){
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Error finding file's name on record",
                    e.getMessage(), e);
        }
        return fileName;
    }

    private String getStorageFilePath(Record datasetRegistryRecord){
        String storageFilePath;
        try {
            Map<String, Object> datasetProperties = (Map) datasetRegistryRecord.getData().get("DatasetProperties");
            Map<String, String> fileSourceInfo = (Map) datasetProperties.get("FileSourceInfo");

            if (!StringUtils.isEmpty(fileSourceInfo.get("FileSource"))) {
                storageFilePath = fileSourceInfo.get("FileSource");    
            }
            else if (!StringUtils.isEmpty(fileSourceInfo.get("PreloadFilePath"))) {
                storageFilePath = fileSourceInfo.get("PreloadFilePath");    
            }
            else if (!StringUtils.isEmpty(fileSourceInfo.get("PreLoadFilePath"))) {
                storageFilePath = fileSourceInfo.get("PreLoadFilePath");
            }                
            else  {
                throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "No valid File Path found for File dataset", "Error finding unsigned path on record for signing");
            }            
        } catch (Exception e){
            throw new AppException(HttpStatus.SC_INTERNAL_SERVER_ERROR, "Error finding unsigned path on record for signing",
                    e.getMessage(), e);
        }
        return storageFilePath;
    }
}
