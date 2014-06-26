package com.mapzen.core;

import org.scribe.model.OAuthRequest;

import static org.scribe.model.Verb.GET;
import static org.scribe.model.Verb.POST;

public class OAuthRequestFactory {
    public OAuthRequest getOAuthRequest() {
        return new OAuthRequest(POST, OSMApi.BASE_URL + OSMApi.CREATE_GPX);
    }

    public OAuthRequest getPermissionsRequest() {
        return new OAuthRequest(GET, OSMApi.BASE_URL + OSMApi.CHECK_PERMISSIONS);
    }
}