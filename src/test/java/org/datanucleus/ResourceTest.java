package org.datanucleus;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.ws.rs.core.Response;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import es.deusto.spq.server.Resource;
import es.deusto.spq.server.jdo.Pelicula;
import es.deusto.spq.server.jdo.Sala;
import es.deusto.spq.server.jdo.TipoUsuario;
import es.deusto.spq.server.jdo.Usuario;

/**
 * Unit tests for the Resource class, focusing on user-related methods and additional endpoints.
 */
public class ResourceTest {

    private Resource resource;

    @Mock
    private PersistenceManager persistenceManager;

    @Mock
    private Transaction transaction;

    @Mock
    private PersistenceManagerFactory pmf;

    private static MockedStatic<JDOHelper> jdoHelper;

    @BeforeClass
    public static void setUpClass() {
        // Initialize the mock for JDOHelper at the class level
        jdoHelper = mockStatic(JDOHelper.class);
    }

    @AfterClass
    public static void tearDownClass() {
        // Close the JDOHelper mock after all tests
        if (jdoHelper != null) {
            jdoHelper.close();
        }
    }

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Configure the mock for JDOHelper to return the mocked pmf
        jdoHelper.when(() -> JDOHelper.getPersistenceManagerFactory("datanucleus.properties")).thenReturn(pmf);
        when(pmf.getPersistenceManager()).thenReturn(persistenceManager);
        when(persistenceManager.currentTransaction()).thenReturn(transaction);

