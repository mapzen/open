package com.mapzen.open;

import com.mapzen.open.core.CommonModule;
import com.mapzen.open.core.TestAppModule;

import java.util.Arrays;
import java.util.List;

public class TestMapzenApplication extends MapzenApplication {
    @Override
    protected List<Object> getModules() {
        return Arrays.asList(
                new CommonModule(this),
                new TestAppModule(this)
        );
    }
}
