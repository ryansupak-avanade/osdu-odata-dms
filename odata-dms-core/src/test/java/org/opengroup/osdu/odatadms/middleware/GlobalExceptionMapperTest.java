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

package org.opengroup.osdu.odatadms.middleware;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import javassist.NotFoundException;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;


import javax.validation.ValidationException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.mock;

@RunWith(PowerMockRunner.class)
public class GlobalExceptionMapperTest {

    @Mock
    private JaxRsDpsLog log;
    @InjectMocks
    private GlobalExceptionMapper sut;

    @Test
    public void should_useValuesInAppExceptionInResponse_When_AppExceptionIsHandledByGlobalExceptionMapper() {

        AppException exception = new AppException(409, "any reason", "any message");

        ResponseEntity<Object> response = sut.handleAppException(exception);
        assertEquals(409, response.getStatusCodeValue());
        assertEquals(exception.getError(), response.getBody());
    }

    @Test
    public void should_use404ValueInResponse_When_NotFoundExceptionIsHandledByGlobalExceptionMapper() {

        NotFoundException exception = new NotFoundException("any message");

         	ResponseEntity<Object> response = sut.handleNotFoundException(exception);
         	assertEquals(404, response.getStatusCodeValue());
         	assertTrue(response.getBody().toString().contains("any message"));
    }

//    @Test
//    public void should_use405ValueInResponse_When_NotAllowedExceptionIsHandledByGlobalExceptionMapper() {
//
//        NotAllowedException exception = new NotAllowedException("any message");
//
//        Response response = sut.toResponse(exception);
//        assertEquals(405, response.getStatus());
//        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
//        assertNotNull(response.getEntity());
//    }

//    @Test
//    public void should_use415ValueInResponse_When_NotSupportedExceptionIsHandledByGlobalExceptionMapper() {
//
//        NotSupportedException exception = new NotSupportedException("any message");
//
//        Response response = sut.toResponse(exception);
//        assertEquals(415, response.getStatus());
//        assertEquals(MediaType.APPLICATION_JSON, response.getMediaType().toString());
//        assertNotNull(response.getEntity());
//    }

    @Test
    public void should_useGenericValuesInResponse_When_ExceptionIsHandledByGlobalExceptionMapper() {

        Exception exception = new Exception("any message");

        ResponseEntity<Object> response = sut.handleGeneralException(exception);
        assertEquals(500, response.getStatusCodeValue());
        assertEquals("AppError(code=500, reason=Server error., message=An unknown error has occurred., errors=null, debuggingInfo=null, originalException=java.lang.Exception: any message)", response.getBody().toString());
    }

    @Test
    public  void should_useBadRequestInResponse_When_JsonProcessingExceptionIsHandledByGlobalExceptionMapper (){
        JsonProcessingException exception = new JsonParseException(null,"any message");

        ResponseEntity<Object> response = sut.handleJsonProcessingException(exception);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusCodeValue());
    }

    @Test
    public  void should_useBadRequestInResponse_When_handleUnrecognizedPropertyExceptionIsHandledByGlobalExceptionMapper (){
        UnrecognizedPropertyException exception = mock(UnrecognizedPropertyException.class);

        ResponseEntity<Object> response = sut.handleUnrecognizedPropertyException(exception);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusCodeValue());
    }

    @Test
    public  void should_useBadRequestInResponse_When_handleValidationExceptionIsHandledByGlobalExceptionMapper (){
        ValidationException exception = new ValidationException();

        ResponseEntity<Object> response = sut.handleValidationException(exception);
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusCodeValue());
    }

    @Test
    public  void should_useBadRequestInResponse_When_handleAccessDeniedExceptionIsHandledByGlobalExceptionMapper (){
        AccessDeniedException exception = new AccessDeniedException("Access is denied.");

        ResponseEntity<Object> response = sut.handleAccessDeniedException(exception);
        assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatusCodeValue());
    }
}