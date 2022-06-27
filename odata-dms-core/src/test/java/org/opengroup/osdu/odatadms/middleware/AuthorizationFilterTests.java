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

package org.opengroup.osdu.odatadms.middleware;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.opengroup.osdu.core.common.model.entitlements.AuthorizationResponse;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.http.RequestInfo;
import org.opengroup.osdu.core.common.provider.interfaces.IAuthorizationService;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class AuthorizationFilterTests {

    private static final String ROLE1 = "role1";
    private static final String ROLE2 = "role2";
    private static final String ROLE3 = "cron.job";

    @Mock
    private DpsHeaders headers;
    @Mock
    private RequestInfo requestInfo;
    @Mock
    private IAuthorizationService authorizationService;
    @InjectMocks
    private AuthorizationFilter sut;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(headers.getAuthorization()).thenReturn("Bearer 123456");
    }

    @Test
    public void should_authenticateRequest_when_resourceIsRolesAllowedAnnotated() {
        final String USER_EMAIL = "test@test.com";
        AuthorizationResponse authorizationResponse = AuthorizationResponse.builder().user(USER_EMAIL).build();
        when(this.authorizationService.authorizeAny(any(), eq(ROLE1), eq(ROLE2))).thenReturn(authorizationResponse);

        assertTrue(this.sut.hasRole(ROLE1, ROLE2));
        verify(headers).put(DpsHeaders.USER_EMAIL, USER_EMAIL);
    }

    @Test(expected = AppException.class)
    public void should_throwAppError_when_noAuthzProvided() {
        when(this.authorizationService.authorizeAny(any(), any())).thenThrow(new AppException(403, "", ""));
        final String USER_EMAIL = "test@test.com";

        this.sut.hasRole(ROLE1, ROLE2);
        assertEquals(USER_EMAIL, this.headers.getUserEmail());
    }

    @Test(expected = AppException.class)
    public void should_notAuthenticateRequest_when_appEngineCronHeaderIsNotAsExpectedForCronJob() {
        when(this.authorizationService.authorizeAny(any(), any())).thenThrow(new AppException(403, "", ""));
        when(this.requestInfo.isCronRequest()).thenReturn(false);
        this.sut.hasRole(ROLE3);
    }
}
