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

package org.opengroup.osdu.odatadms.api;


import org.opengroup.osdu.odatadms.model.DeliveryRole;
import org.opengroup.osdu.odatadms.model.ODataUploadLocation;
import org.opengroup.osdu.odatadms.model.request.GetDatasetRegistryRequest;
import org.opengroup.osdu.odatadms.model.response.GetDatasetRetrievalInstructionsResponse;
import org.opengroup.osdu.odatadms.model.response.GetDatasetStorageInstructionsResponse;
import org.opengroup.osdu.odatadms.service.interfaces.IODataDmsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.RequestScope;

import javax.inject.Inject;


@RestController
@RequestScope
@RequestMapping("/odata")
@Validated
public class ODataDmsApi {

    @Inject
    private IODataDmsService oDataDmsService;

    @GetMapping("/getStorageInstructions")
    @PreAuthorize("@authorizationFilter.hasRole('" + DeliveryRole.VIEWER + "')")
    public ResponseEntity<GetDatasetStorageInstructionsResponse<ODataUploadLocation>> GetFileUploadLocation() throws Exception {
        return new ResponseEntity<>(oDataDmsService.getStorageInstructions(), HttpStatus.OK);
    }

    @PostMapping("/getRetrievalInstructions")
    @PreAuthorize("@authorizationFilter.hasRole('" + DeliveryRole.VIEWER + "')")
    public ResponseEntity<GetDatasetRetrievalInstructionsResponse> GetFileDeliveryInstructions(@RequestBody GetDatasetRegistryRequest getDatasetRegistryRequest) throws Exception {
        return new ResponseEntity<>(oDataDmsService.getRetrievalInstructions(getDatasetRegistryRequest) ,HttpStatus.OK);
    }

}