        // Initialize the Resource object
        resource = new Resource();
    }

    @Test
    public void testLoginUserSuccess() {
        // Prepare test data
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password", "Calle 123", "123456789", TipoUsuario.CLIENTE);
        @SuppressWarnings("unchecked")
        Query<Usuario> query = mock(Query.class);
        when(persistenceManager.newQuery(Usuario.class, "nombreUsuario == :nombreUsuario && contrasenya == :contrasenya")).thenReturn(query);
        doNothing().when(query).setUnique(true);
        when(query.execute("juanperez", "password")).thenReturn(usuario);
        when(transaction.isActive()).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();

        // Call the method
        Response response = resource.loginUser(new Usuario(null, null, null, null, "juanperez", "password", null, null, null));

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("Juan", ((Usuario) response.getEntity()).getNombre());
    }

    @Test
    public void testLoginUserInvalidCredentials() {
        // Prepare test data
        @SuppressWarnings("unchecked")
        Query<Usuario> query = mock(Query.class);
        when(persistenceManager.newQuery(Usuario.class, "nombreUsuario == :nombreUsuario && contrasenya == :contrasenya")).thenReturn(query);
        doNothing().when(query).setUnique(true);
        when(query.execute("juanperez", "wrongpassword")).thenReturn(null);
        when(transaction.isActive()).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();

        // Call the method
        Response response = resource.loginUser(new Usuario(null, null, null, null, "juanperez", "wrongpassword", null, null, null));

        // Verify response
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals("Invalid credentials", response.getEntity());
    }

    @Test
    public void testRegisterUserSuccess() {
        // Prepare test data
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password", "Calle 123", "123456789", TipoUsuario.CLIENTE);
        when(persistenceManager.getObjectById(Usuario.class, "12345678A")).thenThrow(new JDOObjectNotFoundException());
        when(transaction.isActive()).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();

        // Call the method
        Response response = resource.registerUser(usuario);

        // Verify persistence
        ArgumentCaptor<Usuario> userCaptor = ArgumentCaptor.forClass(Usuario.class);
        verify(persistenceManager).makePersistent(userCaptor.capture());
        assertEquals("Juan", userCaptor.getValue().getNombre());
        assertEquals("juanperez", userCaptor.getValue().getNombreUsuario());
        assertEquals(TipoUsuario.CLIENTE, userCaptor.getValue().getTipoUsuario());

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testRegisterUserAlreadyExists() {
        // Prepare test data
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password", "Calle 123", "123456789", TipoUsuario.CLIENTE);
        when(persistenceManager.getObjectById(Usuario.class, "12345678A")).thenReturn(usuario);
        when(transaction.isActive()).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();

        // Call the method
        Response response = resource.registerUser(usuario);

        // Verify no persistence
        verify(persistenceManager, never()).makePersistent(any(Usuario.class));

        // Verify response
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals("User already exists", response.getEntity());
    }

    @Test
    public void testUpdateUserSuccess() {
        // Prepare test data
        Usuario existingUser = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password", "Calle 123", "123456789", TipoUsuario.CLIENTE);
        Usuario updatedUser = new Usuario("12345678A", "Juan", "Gómez", "juan.gomez@example.com", "juanperez", "newpassword", "Calle 456", "987654321", TipoUsuario.ADMINISTRADOR);
        when(persistenceManager.getObjectById(Usuario.class, "12345678A")).thenReturn(existingUser);
        when(transaction.isActive()).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();

        // Call the method
        Response response = resource.updateUser(updatedUser);

        // Verify updated fields
        assertEquals("Juan", existingUser.getNombre());
        assertEquals("Gómez", existingUser.getApellidos());
        assertEquals("juan.gomez@example.com", existingUser.getEmail());
        assertEquals("newpassword", existingUser.getContrasenya());
        assertEquals("Calle 456", existingUser.getDireccion());
        assertEquals("987654321", existingUser.getTelefono());
        assertEquals(TipoUsuario.ADMINISTRADOR, existingUser.getTipoUsuario());

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(existingUser, response.getEntity());
    }

    @Test
    public void testUpdateUserNotFound() {
        // Prepare test data
        Usuario updatedUser = new Usuario("12345678A", "Juan", "Gómez", "juan.gomez@example.com", "juanperez", "newpassword", "Calle 456", "987654321", TipoUsuario.ADMINISTRADOR);
        when(persistenceManager.getObjectById(Usuario.class, "12345678A")).thenThrow(new JDOObjectNotFoundException());
        when(transaction.isActive()).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();

        // Call the method
        Response response = resource.updateUser(updatedUser);

        // Verify response
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Usuario no encontrado", response.getEntity());
    }

    @Test
    public void testDeleteUserSuccess() {
        // Prepare test data
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password", "Calle 123", "123456789", TipoUsuario.CLIENTE);
        @SuppressWarnings("unchecked")
        Query<Usuario> query = mock(Query.class);
        when(persistenceManager.newQuery(Usuario.class, "nombreUsuario == :nombreUsuario")).thenReturn(query);
        doNothing().when(query).setUnique(true);
        when(query.execute("juanperez")).thenReturn(usuario);
        when(transaction.isActive()).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();

        // Call the method
        Response response = resource.deleteUser("juanperez");

        // Verify deletion
        verify(persistenceManager).deletePersistent(usuario);

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("Usuario eliminado con éxito", response.getEntity());
    }

    @Test
    public void testDeleteUserNotFound() {
        // Prepare test data
        @SuppressWarnings("unchecked")
        Query<Usuario> query = mock(Query.class);
        when(persistenceManager.newQuery(Usuario.class, "nombreUsuario == :nombreUsuario")).thenReturn(query);
        doNothing().when(query).setUnique(true);
        when(query.execute("juanperez")).thenReturn(null);
        when(transaction.isActive()).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();

        // Call the method
        Response response = resource.deleteUser("juanperez");

        // Verify no deletion
        verify(persistenceManager, never()).deletePersistent(any());

        // Verify response
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Usuario no encontrado", response.getEntity());
    }

    @Test
    public void testLogoutUserSuccess() {
        // Prepare test data
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password", "Calle 123", "123456789", TipoUsuario.CLIENTE);
        Resource.tokens.put(usuario, 123456789L);

        // Call the method
        Response response = resource.logoutUser(usuario);

        // Verify token removal
        assertEquals(false, Resource.tokens.containsKey(usuario));

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("User logged out successfully", response.getEntity());
    }

    @Test
    public void testLogoutUserNotLoggedIn() {
        // Prepare test data
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password", "Calle 123", "123456789", TipoUsuario.CLIENTE);

        // Call the method
        Response response = resource.logoutUser(usuario);

        // Verify response
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals("User not logged in", response.getEntity());
    }

    @Test
    public void testEliminarCuentaSuccess() {
        // Prepare test data
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password", "Calle 123", "123456789", TipoUsuario.CLIENTE);
        when(persistenceManager.getObjectById(Usuario.class, "12345678A")).thenReturn(usuario);
        when(transaction.isActive()).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.eliminarCuenta("12345678A");

        // Verify deletion
        verify(persistenceManager).deletePersistent(usuario);

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testEliminarCuentaNotFound() {
        // Prepare test data
        when(persistenceManager.getObjectById(Usuario.class, "12345678A")).thenThrow(new JDOObjectNotFoundException());
        when(transaction.isActive()).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.eliminarCuenta("12345678A");

        // Verify no deletion
        verify(persistenceManager, never()).deletePersistent(any());

        // Verify response
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("User not found", response.getEntity());
    }

    @Test
    public void testGetUsuarioIdSuccess() {
        // Prepare test data
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password", "Calle 123", "123456789", TipoUsuario.CLIENTE);
        @SuppressWarnings("unchecked")
        Query<Usuario> query = mock(Query.class);
        when(persistenceManager.newQuery(Usuario.class, "nombreUsuario == :nombreUsuario")).thenReturn(query);
        doNothing().when(query).setUnique(true);
        when(query.execute("juanperez")).thenReturn(usuario);
        when(transaction.isActive()).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.getUsuarioId("juanperez");

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(usuario, response.getEntity());
    }

    @Test
    public void testGetUsuarioIdNotFound() {
        // Prepare test data
        @SuppressWarnings("unchecked")
        Query<Usuario> query = mock(Query.class);
        when(persistenceManager.newQuery(Usuario.class, "nombreUsuario == :nombreUsuario")).thenReturn(query);
        doNothing().when(query).setUnique(true);
        when(query.execute("juanperez")).thenReturn(null);
        when(transaction.isActive()).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.getUsuarioId("juanperez");

        // Verify response
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("User not found", response.getEntity());
    }

    @Test
    public void testGetPeliculasSuccess() {
        // Prepare test data
        List<Pelicula> peliculas = new ArrayList<>();
        Pelicula pelicula = new Pelicula("Test Movie", "Drama", 120, null, "Test Director", "Test Synopsis", "18:00", null);
        peliculas.add(pelicula);
        @SuppressWarnings("unchecked")
        Query<Pelicula> query = mock(Query.class);
        when(persistenceManager.newQuery(Pelicula.class)).thenReturn(query);
        when(query.execute()).thenReturn(peliculas);
        when(transaction.isActive()).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.getPeliculas();

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(peliculas, response.getEntity());
    }

    @Test
    public void testGetPeliculasNotFound() {
        // Prepare test data
        List<Pelicula> peliculas = new ArrayList<>();
        @SuppressWarnings("unchecked")
        Query<Pelicula> query = mock(Query.class);
        when(persistenceManager.newQuery(Pelicula.class)).thenReturn(query);
        when(query.execute()).thenReturn(peliculas);
        when(transaction.isActive()).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.getPeliculas();

        // Verify response
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("No se encontraron películas", response.getEntity());
    }

    @Test
    public void testCrearPeliculaSuccess() {
        // Prepare test data
        Sala sala = new Sala(1, 1, 50, new ArrayList<>(), true);
        Pelicula pelicula = new Pelicula("New Movie", "Action", 110, null, "New Director", "New Synopsis", "20:00", sala);
        @SuppressWarnings("unchecked")
        Query<Pelicula> query = mock(Query.class);
        when(persistenceManager.newQuery(Pelicula.class, "titulo == :titulo")).thenReturn(query);
        doNothing().when(query).setUnique(true);
        when(query.execute("New Movie")).thenReturn(null); // Movie does not exist
        when(persistenceManager.getObjectById(Sala.class, 1)).thenReturn(sala);
        when(transaction.isActive()).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.crearPelicula(pelicula);

        // Verify persistence
        ArgumentCaptor<Pelicula> peliculaCaptor = ArgumentCaptor.forClass(Pelicula.class);
        verify(persistenceManager).makePersistent(peliculaCaptor.capture());
        assertEquals("New Movie", peliculaCaptor.getValue().getTitulo());
        assertEquals("Action", peliculaCaptor.getValue().getGenero());

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCrearPeliculaAlreadyExists() {
        // Prepare test data
        Sala sala = new Sala(1, 1, 50, new ArrayList<>(), true);
        Pelicula pelicula = new Pelicula("New Movie", "Action", 110, null, "New Director", "New Synopsis", "20:00", sala);
        @SuppressWarnings("unchecked")
        Query<Pelicula> query = mock(Query.class);
        when(persistenceManager.newQuery(Pelicula.class, "titulo == :titulo")).thenReturn(query);
        doNothing().when(query).setUnique(true);
        when(query.execute("New Movie")).thenReturn(pelicula); // Movie already exists
        when(transaction.isActive()).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.crearPelicula(pelicula);

        // Verify no persistence
        verify(persistenceManager, never()).makePersistent(any(Pelicula.class));

        // Verify response
        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
        assertEquals("Ya existe una película con ese título", response.getEntity());
    }

    @Test
    public void testGetSalasSuccess() {
        // Prepare test data
        List<Sala> salas = new ArrayList<>();
        Sala sala = new Sala(1, 1, 50, new ArrayList<>(), true);
        salas.add(sala);
        @SuppressWarnings("unchecked")
        Query<Sala> query = mock(Query.class);
        when(persistenceManager.newQuery(Sala.class)).thenReturn(query);
        when(query.execute()).thenReturn(salas);
        when(transaction.isActive()).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.getSalas();

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(salas, response.getEntity());
    }

    @Test
    public void testGetSalasNotFound() {
        // Prepare test data
        List<Sala> salas = new ArrayList<>();
        @SuppressWarnings("unchecked")
        Query<Sala> query = mock(Query.class);
        when(persistenceManager.newQuery(Sala.class)).thenReturn(query);
        when(query.execute()).thenReturn(salas);
        when(transaction.isActive()).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.getSalas();

        // Verify response
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("No se encontraron salas", response.getEntity());
    }
}