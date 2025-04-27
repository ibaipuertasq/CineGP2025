package org.datanucleus.jdo;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import es.deusto.spq.server.jdo.Cine;
import es.deusto.spq.server.jdo.Sala;

@RunWith(MockitoJUnitRunner.class)
public class CineTest {

    private Cine cine;
    private List<Sala> salas;
    private Sala salaMock;

    @Before
    public void setUp() {
        salas = new ArrayList<>(); // Use real ArrayList instead of mock
        salaMock = mock(Sala.class);
        cine = new Cine(1L, "Cinema", "123 Main St", salas);
    }

    @Test
    public void testConstructorsAndGetters() {
        // Parameterized constructor
        assertEquals(1L, cine.getId());
        assertEquals("Cinema", cine.getNombre());
        assertEquals("123 Main St", cine.getDireccion());
        assertEquals(salas, cine.getSalas());

        // Default constructor
        Cine defaultCine = new Cine();
        assertEquals(0, defaultCine.getId());
        assertNull(defaultCine.getNombre());
        assertNull(defaultCine.getDireccion());
        assertNull(defaultCine.getSalas());
    }

    @Test
    public void testSetters() {
        // Test individual setters
        cine.setId(2L);
        assertEquals(2L, cine.getId());

        cine.setNombre("New Cinema");
        assertEquals("New Cinema", cine.getNombre());

        cine.setDireccion("456 New St");
        assertEquals("456 New St", cine.getDireccion());

        List<Sala> newSalas = new ArrayList<>();
        cine.setSalas(newSalas);
        assertEquals(newSalas, cine.getSalas());

        // Test null values
        cine.setNombre(null);
        cine.setDireccion(null);
        cine.setSalas(null);

        assertNull(cine.getNombre());
        assertNull(cine.getDireccion());
        assertNull(cine.getSalas());
    }

    @Test
    public void testSalasListOperations() {
        // Add a real Sala to the list
        cine.getSalas().add(salaMock);
        assertEquals(1, cine.getSalas().size());
        assertTrue(cine.getSalas().contains(salaMock));

        cine.getSalas().remove(0);
        assertEquals(0, cine.getSalas().size());
    }

    @Test
    public void testToStringMethods() {
        // Test toString with valid salas
        assertEquals("Cine{id=1, nombre='Cinema', direccion='123 Main St', salas=[]}", cine.toString());

        // Test toShortString
        assertEquals("Cine{id=1, nombre='Cinema'}", cine.toShortString());
        
        // Test toString and toShortString with null fields
        cine.setNombre(null);
        cine.setSalas(null);
        assertEquals("Cine{id=1, nombre='null', direccion='123 Main St', salas=null}", cine.toString());
        assertEquals("Cine{id=1, nombre='null'}", cine.toShortString());

        // Test with empty name
        cine.setNombre("");
        assertEquals("Cine{id=1, nombre='', direccion='123 Main St', salas=null}", cine.toString());
        assertEquals("Cine{id=1, nombre=''}", cine.toShortString());
    }

    @Test
    public void testEqualsAndHashCode() {
        // Create a Cine with the same fields
        List<Sala> sameSalas = new ArrayList<>();
        Cine sameCine = new Cine(1L, "Cinema", "123 Main St", sameSalas);
        Cine differentCine = new Cine(2L, "Other Cinema", "456 Other St", new ArrayList<>());
        
        // Test equality
        assertTrue(cine.equals(sameCine));
        assertFalse(cine.equals(differentCine));
        
        // Test hashCode
        assertEquals(cine.hashCode(), sameCine.hashCode());
        assertNotEquals(cine.hashCode(), differentCine.hashCode());
        
        // Test with null
        assertFalse(cine.equals(null));
        
        // Test with different class
        assertFalse(cine.equals(new Object()));
    }
}