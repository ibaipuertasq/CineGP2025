package org.datanucleus.jdo;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import es.deusto.spq.server.jdo.Pelicula;
import es.deusto.spq.server.jdo.Sala;

@RunWith(MockitoJUnitRunner.class)
public class PeliculaTest {

    private Pelicula pelicula;
    private Sala salaMock;
    private Date fechaEstreno;

    @Before
    public void setUp() {
        salaMock = mock(Sala.class);
        fechaEstreno = new Date();
        pelicula = new Pelicula("Inception", "Sci-Fi", 148, fechaEstreno, "Nolan", "Dreams", "18:00", salaMock);
    }

    @Test
    public void testConstructorsAndGetters() {
        assertEquals("Inception", pelicula.getTitulo());
        assertEquals("Sci-Fi", pelicula.getGenero());
        assertEquals(148, pelicula.getDuracion());
        assertEquals(fechaEstreno, pelicula.getFechaEstreno());
        assertEquals("Nolan", pelicula.getDirector());
        assertEquals("Dreams", pelicula.getSinopsis());
        assertEquals("18:00", pelicula.getHorario());
        assertEquals(salaMock, pelicula.getSala());
        assertEquals(0, pelicula.getId());

        Pelicula defaultPelicula = new Pelicula();
        assertNull(defaultPelicula.getTitulo());
        assertNull(defaultPelicula.getGenero());
        assertEquals(0, defaultPelicula.getDuracion());
        assertNull(defaultPelicula.getFechaEstreno());
        assertNull(defaultPelicula.getDirector());
        assertNull(defaultPelicula.getSinopsis());
        assertNull(defaultPelicula.getHorario());
        assertNull(defaultPelicula.getSala());
    }

    @Test
    public void testSetters() {
        pelicula.setId(1L);
        pelicula.setTitulo("Matrix");
        pelicula.setGenero("Action");
        pelicula.setDuracion(136);
        pelicula.setFechaEstreno(null);
        pelicula.setDirector("Wachowski");
        pelicula.setSinopsis("Reality");
        pelicula.setHorario("20:00");
        pelicula.setSala(null);

        assertEquals(1L, pelicula.getId());
        assertEquals("Matrix", pelicula.getTitulo());
        assertEquals("Action", pelicula.getGenero());
        assertEquals(136, pelicula.getDuracion());
        assertNull(pelicula.getFechaEstreno());
        assertEquals("Wachowski", pelicula.getDirector());
        assertEquals("Reality", pelicula.getSinopsis());
        assertEquals("20:00", pelicula.getHorario());
        assertNull(pelicula.getSala());
    }

    @Test
    public void testToStringMethods() {
        String toString = pelicula.toString();
        assertTrue(toString.contains("Inception"));
        assertTrue(toString.contains("Sci-Fi"));
        String toStringCorto = pelicula.toStringCorto();
        assertTrue(toStringCorto.contains("Inception"));
        Pelicula nullDatePelicula = new Pelicula("Test", "Test", 0, null, "Test", "Test", "Test", null);
        assertTrue(nullDatePelicula.toStringCorto().contains("No disponible"));
    }
}