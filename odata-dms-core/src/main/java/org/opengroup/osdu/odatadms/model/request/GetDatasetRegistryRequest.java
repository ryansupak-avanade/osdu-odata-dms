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

//TODO: move from dataset service to core-common and remove this and use that one instead

package org.opengroup.osdu.odatadms.model.request;

import java.util.List;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import org.opengroup.osdu.core.common.model.storage.validation.ValidNotNullCollection;
import org.opengroup.osdu.odatadms.model.shared.DatasetRegistryValidationDoc;

import lombok.Data;

@Data
public class GetDatasetRegistryRequest {
    
    @ValidNotNullCollection
	@NotEmpty(message = DatasetRegistryValidationDoc.MISSING_DATASET_REGISTRIES_ARRAY)
	@Size(min = 1, max = 20, message = DatasetRegistryValidationDoc.MAX_DATASET_REGISTRIES_EXCEEDED) //TODO: need to support pagination of storage record get and then extend this back to 500
    public List<String> datasetRegistryIds;

}
