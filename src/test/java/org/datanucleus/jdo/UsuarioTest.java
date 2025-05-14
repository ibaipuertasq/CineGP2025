package org.datanucleus.jdo;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import org.datanucleus.categories.UnitTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import es.deusto.spq.server.jdo.Usuario;
import es.deusto.spq.server.jdo.TipoUsuario; // Add this import for TipoUsuario

@Category(UnitTest.class)
@RunWith(MockitoJUnitRunner.class)
public class UsuarioTest {

    private Usuario usuario;
    private TipoUsuario tipoUsuarioMock;

    @Before
    public void setUp() {
        tipoUsuarioMock = mock(TipoUsuario.class);
        when(tipoUsuarioMock.toString()).thenReturn("CLIENTE");
        usuario = new Usuario("12345678A", "John", "Doe", "john.doe@example.com", "johndoe", "password123", "123 Main St", "123456789", tipoUsuarioMock);
    }

    @Test
    public void testConstructorsAndGetters() {
        // Test full constructor and getters
        assertEquals("12345678A", usuario.getDni());
        assertEquals("John", usuario.getNombre());
        assertEquals("Doe", usuario.getApellidos());
        assertEquals("john.doe@example.com", usuario.getEmail());
        assertEquals("johndoe", usuario.getNombreUsuario());
        assertEquals("password123", usuario.getContrasenya());
        assertEquals("123 Main St", usuario.getDireccion());
        assertEquals("123456789", usuario.getTelefono());
        assertEquals(tipoUsuarioMock, usuario.getTipoUsuario());

        // Test default constructor
        Usuario defaultUsuario = new Usuario();
        assertEquals("", defaultUsuario.getDni());
        assertEquals("", defaultUsuario.getNombre());
        assertEquals("", defaultUsuario.getApellidos());
        assertEquals("", defaultUsuario.getEmail());
        assertEquals("", defaultUsuario.getNombreUsuario());
        assertEquals("", defaultUsuario.getContrasenya());
        assertEquals("", defaultUsuario.getDireccion());
        assertEquals("", defaultUsuario.getTelefono());
        assertEquals(TipoUsuario.CLIENTE, defaultUsuario.getTipoUsuario());

        // Test username-password constructor
        Usuario user1 = new Usuario("testuser", "testpass");
        assertEquals("testuser", user1.getNombreUsuario());
        assertEquals("testpass", user1.getContrasenya());
        assertNull(user1.getDni());

        // Test username-only constructor
        Usuario user2 = new Usuario("testuser2");
        assertEquals("testuser2", user2.getNombreUsuario());
        assertNull(user2.getContrasenya());
        assertNull(user2.getDni());
    }

    @Test
    public void testSetters() {
        usuario.setDni("87654321B");
        usuario.setNombre("Jane");
        usuario.setApellidos("Smith");
        usuario.setEmail("jane.smith@example.com");
        usuario.setNombreUsuario("janesmith");
        usuario.setContrasenya("newpass");
        usuario.setDireccion("456 Oak St");
        usuario.setTelefono("987654321");
        usuario.setTipoUsuario(null);

        assertEquals("87654321B", usuario.getDni());
        assertEquals("Jane", usuario.getNombre());
        assertEquals("Smith", usuario.getApellidos());
        assertEquals("jane.smith@example.com", usuario.getEmail());
        assertEquals("janesmith", usuario.getNombreUsuario());
        assertEquals("newpass", usuario.getContrasenya());
        assertEquals("456 Oak St", usuario.getDireccion());
        assertEquals("987654321", usuario.getTelefono());
        assertNull(usuario.getTipoUsuario());
    }

    @Test
    public void testToStringMethods() {
        assertEquals("Usuario{dni='12345678A', nombre='John', apellidos='Doe', email='john.doe@example.com', nombreUsuario='johndoe', telefono='123456789', tipoUsuario=CLIENTE}", usuario.toString());
        assertEquals("Usuario John con DNI:12345678A y nombreUsuario: johndoe", usuario.toStringCorto());
    }
}