package com.mapzen.open.util;

import com.mapzen.open.support.MapzenTestRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.Assertions.assertThat;

@Config(emulateSdk = 18)
@RunWith(MapzenTestRunner.class)
public class SimpleCryptTest {
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void shouldBeSymmetric() throws Exception {
        SimpleCrypt simpleCrypt = SimpleCrypt.withSpecialSalt("for-testing");
        String expected = "hello this is a regular string";
        String encoded = simpleCrypt.encode(expected);
        assertThat(simpleCrypt.decode(encoded)).isEqualTo(expected);
    }
}
