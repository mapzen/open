package com.mapzen.open.core;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scribe.model.Token;

import java.util.Locale;

import static org.fest.assertions.api.Assertions.assertThat;

public class OSMApiTest {
    private OSMApi api;

    @Before
    public void setUp() throws Exception {
        api = new OSMApi();
    }

    @After
    public void tearDown() throws Exception {
        Locale.setDefault(Locale.US);
    }

    @Test
    public void shouldAppendLanguageCode() throws Exception {
        assertThat(api.getAuthorizationUrl(new Token("token", "secret"))).endsWith("locale=en");

        Locale.setDefault(Locale.FRENCH);
        assertThat(api.getAuthorizationUrl(new Token("token", "secret"))).endsWith("locale=fr");

        Locale.setDefault(Locale.GERMAN);
        assertThat(api.getAuthorizationUrl(new Token("token", "secret"))).endsWith("locale=de");
    }
}
