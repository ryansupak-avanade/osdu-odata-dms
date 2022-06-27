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

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;

@RestController
@RequestMapping("/health")
public class HealthCheckApi {

	@PermitAll
	@GetMapping("/liveness_check")
	public ResponseEntity<String> livenessCheck() {
		return new ResponseEntity<String>("File DMS Service is alive", HttpStatus.OK);
	}

	@PermitAll
	@GetMapping("/readiness_check")
	public ResponseEntity<String> readinessCheck() {
		return new ResponseEntity<String>("File DMS Service is ready", HttpStatus.OK);
	}
}
