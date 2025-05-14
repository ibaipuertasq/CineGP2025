package org.datanucleus.jdo;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.datanucleus.categories.UnitTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import es.deusto.spq.server.jdo.Asiento;
import es.deusto.spq.server.jdo.Sala;

@RunWith(MockitoJUnitRunner.class)
@Category(UnitTest.class)
public class SalaTest {

    private Sala sala;
    private List<Asiento> asientosMock;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        asientosMock = mock(List.class);
        sala = new Sala(1L, 1, 50, asientosMock, true);
    }

    @Test
    public void testConstructorsAndGetters() {
        assertEquals(1L, sala.getId());
        assertEquals(1, sala.getNumero());
        assertEquals(50, sala.getCapacidad());
        assertEquals(asientosMock, sala.getAsientos());
        assertTrue(sala.isDisponible());

        Sala defaultSala = new Sala();
        assertEquals(0, defaultSala.getId());
        assertEquals(0, defaultSala.getNumero());
        assertEquals(0, defaultSala.getCapacidad());
        assertNull(defaultSala.getAsientos());
        assertFalse(defaultSala.isDisponible());
    }

    @Test
    public void testSetters() {
        sala.setId(2L);
        sala.setNumero(2);
        sala.setCapacidad(100);
        sala.setAsientos(null);
        sala.setDisponible(false);

        assertEquals(2L, sala.getId());
        assertEquals(2, sala.getNumero());
        assertEquals(100, sala.getCapacidad());
        assertNull(sala.getAsientos());
        assertFalse(sala.isDisponible());
    }

    @Test
    public void testToStringMethods() {
        assertEquals("Sala{id=1, numero=1, capacidad=50, asientos=" + asientosMock + "}", sala.toString());
        assertEquals("Sala 1 con capacidad de 50 asientos.Dsiponible", sala.toStringCorto());
        sala.setDisponible(false);
        assertEquals("Sala 1 con capacidad de 50 asientos.No disponible", sala.toStringCorto());
    }
}