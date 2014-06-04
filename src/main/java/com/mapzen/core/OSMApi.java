package com.mapzen.core;

import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.Token;


public class OSMApi extends DefaultApi10a {

    public static final String BASE_URL = "http://www.openstreetmap.org";
    private static final String OAUTH_URL = BASE_URL + "/oauth/";
    private static final String AUTHORIZATION_URL = OAUTH_URL + "authorize?oauth_token=%s";
    public static final String CREATE_GPX = "/api/0.6/gpx/create";

    public OSMApi() {
        super();
    }

    @Override
    public String getRequestTokenEndpoint() {
        return OAUTH_URL + "request_token";
    }

    @Override
    public String getAccessTokenEndpoint() {
        return OAUTH_URL + "access_token";
    }

    @Override
    public String getAuthorizationUrl(Token requestToken) {
        return String.format(AUTHORIZATION_URL, requestToken.getToken());
    }
}
