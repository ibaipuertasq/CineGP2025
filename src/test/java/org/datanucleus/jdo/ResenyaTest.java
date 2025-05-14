package org.datanucleus.jdo;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.datanucleus.categories.UnitTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import es.deusto.spq.server.jdo.Pelicula;
import es.deusto.spq.server.jdo.Resenya;
import es.deusto.spq.server.jdo.Usuario;

@RunWith(MockitoJUnitRunner.class)
@Category(UnitTest.class)
public class ResenyaTest {

    private Resenya resenya;
    private Usuario usuarioMock;
    private Pelicula peliculaMock;

    @Before
    public void setUp() {
        usuarioMock = mock(Usuario.class);
        peliculaMock = mock(Pelicula.class);
        
        when(usuarioMock.getNombre()).thenReturn("John");
        when(peliculaMock.getTitulo()).thenReturn("Inception");
        when(usuarioMock.toString()).thenReturn("UsuarioMock");
        when(peliculaMock.toString()).thenReturn("PeliculaMock");
        
        resenya = new Resenya("Great movie!", 5, usuarioMock, peliculaMock);
    }

    @Test
    public void testConstructorsAndGetters() {
        // Parameterized constructor
        assertEquals("Great movie!", resenya.getComentario());
        assertEquals(5, resenya.getPuntuacion());
        assertEquals(usuarioMock, resenya.getUsuario());
        assertEquals(peliculaMock, resenya.getPelicula());
        assertEquals(0, resenya.getId());

        // Default constructor
        Resenya defaultResenya = new Resenya();
        assertNull(defaultResenya.getComentario());
        assertEquals(0, defaultResenya.getPuntuacion());
        assertNull(defaultResenya.getUsuario());
        assertNull(defaultResenya.getPelicula());
        assertEquals(0, defaultResenya.getId());
    }

    @Test
    public void testSetters() {
        // Test individual setters
        resenya.setId(1L);
        assertEquals(1L, resenya.getId());

        resenya.setComentario("New comment");
        assertEquals("New comment", resenya.getComentario());

        resenya.setPuntuacion(4);
        assertEquals(4, resenya.getPuntuacion());

        Usuario newUsuario = mock(Usuario.class);
        resenya.setUsuario(newUsuario);
        assertEquals(newUsuario, resenya.getUsuario());

        Pelicula newPelicula = mock(Pelicula.class);
        resenya.setPelicula(newPelicula);
        assertEquals(newPelicula, resenya.getPelicula());

        // Test null values
        resenya.setComentario(null);
        resenya.setUsuario(null);
        resenya.setPelicula(null);

        assertNull(resenya.getComentario());
        assertNull(resenya.getUsuario());
        assertNull(resenya.getPelicula());
    }

    @Test
    public void testPuntuacionBoundaries() {
        // Test minimum valid score
        resenya.setPuntuacion(1);
        assertEquals(1, resenya.getPuntuacion());

        // Test maximum valid score
        resenya.setPuntuacion(5);
        assertEquals(5, resenya.getPuntuacion());

        // Test invalid scores (should be allowed if there are no validation rules)
        resenya.setPuntuacion(0);
        assertEquals(0, resenya.getPuntuacion());

        resenya.setPuntuacion(6);
        assertEquals(6, resenya.getPuntuacion());

        resenya.setPuntuacion(-1);
        assertEquals(-1, resenya.getPuntuacion());
    }

    @Test
    public void testToStringMethods() {
        // Test toString
        String expectedToString = "Resenya{id=0, comentario='Great movie!', puntuacion=5, usuario=UsuarioMock, pelicula=PeliculaMock}";
        assertEquals(expectedToString, resenya.toString());

        // Test toStringCorto
        String expectedToStringCorto = "Resenya 0 -> Pelicula: Inception, Usuario: John, Comentario: Great movie!, Puntuacion: 5";
        assertEquals(expectedToStringCorto, resenya.toStringCorto());

        // Test with null values
        Resenya nullResenya = new Resenya();
        // Set mocks to avoid NullPointerException
        Usuario nullUsuario = mock(Usuario.class);
        Pelicula nullPelicula = mock(Pelicula.class);
        when(nullUsuario.getNombre()).thenReturn(null);
        when(nullPelicula.getTitulo()).thenReturn(null);
        when(nullUsuario.toString()).thenReturn("null");
        when(nullPelicula.toString()).thenReturn("null");
        nullResenya.setUsuario(nullUsuario);
        nullResenya.setPelicula(nullPelicula);
        assertEquals("Resenya{id=0, comentario='null', puntuacion=0, usuario=null, pelicula=null}", nullResenya.toString());
        assertEquals("Resenya 0 -> Pelicula: null, Usuario: null, Comentario: null, Puntuacion: 0", nullResenya.toStringCorto());

        // Test with empty comment
        resenya.setComentario("");
        assertEquals("Resenya 0 -> Pelicula: Inception, Usuario: John, Comentario: , Puntuacion: 5", resenya.toStringCorto());
    }
}