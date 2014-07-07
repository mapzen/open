package com.mapzen.util;

import com.mapzen.activity.BaseActivity;
import com.mapzen.support.MapzenTestRunner;
import com.mapzen.support.TestHelper;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class MapzenThemeTest {
    BaseActivity act;
    MapzenTheme theme;
    String pathToFile;

    @Before
    public void setup() throws Exception {
        act = TestHelper.initBaseActivity();
        theme  = MapzenTheme.valueOf("MAPZEN");
        theme.setContext(act);
        pathToFile = act.getExternalFilesDir(null).getAbsolutePath() + "/" + "styles/mapzen.xml";
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

    private InputStream writeExternalFile() throws IOException {
        Files.write("some content", new File(pathToFile), Charsets.UTF_8);
        return new ByteArrayInputStream(Files.toByteArray(new File(pathToFile)));
    }

    private InputStream getResourceStyleFile() throws IOException {
        return new ByteArrayInputStream(
                Files.toByteArray(new File("assets/styles/mapzen.xml")));
    }

}
