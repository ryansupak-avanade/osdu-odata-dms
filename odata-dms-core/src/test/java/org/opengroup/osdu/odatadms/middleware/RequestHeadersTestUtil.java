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

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class RequestHeadersTestUtil {
	public static void setupRequestHeaderMock(Map<String, String> headers, HttpServletRequest request) {
    	// create an Enumeration over the header keys
    	Iterator<String> iterator = headers.keySet().iterator();
    	Enumeration<String> headerNames = new Enumeration<String>() {
    	    @Override
    	    public boolean hasMoreElements() {
    	        return iterator.hasNext();
    	    }

    	    @Override
    	    public String nextElement() {
    	        return iterator.next();
    	    }
    	};

    	when(request.getHeaderNames()).thenReturn(headerNames);
    	
    	when(request.getHeader(anyString())).thenAnswer(invocation -> {
    		return headers.get(invocation.getArguments()[0]);
    	});
    	
    	when(request.getHeaders(anyString())).thenAnswer(invocation -> {
    		
    		ArrayList<String> values = new ArrayList<String>();
    		values.add(headers.get(invocation.getArguments()[0]));
    		
    		
        	Iterator<String> it = values.iterator();
        	return new Enumeration<String>() {
        	    @Override
        	    public boolean hasMoreElements() {
        	        return it.hasNext();
        	    }

        	    @Override
        	    public String nextElement() {
        	        return it.next();
        	    }
        	};
    	});

    	
    	
    }
}
