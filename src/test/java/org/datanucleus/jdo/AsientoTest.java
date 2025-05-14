package org.datanucleus.jdo;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.datanucleus.categories.UnitTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import es.deusto.spq.server.jdo.Asiento;
import es.deusto.spq.server.jdo.TipoAsiento;

@RunWith(MockitoJUnitRunner.class)
@Category(UnitTest.class)
public class AsientoTest {

    private Asiento asiento;
    private TipoAsiento tipoAsientoMock;

    @Before
    public void setUp() {
        tipoAsientoMock = mock(TipoAsiento.class);
        when(tipoAsientoMock.toString()).thenReturn("NORMAL");
        asiento = new Asiento(1L, 10, tipoAsientoMock, true);
    }

    @Test
    public void testConstructorsAndGetters() {
        assertEquals(1L, asiento.getId());
        assertEquals(10, asiento.getNumero());
        assertEquals(tipoAsientoMock, asiento.getTipo());
        assertTrue(asiento.isOcupado());

        Asiento defaultAsiento = new Asiento();
        assertEquals(0, defaultAsiento.getId());
        assertEquals(0, defaultAsiento.getNumero());
        assertNull(defaultAsiento.getTipo());
        assertFalse(defaultAsiento.isOcupado());
    }

    @Test
    public void testSetters() {
        asiento.setId(2L);
        asiento.setNumero(11);
        asiento.setTipo(null);
        asiento.setOcupado(false);

        assertEquals(2L, asiento.getId());
        assertEquals(11, asiento.getNumero());
        assertNull(asiento.getTipo());
        assertFalse(asiento.isOcupado());
    }

    @Test
    public void testToStringMethods() {
        assertEquals("Asiento{id=1, numero=10, tipo=NORMAL, ocupado=true}", asiento.toString());
        assertEquals("Asiento 10 (NORMAL) - Ocupado", asiento.toStringCorto());
        asiento.setOcupado(false);
        assertEquals("Asiento 10 (NORMAL) - Libre", asiento.toStringCorto());
    }
}
