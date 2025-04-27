package org.datanucleus.jdo;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import es.deusto.spq.server.jdo.Cine;
import es.deusto.spq.server.jdo.Entrada;
import es.deusto.spq.server.jdo.TipoAsiento;
import es.deusto.spq.server.jdo.Usuario;

@RunWith(MockitoJUnitRunner.class)
public class EntradaTest {

    private Entrada entrada;
    private Usuario usuarioMock;
    private Cine cineMock;
    private TipoAsiento tipoAsientoMock;

    @Before
    public void setUp() {
        usuarioMock = mock(Usuario.class);
        cineMock = mock(Cine.class);
        tipoAsientoMock = mock(TipoAsiento.class);
        
        when(usuarioMock.getNombre()).thenReturn("John");
        when(cineMock.getNombre()).thenReturn("Cinema");
        when(tipoAsientoMock.toString()).thenReturn("NORMAL");
        
        entrada = new Entrada(usuarioMock, cineMock, 10, 5, tipoAsientoMock);
    }

    @Test
    public void testConstructorsAndGetters() {
        // Parameterized constructor
        assertEquals(usuarioMock, entrada.getUsuario());
        assertEquals(cineMock, entrada.getCine());
        assertEquals(10, entrada.getPrecio());
        assertEquals(5, entrada.getAsiento());
        assertEquals(tipoAsientoMock, entrada.getTipoAsiento());
        assertEquals(0, entrada.getId());

        // Default constructor
        Entrada defaultEntrada = new Entrada();
        assertNotNull(defaultEntrada.getUsuario());
        assertNotNull(defaultEntrada.getCine());
        assertEquals(0, defaultEntrada.getPrecio());
        assertEquals(0, defaultEntrada.getAsiento());
        assertNull(defaultEntrada.getTipoAsiento());
        assertEquals(0, defaultEntrada.getId());
    }

    @Test
    public void testSetters() {
        // Test individual setters
        entrada.setId(1L);
        assertEquals(1L, entrada.getId());

        Usuario newUsuario = mock(Usuario.class);
        entrada.setUsuario(newUsuario);
        assertEquals(newUsuario, entrada.getUsuario());

        Cine newCine = mock(Cine.class);
        entrada.setCine(newCine);
        assertEquals(newCine, entrada.getCine());

        entrada.setPrecio(15);
        assertEquals(15, entrada.getPrecio());

        entrada.setAsiento(6);
        assertEquals(6, entrada.getAsiento());

        TipoAsiento newTipo = mock(TipoAsiento.class);
        entrada.setTipoAsiento(newTipo);
        assertEquals(newTipo, entrada.getTipoAsiento());

        // Test null values
        entrada.setUsuario(null);
        entrada.setCine(null);
        entrada.setTipoAsiento(null);

        assertNull(entrada.getUsuario());
        assertNull(entrada.getCine());
        assertNull(entrada.getTipoAsiento());
    }

    @Test
    public void testToStringMethods() {
        // Test toString with valid usuario and cine
        String expectedToString = "Entrada 0 usuario: John, cine: Cinema, asiento: 5, tipoAsiento: NORMAL";
        assertEquals(expectedToString, entrada.toString());

        // Test toStringCorto with valid cine
        String expectedToStringCorto = "Entrada 0 para cine -> Cinema";
        assertEquals(expectedToStringCorto, entrada.toStringCorto());

        // Test with null values
        Entrada nullEntrada = new Entrada();
        // Set mocks to avoid NullPointerException
        Usuario nullUsuario = mock(Usuario.class);
        Cine nullCine = mock(Cine.class);
        when(nullUsuario.getNombre()).thenReturn(null);
        when(nullCine.getNombre()).thenReturn(null);
        nullEntrada.setUsuario(nullUsuario);
        nullEntrada.setCine(nullCine);
        assertEquals("Entrada 0 usuario: null, cine: null, asiento: 0, tipoAsiento: null", nullEntrada.toString());
        assertEquals("Entrada 0 para cine -> null", nullEntrada.toStringCorto());

        // Test with empty names
        when(usuarioMock.getNombre()).thenReturn("");
        when(cineMock.getNombre()).thenReturn("");
        assertEquals("Entrada 0 usuario: , cine: , asiento: 5, tipoAsiento: NORMAL", entrada.toString());
        assertEquals("Entrada 0 para cine -> ", entrada.toStringCorto());
    }

    @Test
    public void testEdgeCases() {
        // Test negative price
        entrada.setPrecio(-5);
        assertEquals(-5, entrada.getPrecio());

        // Test negative seat number
        entrada.setAsiento(-1);
        assertEquals(-1, entrada.getAsiento());

        // Test max values
        entrada.setPrecio(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, entrada.getPrecio());

        entrada.setAsiento(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, entrada.getAsiento());
    }
}