package com.mapzen.android;

import org.mockito.Mockito;

public final class TestPelias extends Pelias {

    public TestPelias(PeliasService service) {
        super(service);
    }

    public static PeliasService getPeliasMock() {
        PeliasService service = Mockito.mock(PeliasService.class);
        instance = new TestPelias(service);
        return service;
    }
}