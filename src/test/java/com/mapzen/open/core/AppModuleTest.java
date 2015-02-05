package com.mapzen.open.core;

import com.mapzen.open.R;
import com.mapzen.open.support.MapzenTestRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import android.content.Context;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MapzenTestRunner.class)
public class AppModuleTest {
    @Test
    public void provideSimpleCrypt_shouldReturnSimpleCryptForRealOsmKey() throws Exception {
        Context mockContext = Mockito.mock(Context.class);
        when(mockContext.getString(R.string.osm_key)).thenReturn("real_key");
        AppModule appModule = new AppModule(mockContext);
        assertThat(appModule.provideSimpleCrypt()).isNotNull();
    }

    @Test
    public void provideSimpleCrypt_shouldNotReturnSimpleCryptForBogusOsmKey() throws Exception {
        Context mockContext = Mockito.mock(Context.class);
        when(mockContext.getString(R.string.osm_key)).thenReturn("bogus_key");
        AppModule appModule = new AppModule(mockContext);
        assertThat(appModule.provideSimpleCrypt()).isNull();
    }
}
