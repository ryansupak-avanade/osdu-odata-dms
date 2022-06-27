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

package org.opengroup.osdu.odatadms.service.interfaces;

import org.opengroup.osdu.odatadms.model.ODataUploadLocation;
import org.opengroup.osdu.odatadms.model.request.GetDatasetRegistryRequest;
import org.opengroup.osdu.odatadms.model.response.GetDatasetRetrievalInstructionsResponse;
import org.opengroup.osdu.odatadms.model.response.GetDatasetStorageInstructionsResponse;

public interface IODataDmsService extends IDmsService<ODataUploadLocation> {
    GetDatasetStorageInstructionsResponse<ODataUploadLocation> getStorageInstructions();
    GetDatasetRetrievalInstructionsResponse getRetrievalInstructions(GetDatasetRegistryRequest getDatasetRegistryRequest);
}
