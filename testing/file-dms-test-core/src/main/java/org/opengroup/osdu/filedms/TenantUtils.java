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

public class TenantUtils {

	public static String getTenantName() {
		// use "opendes" for local
		//return "opendes";
		return System.getProperty("TENANT_NAME", System.getenv("TENANT_NAME"));
	}

	public static String getFirstTenantName() {
		return getTenantName();
	}
}
