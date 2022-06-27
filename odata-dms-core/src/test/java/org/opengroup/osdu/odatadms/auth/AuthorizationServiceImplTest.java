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

package org.opengroup.osdu.odatadms.auth;

import org.opengroup.osdu.core.common.model.entitlements.AuthorizationResponse;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.opengroup.osdu.core.common.model.entitlements.EntitlementsException;
import org.opengroup.osdu.core.common.model.entitlements.GroupInfo;
import org.opengroup.osdu.core.common.model.entitlements.Groups;
import org.opengroup.osdu.core.common.http.HttpResponse;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsFactory;
import org.opengroup.osdu.core.common.entitlements.IEntitlementsService;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.opengroup.osdu.core.common.entitlements.AuthorizationServiceImpl;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.powermock.modules.junit4.PowerMockRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class AuthorizationServiceImplTest {

    @Mock
    IEntitlementsFactory entitlementsFactory;
    @Mock
    IEntitlementsService service;
    @Mock
    JaxRsDpsLog log;

    @InjectMocks
    AuthorizationServiceImpl sut;

    @Before
    public void setup(){
        when(entitlementsFactory.create(any())).thenReturn(service);
    }

    @Test
    public void should_returnUser_when_ussrHasPermission()throws EntitlementsException {
        sut = createSut("service.legal.user");

        AuthorizationResponse result = sut.authorizeAny(DpsHeaders.createFromMap(new HashMap<>()), "service.legal.user");

        assertEquals("akelham@bbc.com", result.getUser());
    }

    @Test
    public void should_returnUser_when_ussrHasAnyPermission()throws EntitlementsException{
        sut = createSut("service.legal.editor");

        AuthorizationResponse result = sut.authorizeAny(DpsHeaders.createFromMap(new HashMap<>()), "service.legal.user", "service.legal.editor");

        assertEquals("akelham@bbc.com", result.getUser());
    }

    @Test
    public void should_throwForbidden_when_userDoesNotHaveRequiredPermission()throws EntitlementsException {
        sut = createSut("service.legal.user");

        try {
            sut.authorizeAny(DpsHeaders.createFromMap(new HashMap<>()), "service.legal.editor");
            fail("expected exception");
        }catch(AppException ex){
            assertEquals(401, ex.getError().getCode());
        }
    }

    @Test
    public void should_throwServerError_when_getGroupsThrowsServerError()throws EntitlementsException{
        sut = createSut("service.legal.user");
        HttpResponse response = new HttpResponse();
        response.setResponseCode(500);
        when(service.getGroups()).thenThrow(new EntitlementsException("", response));
        try {
            sut.authorizeAny(DpsHeaders.createFromMap(new HashMap<>()), "service.legal.editor");
            fail("expected exception");
        }catch(AppException ex){
            assertEquals(500, ex.getError().getCode());
        }
    }

    @Test
    public void should_throw400AppError_when_getGroupsThrows400EntitlementsError()throws EntitlementsException{
        sut = createSut("service.legal.user");
        HttpResponse response = new HttpResponse();
        response.setResponseCode(400);
        when(service.getGroups()).thenThrow(new EntitlementsException("", response));
        try {
            sut.authorizeAny(DpsHeaders.createFromMap(new HashMap<>()), "service.legal.editor");
            fail("expected exception");
        }catch(AppException ex){
            assertEquals(400, ex.getError().getCode());
        }
    }

    private AuthorizationServiceImpl createSut(String... roles) throws EntitlementsException {
        List<GroupInfo> groupInfos = new ArrayList<>();

        for(String s : roles) {
            GroupInfo group = new GroupInfo();
            group.setName(s);
            groupInfos.add(group);
        }
        Groups output = new Groups();
        output.setMemberEmail("akelham@bbc.com");
        output.setDesId("akelham@bbc.com");
        output.setGroups(groupInfos);

        when(service.getGroups()).thenReturn(output);

        return sut;
    }


}
