package com.mapzen.android;

import org.mockito.Mockito;

public final class TestPelias {
    private TestPelias() {
    }

    public static PeliasService getPeliasMock() {
        PeliasService service = Mockito.mock(PeliasService.class);
        Pelias.setInstance(service);
        return service;
    }
}
