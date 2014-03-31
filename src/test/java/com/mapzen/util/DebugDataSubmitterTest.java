package com.mapzen.util;

import com.mapzen.activity.BaseActivity;
import com.mapzen.support.MapzenTestRunner;

import com.google.common.io.Files;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.io.File;

import static com.mapzen.support.TestHelper.initBaseActivity;
import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class DebugDataSubmitterTest {
    DebugDataSubmitter submitter;
    MockWebServer server;
    BaseActivity activity;

    @Before
    public void setUp() throws Exception {
        MockResponse response = new MockResponse();
        server = new MockWebServer();
        server.enqueue(response);
        server.play();
        activity = initBaseActivity();
        submitter = new DebugDataSubmitter(activity);
        submitter.setRunInThread(false);
        submitter.setFile(new File(activity.getDb().getPath()));
        submitter.setEndpoint(server.getUrl("/fake").toString());
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    public void setEndpoint_shouldSubmitToChosenEndpoint() throws Exception {
        String expected = "/fake-endpoint";
        submitter.setEndpoint(server.getUrl(expected).toString());
        submitter.run();
        RecordedRequest request = server.takeRequest();
        assertThat(request.getPath()).isEqualTo(expected);
    }

    @Test
    public void shouldPostToEndpoint() throws Exception {
        submitter.run();
        RecordedRequest request = server.takeRequest();
        assertThat(request.getMethod()).isEqualTo("POST");
    }

    @Test
    public void setFile_shouldSubmitChosenFile() throws Exception {
        submitter.run();
        RecordedRequest request = server.takeRequest();
        File expectedFile = new File(activity.getDb().getPath());
        assertThat(request.getBody()).isEqualTo(Files.toByteArray(expectedFile));
    }
}
