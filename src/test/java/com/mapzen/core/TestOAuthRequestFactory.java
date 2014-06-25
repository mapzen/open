package com.mapzen.core;

import org.mockito.Mockito;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class TestOAuthRequestFactory extends OAuthRequestFactory {
    @Override
    public OAuthRequest getOAuthRequest() {
        OAuthRequest mockRequest = Mockito.spy(super.getOAuthRequest());
        Response responseMock = mock(Response.class);
        doReturn(responseMock).when(mockRequest).send();
        doReturn(true).when(responseMock).isSuccessful();
        return mockRequest;
    }

    @Override
    public OAuthRequest getPermissionsRequest() {
        OAuthRequest mockRequest = Mockito.spy(super.getPermissionsRequest());
        Response responseMock = mock(Response.class);
        doReturn(responseMock).when(mockRequest).send();
        doReturn(true).when(responseMock).isSuccessful();
        return mockRequest;
    }
}
