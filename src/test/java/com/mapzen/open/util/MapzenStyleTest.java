package com.mapzen.open.util;

import com.mapzen.open.activity.BaseActivity;
import com.mapzen.open.support.MapzenTestRunner;
import com.mapzen.open.support.TestHelper;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.oscim.backend.AssetAdapter;

import android.content.Context;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.getShadowApplication;

@RunWith(MapzenTestRunner.class)
public class MapzenStyleTest {
    BaseActivity act;
    MapzenStyle.Theme theme;
    String pathToFile;

    @Before
    public void setup() throws Exception {
        act = TestHelper.initBaseActivity();
        AssetAdapter.init(new MapzenStyle.MapzenAssetAdapter(act));
        theme  = MapzenStyle.Theme.valueOf("MAPZEN");
        pathToFile = act.getExternalFilesDir(null).getAbsolutePath()
                + "/assets/" + "styles/mapzen.xml";
        Files.createParentDirs(new File(pathToFile));
    }

    @Test
    public void getRenderThemeAsStream_shouldReturnExternalFile() throws Exception {
        InputStream expected = writeExternalFile();
        InputStream actual = theme.getRenderThemeAsStream();
        assertThat(IOUtils.contentEquals(actual, expected)).isTrue();
    }

    @Test
    public void getRenderThemeAsStream_shouldNotReturnResourceFile() throws Exception {
        writeExternalFile();
        InputStream actual = theme.getRenderThemeAsStream();
        InputStream notExpected = getResourceStyleFile();
        assertThat(IOUtils.contentEquals(actual, notExpected)).isFalse();
    }

    @Test
    public void getRenderThemeAsStream_shouldReturnResourceFile() throws Exception {
        InputStream actual = theme.getRenderThemeAsStream();
        InputStream expected = getResourceStyleFile();
        assertThat(IOUtils.contentEquals(actual, expected)).isTrue();
    }

    @Test
    public void openFileAsStream_shouldReturnAssetsDirectoryIfNullExternalFiles() throws Exception {
        Context mockContext = Mockito.mock(Context.class);
        Mockito.when(mockContext.getExternalCacheDir()).thenReturn(null);
        Mockito.when(mockContext.getAssets()).thenReturn(getShadowApplication().getAssets());
        InputStream in = new MapzenStyle.MapzenAssetAdapter(mockContext)
                .openFileAsStream("styles/mapzen.xml");
        assertThat(in).isNotNull();
    }

    private InputStream writeExternalFile() throws IOException {
        Files.write("some content", new File(pathToFile), Charsets.UTF_8);
        return new ByteArrayInputStream(Files.toByteArray(new File(pathToFile)));
    }

    private InputStream getResourceStyleFile() throws IOException {
        return new ByteArrayInputStream(
                Files.toByteArray(new File("assets/styles/mapzen.xml")));
    }
}
