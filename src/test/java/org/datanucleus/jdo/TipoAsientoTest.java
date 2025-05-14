package org.datanucleus.jdo;

import static org.junit.Assert.*;
import org.junit.Test;

import es.deusto.spq.server.jdo.TipoAsiento;


public class TipoAsientoTest {

    @Test
    public void testEnumValues() {
        assertEquals(3, TipoAsiento.values().length);
        assertEquals(TipoAsiento.NORMAL, TipoAsiento.valueOf("NORMAL"));
        assertEquals(TipoAsiento.VIP, TipoAsiento.valueOf("VIP"));
        assertEquals(TipoAsiento.DISCAPACITADOS, TipoAsiento.valueOf("DISCAPACITADOS"));
    }
}
