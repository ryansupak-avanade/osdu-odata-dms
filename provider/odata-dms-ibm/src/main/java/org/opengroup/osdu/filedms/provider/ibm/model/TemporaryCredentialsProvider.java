/* Licensed Materials - Property of IBM              */
/* (c) Copyright IBM Corp. 2020. All Rights Reserved.*/

package org.opengroup.osdu.odatadms.provider.ibm.model;

import com.amazonaws.auth.AWSSessionCredentials;
import com.amazonaws.auth.AWSSessionCredentialsProvider;

public class TemporaryCredentialsProvider implements AWSSessionCredentialsProvider {

    private TemporaryCredentials temporaryCredentials;

    public TemporaryCredentialsProvider(TemporaryCredentials temporaryCredentials) {
        this.temporaryCredentials = temporaryCredentials;

    }

    @Override
    public void refresh() {
        // TODO Auto-generated method stub

    }

    @Override
    public AWSSessionCredentials getCredentials() {
        return temporaryCredentials;
    }
    
}
