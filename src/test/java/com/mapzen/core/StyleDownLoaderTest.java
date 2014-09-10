package com.mapzen.core;

import com.mapzen.TestMapzenApplication;
import com.mapzen.support.MapzenTestRunner;

import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import java.io.File;

import static org.apache.commons.lang.StringUtils.join;
import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class StyleDownLoaderTest {
    StyleDownLoader downLoader;
    TestMapzenApplication app;
    MockWebServer server;

    @Before
    public void setup() throws Exception {
        app = (TestMapzenApplication) Robolectric.application;
        downLoader = new StyleDownLoader(app);
        server = new MockWebServer();
        server.play();
        downLoader.setHost(server.getUrl("/").toString());
    }

    @After
    public void teardown() throws Exception {
        server.shutdown();
    }

    @Test
    public void shouldRequestManifest() throws Exception {
        server.enqueue(new MockResponse());
        downLoader.download();
        RecordedRequest request = server.takeRequest();
        assertThat(request.getPath()).isEqualTo("/manifest");
    }

    @Test
    public void shouldDownloadFilesFromManifest() throws Exception {
        MockResponse response = new MockResponse();
        String[] manifest = new String[] { "file1", "file2", "file3", "path/to/file4" };
        response.setBody(join(manifest, "\n"));
        server.enqueue(response);
        for (String path: manifest) {
            server.enqueue(new MockResponse());
        }
        downLoader.download();
        Robolectric.runUiThreadTasksIncludingDelayedTasks();
        RecordedRequest request = server.takeRequest();
        assertThat(request.getPath()).isEqualTo("/manifest");
        for (String path: manifest) {
            assertThat(server.takeRequest().getPath()).isEqualTo("/" + path);
        }
    }

    @Test
    public void shouldWriteDownloadedFilesToExternalDrive() throws Exception {
        MockResponse response = new MockResponse();
        String[] manifest = new String[] { "file1", "file2", "file3", "path/to/file4" };
        response.setBody(join(manifest, "\n"));
        server.enqueue(response);
        for (String path: manifest) {
            server.enqueue(new MockResponse().setBody(path));
        }
        downLoader.download();
        Robolectric.runUiThreadTasksIncludingDelayedTasks();
        for (String path: manifest) {
            File file = new File(app.getExternalFilesDir(null).getAbsolutePath() + "/" + path);
            assertThat(file).exists();
        }
    }
}
