package org.datanucleus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import es.deusto.spq.server.jdo.Asiento;
import es.deusto.spq.server.jdo.Cine;
import es.deusto.spq.server.jdo.Entrada;
import es.deusto.spq.server.jdo.Pelicula;
import es.deusto.spq.server.jdo.Resenya;
import es.deusto.spq.server.jdo.Sala;
import es.deusto.spq.server.jdo.TipoAsiento;
import es.deusto.spq.server.jdo.TipoUsuario;
import es.deusto.spq.server.jdo.Usuario;

/**
 * Unit tests for the Resource class, focusing on user-related methods and
 * additional endpoints.
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
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password",
                "Calle 123", "123456789", TipoUsuario.CLIENTE);
        @SuppressWarnings("unchecked")
        Query<Usuario> query = mock(Query.class);
        when(persistenceManager.newQuery(Usuario.class,
                "nombreUsuario == :nombreUsuario && contrasenya == :contrasenya")).thenReturn(query);
        doNothing().when(query).setUnique(true);
        when(query.execute("juanperez", "password")).thenReturn(usuario);
        when(transaction.isActive()).thenReturn(true).thenReturn(false);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource
                .loginUser(new Usuario(null, null, null, null, "juanperez", "password", null, null, null));

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("Juan", ((Usuario) response.getEntity()).getNombre());
    }

    @Test
    public void testLoginUserInvalidCredentials() {
        // Prepare test data
        @SuppressWarnings("unchecked")
        Query<Usuario> query = mock(Query.class);
        when(persistenceManager.newQuery(Usuario.class,
                "nombreUsuario == :nombreUsuario && contrasenya == :contrasenya")).thenReturn(query);
        doNothing().when(query).setUnique(true);
        when(query.execute("juanperez", "wrongpassword")).thenReturn(null);
        when(transaction.isActive()).thenReturn(true).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource
                .loginUser(new Usuario(null, null, null, null, "juanperez", "wrongpassword", null, null, null));

        // Verify response
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals("Invalid credentials", response.getEntity());
    }

    @Test
    public void testRegisterUserSuccess() {
        // Prepare test data
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password",
                "Calle 123", "123456789", TipoUsuario.CLIENTE);
        when(persistenceManager.getObjectById(Usuario.class, "12345678A")).thenThrow(new JDOObjectNotFoundException());
        when(transaction.isActive()).thenReturn(true).thenReturn(false);
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
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password",
                "Calle 123", "123456789", TipoUsuario.CLIENTE);
        when(persistenceManager.getObjectById(Usuario.class, "12345678A")).thenReturn(usuario);
        when(transaction.isActive()).thenReturn(true).thenReturn(true);
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
        Usuario existingUser = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password",
                "Calle 123", "123456789", TipoUsuario.CLIENTE);
        Usuario updatedUser = new Usuario("12345678A", "Juan", "Gómez", "juan.gomez@example.com", "juanperez",
                "newpassword", "Calle 456", "987654321", TipoUsuario.ADMINISTRADOR);
        when(persistenceManager.getObjectById(Usuario.class, "12345678A")).thenReturn(existingUser);
        when(transaction.isActive()).thenReturn(true).thenReturn(false);
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
        Usuario updatedUser = new Usuario("12345678A", "Juan", "Gómez", "juan.gomez@example.com", "juanperez",
                "newpassword", "Calle 456", "987654321", TipoUsuario.ADMINISTRADOR);
        when(persistenceManager.getObjectById(Usuario.class, "12345678A")).thenThrow(new JDOObjectNotFoundException());
        when(transaction.isActive()).thenReturn(true).thenReturn(true);
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
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password",
                "Calle 123", "123456789", TipoUsuario.CLIENTE);
        @SuppressWarnings("unchecked")
        Query<Usuario> query = mock(Query.class);
        when(persistenceManager.newQuery(Usuario.class, "nombreUsuario == :nombreUsuario")).thenReturn(query);
        doNothing().when(query).setUnique(true);
        when(query.execute("juanperez")).thenReturn(usuario);
        when(transaction.isActive()).thenReturn(true).thenReturn(false);
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
        when(transaction.isActive()).thenReturn(true).thenReturn(true);
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
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password",
                "Calle 123", "123456789", TipoUsuario.CLIENTE);
        Resource.tokens.put(usuario, 123456789L);

        // Call the method
        Response response = resource.logoutUser(usuario);

        // Verify token removal
        assertFalse(Resource.tokens.containsKey(usuario));

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("User logged out successfully", response.getEntity());
    }

    @Test
    public void testLogoutUserNotLoggedIn() {
        // Prepare test data
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password",
                "Calle 123", "123456789", TipoUsuario.CLIENTE);

        // Call the method
        Response response = resource.logoutUser(usuario);

        // Verify response
        assertEquals(Response.Status.UNAUTHORIZED.getStatusCode(), response.getStatus());
        assertEquals("User not logged in", response.getEntity());
    }

    @Test
    public void testEliminarCuentaSuccess() {
        // Prepare test data
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password",
                "Calle 123", "123456789", TipoUsuario.CLIENTE);
        when(persistenceManager.getObjectById(Usuario.class, "12345678A")).thenReturn(usuario);
        when(transaction.isActive()).thenReturn(true).thenReturn(false);
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
        when(transaction.isActive()).thenReturn(true).thenReturn(true);
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
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password",
                "Calle 123", "123456789", TipoUsuario.CLIENTE);
        @SuppressWarnings("unchecked")
        Query<Usuario> query = mock(Query.class);
        when(persistenceManager.newQuery(Usuario.class, "nombreUsuario == :nombreUsuario")).thenReturn(query);
        doNothing().when(query).setUnique(true);
        when(query.execute("juanperez")).thenReturn(usuario);
        when(transaction.isActive()).thenReturn(true).thenReturn(false);
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
        when(transaction.isActive()).thenReturn(true).thenReturn(true);
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
    public void testGetUsuarioSuccess() {
        // Prepare test data
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password",
                "Calle 123", "123456789", TipoUsuario.CLIENTE);
        @SuppressWarnings("unchecked")
        Query<Usuario> query = mock(Query.class);
        when(persistenceManager.newQuery(Usuario.class, "nombreUsuario == :nombreUsuario")).thenReturn(query);
        doNothing().when(query).setUnique(true);
        when(query.execute("juanperez")).thenReturn(usuario);
        when(transaction.isActive()).thenReturn(true).thenReturn(false);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.getUsuario("juanperez");

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(usuario, response.getEntity());
    }

    @Test
    public void testGetUsuarioNotFound() {
        // Prepare test data
        @SuppressWarnings("unchecked")
        Query<Usuario> query = mock(Query.class);
        when(persistenceManager.newQuery(Usuario.class, "nombreUsuario == :nombreUsuario")).thenReturn(query);
        doNothing().when(query).setUnique(true);
        when(query.execute("juanperez")).thenReturn(null);
        when(transaction.isActive()).thenReturn(true).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.getUsuario("juanperez");

        // Verify response
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Usuario no encontrado", response.getEntity());
    }

    @Test
    public void testGetPeliculasSuccess() {
        // Prepare test data
        List<Pelicula> peliculas = new ArrayList<>();
        Pelicula pelicula = new Pelicula("Test Movie", "Drama", 120, null, "Test Director", "Test Synopsis", "18:00",
                null);
        peliculas.add(pelicula);
        @SuppressWarnings("unchecked")
        Query<Pelicula> query = mock(Query.class);
        when(persistenceManager.newQuery(Pelicula.class)).thenReturn(query);
        when(query.execute()).thenReturn(peliculas).thenReturn(peliculas); // For inicializarPeliculas and getPeliculas
        when(transaction.isActive()).thenReturn(true).thenReturn(false).thenReturn(true).thenReturn(false);
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
    public void testGetPeliculasNotFoundAndInicializar() throws Exception {
        // Prepare test data for inicializarPeliculas
        List<Pelicula> emptyPeliculas = new ArrayList<>();
        @SuppressWarnings("unchecked")
        Query<Pelicula> queryPelicula = mock(Query.class);
        when(persistenceManager.newQuery(Pelicula.class)).thenReturn(queryPelicula);
        doNothing().when(queryPelicula).setRange(0, 1);
        when(queryPelicula.execute()).thenReturn(emptyPeliculas); // No movies initially

        // Mock the creation of salas and peliculas in inicializarPeliculas
        Sala sala1 = new Sala(1, 1, 80, new ArrayList<>(), true);
        Sala sala2 = new Sala(2, 2, 90, new ArrayList<>(), true);
        Sala sala3 = new Sala(3, 3, 100, new ArrayList<>(), true);
        Sala sala4 = new Sala(4, 4, 110, new ArrayList<>(), true);
        Sala sala5 = new Sala(5, 5, 120, new ArrayList<>(), true);
        List<Pelicula> peliculasIniciales = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        peliculasIniciales.add(new Pelicula("El Padrino", "Drama", 175, dateFormat.parse("1972-03-24"),
                "Francis Ford Coppola", "Sinopsis", "18:00, 21:00", sala1));
        peliculasIniciales.add(new Pelicula("Titanic", "Romance", 195, dateFormat.parse("1997-12-19"), "James Cameron",
                "Sinopsis", "17:30, 20:30", sala2));
        peliculasIniciales.add(new Pelicula("El Señor de los Anillos: La Comunidad del Anillo", "Fantasía", 178,
                dateFormat.parse("2001-12-19"), "Peter Jackson", "Sinopsis", "16:00, 19:30", sala3));
        peliculasIniciales.add(new Pelicula("Pulp Fiction", "Thriller", 154, dateFormat.parse("1994-10-14"),
                "Quentin Tarantino", "Sinopsis", "19:00, 22:00", sala4));
        peliculasIniciales.add(new Pelicula("El Caballero Oscuro", "Acción", 152, dateFormat.parse("2008-07-18"),
                "Christopher Nolan", "Sinopsis", "17:00, 20:00", sala5));
        peliculasIniciales.add(new Pelicula("Forrest Gump", "Drama", 142, dateFormat.parse("1994-07-06"),
                "Robert Zemeckis", "Sinopsis", "16:30, 19:30", sala1));
        peliculasIniciales.add(new Pelicula("Star Wars: Episodio IV - Una nueva esperanza", "Ciencia ficción", 121,
                dateFormat.parse("1977-05-25"), "George Lucas", "Sinopsis", "18:30, 21:30", sala2));
        peliculasIniciales.add(new Pelicula("Jurassic Park", "Aventura", 127, dateFormat.parse("1993-06-11"),
                "Steven Spielberg", "Sinopsis", "17:00, 20:00", sala3));

        // Mock getPeliculas after inicializarPeliculas
        when(queryPelicula.execute()).thenReturn(emptyPeliculas).thenReturn(peliculasIniciales);
        when(transaction.isActive()).thenReturn(true).thenReturn(false).thenReturn(true).thenReturn(false);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.getPeliculas();

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(peliculasIniciales, response.getEntity());
    }

    @Test
    public void testCrearPeliculaSuccess() {
        // Prepare test data
        Sala sala = new Sala(1, 1, 50, new ArrayList<>(), true);
        Pelicula pelicula = new Pelicula("New Movie", "Action", 110, null, "New Director", "New Synopsis", "20:00",
                sala);
        @SuppressWarnings("unchecked")
        Query<Pelicula> query = mock(Query.class);
        when(persistenceManager.newQuery(Pelicula.class, "titulo == :titulo")).thenReturn(query);
        doNothing().when(query).setUnique(true);
        when(query.execute("New Movie")).thenReturn(null); // Movie does not exist
        when(persistenceManager.getObjectById(Sala.class, 1)).thenReturn(sala);
        when(transaction.isActive()).thenReturn(true).thenReturn(false);
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
        Pelicula pelicula = new Pelicula("New Movie", "Action", 110, null, "New Director", "New Synopsis", "20:00",
                sala);
        @SuppressWarnings("unchecked")
        Query<Pelicula> query = mock(Query.class);
        when(persistenceManager.newQuery(Pelicula.class, "titulo == :titulo")).thenReturn(query);
        doNothing().when(query).setUnique(true);
        when(query.execute("New Movie")).thenReturn(pelicula); // Movie already exists
        when(transaction.isActive()).thenReturn(true).thenReturn(true);
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

    // @Test
    // public void testCrearPeliculaInvalidSala() {
    // // Prepare test data
    // Sala sala = new Sala(1, 1, 50, new ArrayList<>(), true);
    // Pelicula pelicula = new Pelicula("New Movie", "Action", 110, null, "New
    // Director", "New Synopsis", "20:00",
    // sala);
    // @SuppressWarnings("unchecked")
    // Query<Pelicula> query = mock(Query.class);
    // when(persistenceManager.newQuery(Pelicula.class, "titulo ==
    // :titulo")).thenReturn(query);
    // doNothing().when(query).setUnique(true);
    // when(query.execute("New Movie")).thenReturn(null); // Movie does not exist
    // when(persistenceManager.getObjectById(Sala.class, 1)).thenThrow(new
    // JDOObjectNotFoundException());
    // when(transaction.isActive()).thenReturn(true).thenReturn(true);
    // doNothing().when(transaction).begin();
    // doNothing().when(transaction).rollback();
    // doNothing().when(persistenceManager).close();

    // // Call the method
    // Response response = resource.crearPelicula(pelicula);

    // // Verify no persistence
    // verify(persistenceManager, never()).makePersistent(any(Pelicula.class));

    // // Verify response
    // assertEquals(Response.Status.NOT_FOUND.getStatusCode(),
    // response.getStatus());
    // assertEquals("No se encontró la sala seleccionada", response.getEntity());
    // }

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
        when(transaction.isActive()).thenReturn(true).thenReturn(false);
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
        when(transaction.isActive()).thenReturn(true).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.getSalas();

        // Verify response
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("No se encontraron salas", response.getEntity());
    }

    @Test
    public void testGetResenyasSuccess() {
        // Prepare test data
        List<Resenya> resenyas = new ArrayList<>();
        Pelicula pelicula = new Pelicula("Test Movie", "Drama", 120, null, "Test Director", "Test Synopsis", "18:00",
                null);
        pelicula.setId(1);
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password",
                "Calle 123", "123456789", TipoUsuario.CLIENTE);
        Resenya resenya = new Resenya("Great movie!", 5, usuario, pelicula);
        resenyas.add(resenya);
        @SuppressWarnings("unchecked")
        Query<Resenya> query = mock(Query.class);
        when(persistenceManager.newQuery(Resenya.class, "pelicula.id == :peliculaId")).thenReturn(query);
        when(query.execute(1L)).thenReturn(resenyas);
        when(transaction.isActive()).thenReturn(true).thenReturn(false);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.getResenyas(1L);

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(resenyas, response.getEntity());
    }

    @Test
    public void testGetResenyasNotFound() {
        // Prepare test data
        List<Resenya> resenyas = new ArrayList<>();
        @SuppressWarnings("unchecked")
        Query<Resenya> query = mock(Query.class);
        when(persistenceManager.newQuery(Resenya.class, "pelicula.id == :peliculaId")).thenReturn(query);
        when(query.execute(1L)).thenReturn(resenyas);
        when(transaction.isActive()).thenReturn(true).thenReturn(false);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.getResenyas(1L);

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(resenyas, response.getEntity());
    }

    @Test
    public void testGetAllResenyasSuccess() {
        // Prepare test data
        List<Resenya> resenyas = new ArrayList<>();
        Pelicula pelicula = new Pelicula("Test Movie", "Drama", 120, null, "Test Director", "Test Synopsis", "18:00",
                null);
        pelicula.setId(1);
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password",
                "Calle 123", "123456789", TipoUsuario.CLIENTE);
        Resenya resenya = new Resenya("Great movie!", 5, usuario, pelicula);
        resenyas.add(resenya);
        @SuppressWarnings("unchecked")
        Query<Resenya> query = mock(Query.class);
        when(persistenceManager.newQuery(Resenya.class)).thenReturn(query);
        when(query.execute()).thenReturn(resenyas);
        when(transaction.isActive()).thenReturn(true).thenReturn(false);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.getAllResenyas();

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(resenyas, response.getEntity());
    }

    @Test
    public void testGetAllResenyasNotFound() {
        // Prepare test data
        List<Resenya> resenyas = new ArrayList<>();
        @SuppressWarnings("unchecked")
        Query<Resenya> query = mock(Query.class);
        when(persistenceManager.newQuery(Resenya.class)).thenReturn(query);
        when(query.execute()).thenReturn(resenyas);
        when(transaction.isActive()).thenReturn(true).thenReturn(false);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.getAllResenyas();

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(resenyas, response.getEntity());
    }

    @Test
    public void testGetResenyaSuccess() {
        // Prepare test data
        Pelicula pelicula = new Pelicula("Test Movie", "Drama", 120, null, "Test Director", "Test Synopsis", "18:00",
                null);
        pelicula.setId(1);
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password",
                "Calle 123", "123456789", TipoUsuario.CLIENTE);
        Resenya resenya = new Resenya("Great movie!", 5, usuario, pelicula);
        resenya.setId(1);
        when(persistenceManager.getObjectById(Resenya.class, 1L)).thenReturn(resenya);
        when(transaction.isActive()).thenReturn(true).thenReturn(false);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.getResenya(1L);

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(resenya, response.getEntity());
    }

    @Test
    public void testGetResenyaNotFound() {
        // Prepare test data
        when(persistenceManager.getObjectById(Resenya.class, 1L)).thenThrow(new JDOObjectNotFoundException());
        when(transaction.isActive()).thenReturn(true).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.getResenya(1L);

        // Verify response
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Reseña no encontrada", response.getEntity());
    }

    @Test
    public void testAddResenyaSuccess() {
        // Prepare test data
        Pelicula pelicula = new Pelicula("Test Movie", "Drama", 120, null, "Test Director", "Test Synopsis", "18:00",
                null);
        pelicula.setId(1);
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password",
                "Calle 123", "123456789", TipoUsuario.CLIENTE);
        Resenya resenya = new Resenya("Great movie!", 5, usuario, pelicula);
        when(persistenceManager.getObjectById(Pelicula.class, 1L)).thenReturn(pelicula);
        @SuppressWarnings("unchecked")
        Query<Usuario> query = mock(Query.class);
        when(persistenceManager.newQuery(Usuario.class, "nombreUsuario == :nombreUsuario")).thenReturn(query);
        doNothing().when(query).setUnique(true);
        when(query.execute("juanperez")).thenReturn(usuario);
        when(transaction.isActive()).thenReturn(true).thenReturn(false);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.addResenya(resenya);

        // Verify persistence
        ArgumentCaptor<Resenya> resenyaCaptor = ArgumentCaptor.forClass(Resenya.class);
        verify(persistenceManager).makePersistent(resenyaCaptor.capture());
        assertEquals("Great movie!", resenyaCaptor.getValue().getComentario());
        assertEquals(5, resenyaCaptor.getValue().getPuntuacion());

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(resenya, response.getEntity());
    }

    @Test
    public void testAddResenyaPeliculaNotFound() {
        // Prepare test data
        Pelicula pelicula = new Pelicula("Test Movie", "Drama", 120, null, "Test Director", "Test Synopsis", "18:00",
                null);
        pelicula.setId(1);
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password",
                "Calle 123", "123456789", TipoUsuario.CLIENTE);
        Resenya resenya = new Resenya("Great movie!", 5, usuario, pelicula);
        when(persistenceManager.getObjectById(Pelicula.class, 1L)).thenThrow(new JDOObjectNotFoundException());
        when(transaction.isActive()).thenReturn(true).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.addResenya(resenya);

        // Verify no persistence
        verify(persistenceManager, never()).makePersistent(any(Resenya.class));

        // Verify response
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Película no encontrada", response.getEntity());
    }

    @Test
    public void testAddResenyaUsuarioNotFound() {
        // Prepare test data
        Pelicula pelicula = new Pelicula("Test Movie", "Drama", 120, null, "Test Director", "Test Synopsis", "18:00",
                null);
        pelicula.setId(1);
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password",
                "Calle 123", "123456789", TipoUsuario.CLIENTE);
        Resenya resenya = new Resenya("Great movie!", 5, usuario, pelicula);
        when(persistenceManager.getObjectById(Pelicula.class, 1L)).thenReturn(pelicula);
        @SuppressWarnings("unchecked")
        Query<Usuario> query = mock(Query.class);
        when(persistenceManager.newQuery(Usuario.class, "nombreUsuario == :nombreUsuario")).thenReturn(query);
        doNothing().when(query).setUnique(true);
        when(query.execute("juanperez")).thenReturn(null);
        when(transaction.isActive()).thenReturn(true).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.addResenya(resenya);

        // Verify no persistence
        verify(persistenceManager, never()).makePersistent(any(Resenya.class));

        // Verify response
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Usuario no encontrado", response.getEntity());
    }

    @Test
    public void testUpdateResenyaSuccess() {
        // Prepare test data
        Pelicula pelicula = new Pelicula("Test Movie", "Drama", 120, null, "Test Director", "Test Synopsis", "18:00",
                null);
        pelicula.setId(1);
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password",
                "Calle 123", "123456789", TipoUsuario.CLIENTE);
        Resenya resenya = new Resenya("Great movie!", 5, usuario, pelicula);
        resenya.setId(1);
        Resenya updatedResenya = new Resenya("Updated review", 4, usuario, pelicula);
        when(persistenceManager.getObjectById(Resenya.class, 1L)).thenReturn(resenya);
        when(transaction.isActive()).thenReturn(true).thenReturn(false);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.updateResenya(1L, updatedResenya);

        // Verify updated fields
        assertEquals("Updated review", resenya.getComentario());
        assertEquals(4, resenya.getPuntuacion());

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(resenya, response.getEntity());
    }

    @Test
    public void testUpdateResenyaNotFound() {
        // Prepare test data
        Pelicula pelicula = new Pelicula("Test Movie", "Drama", 120, null, "Test Director", "Test Synopsis", "18:00",
                null);
        pelicula.setId(1);
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password",
                "Calle 123", "123456789", TipoUsuario.CLIENTE);
        Resenya updatedResenya = new Resenya("Updated review", 4, usuario, pelicula);
        when(persistenceManager.getObjectById(Resenya.class, 1L)).thenThrow(new JDOObjectNotFoundException());
        when(transaction.isActive()).thenReturn(true).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.updateResenya(1L, updatedResenya);

        // Verify response
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Reseña no encontrada", response.getEntity());
    }

    @Test
    public void testDeleteResenyaSuccess() {
        // Prepare test data
        Pelicula pelicula = new Pelicula("Test Movie", "Drama", 120, null, "Test Director", "Test Synopsis", "18:00",
                null);
        pelicula.setId(1);
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password",
                "Calle 123", "123456789", TipoUsuario.CLIENTE);
        Resenya resenya = new Resenya("Great movie!", 5, usuario, pelicula);
        resenya.setId(1);
        when(persistenceManager.getObjectById(Resenya.class, 1L)).thenReturn(resenya);
        when(transaction.isActive()).thenReturn(true).thenReturn(false);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.deleteResenya(1L);

        // Verify deletion
        verify(persistenceManager).deletePersistent(resenya);

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testDeleteResenyaNotFound() {
        // Prepare test data
        when(persistenceManager.getObjectById(Resenya.class, 1L)).thenThrow(new JDOObjectNotFoundException());
        when(transaction.isActive()).thenReturn(true).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.deleteResenya(1L);

        // Verify no deletion
        verify(persistenceManager, never()).deletePersistent(any());

        // Verify response
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Reseña no encontrada", response.getEntity());
    }

    @Test
    public void testGetPeliculaSuccess() {
        // Prepare test data
        Pelicula pelicula = new Pelicula("Test Movie", "Drama", 120, null, "Test Director", "Test Synopsis", "18:00",
                null);
        pelicula.setId(1);
        when(persistenceManager.getObjectById(Pelicula.class, 1L)).thenReturn(pelicula);
        when(persistenceManager.detachCopy(pelicula)).thenReturn(pelicula);
        when(transaction.isActive()).thenReturn(true).thenReturn(false);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.getPelicula(1L);

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(pelicula, response.getEntity());
    }

    @Test
    public void testGetPeliculaNotFound() {
        // Prepare test data
        when(persistenceManager.getObjectById(Pelicula.class, 1L)).thenThrow(new JDOObjectNotFoundException());
        when(transaction.isActive()).thenReturn(true).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.getPelicula(1L);

        // Verify response
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Película no encontrada", response.getEntity());
    }

    @Test
    public void testEliminarPeliculaSuccess() {
        // Prepare test data
        Pelicula pelicula = new Pelicula("Test Movie", "Drama", 120, null, "Test Director", "Test Synopsis", "18:00",
                null);
        pelicula.setId(1);
        when(persistenceManager.getObjectById(Pelicula.class, "1")).thenReturn(pelicula);
        when(transaction.isActive()).thenReturn(true).thenReturn(false);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.eliminarpelicula("1");

        // Verify deletion
        verify(persistenceManager).deletePersistent(pelicula);

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testEliminarPeliculaNotFound() {
        // Prepare test data
        when(persistenceManager.getObjectById(Pelicula.class, "1")).thenThrow(new JDOObjectNotFoundException());
        when(transaction.isActive()).thenReturn(true).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.eliminarpelicula("1");

        // Verify no deletion
        verify(persistenceManager, never()).deletePersistent(any());

        // Verify response
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Film not found", response.getEntity());
    }

    @Test
    public void testUpdatePeliculaSuccess() {
        // Prepare test data
        Sala sala = new Sala(1, 1, 50, new ArrayList<>(), true);
        Pelicula pelicula = new Pelicula("Test Movie", "Drama", 120, null, "Test Director", "Test Synopsis", "18:00",
                sala);
        pelicula.setId(1);
        Pelicula updatedPelicula = new Pelicula("Updated Movie", "Action", 110, null, "New Director", "New Synopsis",
                "20:00", sala);
        when(persistenceManager.getObjectById(Pelicula.class, 1L)).thenReturn(pelicula);
        when(transaction.isActive()).thenReturn(true).thenReturn(false);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.updatePelicula(1L, updatedPelicula);

        // Verify updated fields
        assertEquals("Updated Movie", pelicula.getTitulo());
        assertEquals("Action", pelicula.getGenero());
        assertEquals(110, pelicula.getDuracion());
        assertEquals("New Director", pelicula.getDirector());
        assertEquals("New Synopsis", pelicula.getSinopsis());
        assertEquals("20:00", pelicula.getHorario());
        assertEquals(sala, pelicula.getSala());

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(pelicula, response.getEntity());
    }

    @Test
    public void testUpdatePeliculaNotFound() {
        // Prepare test data
        Sala sala = new Sala(1, 1, 50, new ArrayList<>(), true);
        Pelicula updatedPelicula = new Pelicula("Updated Movie", "Action", 110, null, "New Director", "New Synopsis",
                "20:00", sala);
        when(persistenceManager.getObjectById(Pelicula.class, 1L)).thenThrow(new JDOObjectNotFoundException());
        when(transaction.isActive()).thenReturn(true).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.updatePelicula(1L, updatedPelicula);

        // Verify response
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Reseña no encontrada", response.getEntity());
    }

    @Test
    public void testGetSalaSuccess() {
        // Prepare test data
        Sala sala = new Sala(1, 1, 50, new ArrayList<>(), true);
        sala.setId(1);
        when(persistenceManager.getObjectById(Sala.class, 1L)).thenReturn(sala);
        when(persistenceManager.detachCopy(sala)).thenReturn(sala);
        when(transaction.isActive()).thenReturn(true).thenReturn(false);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.getSala(1L);

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(sala, response.getEntity());
    }

    @Test
    public void testGetSalaNotFound() {
        // Prepare test data
        when(persistenceManager.getObjectById(Sala.class, 1L)).thenThrow(new JDOObjectNotFoundException());
        when(transaction.isActive()).thenReturn(true).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.getSala(1L);

        // Verify response
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Sala no encontrada", response.getEntity());
    }

    @Test
    public void testGetAsientosSuccess() {
        // Prepare test data
        List<Asiento> asientos = new ArrayList<>();
        Asiento asiento = new Asiento(1, 1, TipoAsiento.NORMAL, false);
        asientos.add(asiento);
        Sala sala = new Sala(1, 1, 50, asientos, true);
        sala.setId(1);
        when(persistenceManager.getObjectById(Sala.class, 1L)).thenReturn(sala);
        when(transaction.isActive()).thenReturn(true).thenReturn(false);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.getAsientos(1L);

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(asientos, response.getEntity());
    }

    @Test
    public void testGetAsientosNotFound() {
        // Prepare test data
        when(persistenceManager.getObjectById(Sala.class, 1L)).thenThrow(new JDOObjectNotFoundException());
        when(transaction.isActive()).thenReturn(true).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.getAsientos(1L);

        // Verify response
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Sala no encontrada", response.getEntity());
    }

    @Test
    public void testGetCineSuccess() {
        // Prepare test data
        Cine cine = new Cine(1, "CineGP", "Av. Universidad 123", new ArrayList<>());
        cine.setId(1);
        when(persistenceManager.getObjectById(Cine.class, 1L)).thenReturn(cine);
        when(transaction.isActive()).thenReturn(true).thenReturn(false);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.getCine(1L);

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(cine, response.getEntity());
    }

    @Test
    public void testGetCineNotFound() {
        // Prepare test data
        when(persistenceManager.getObjectById(Cine.class, 1L)).thenThrow(new JDOObjectNotFoundException());
        when(transaction.isActive()).thenReturn(true).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.getCine(1L);

        // Verify response
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Cine no encontrado", response.getEntity());
    }

    @Test
    public void testGetCinesSuccess() {
        // Prepare test data
        List<Cine> cines = new ArrayList<>();
        Cine cine = new Cine(1, "CineGP", "Av. Universidad 123", new ArrayList<>());
        cine.setId(1);
        cines.add(cine);
        @SuppressWarnings("unchecked")
        Query<Cine> query = mock(Query.class);
        when(persistenceManager.newQuery(Cine.class)).thenReturn(query);
        when(query.execute()).thenReturn(cines);
        when(transaction.isActive()).thenReturn(true).thenReturn(false);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.getCines();

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(cines, response.getEntity());
    }

    @Test
    public void testGetCinesNotFoundAndCreateDefault() {
        // Prepare test data
        List<Cine> cines = new ArrayList<>();
        List<Sala> salas = new ArrayList<>();
        Sala sala = new Sala(1, 1, 50, new ArrayList<>(), true);
        salas.add(sala);
        @SuppressWarnings("unchecked")
        Query<Cine> queryCine = mock(Query.class);
        @SuppressWarnings("unchecked")
        Query<Sala> querySala = mock(Query.class);
        when(persistenceManager.newQuery(Cine.class)).thenReturn(queryCine);
        when(persistenceManager.newQuery(Sala.class)).thenReturn(querySala);
        when(queryCine.execute()).thenReturn(cines); // No cines initially
        when(querySala.execute()).thenReturn(salas);
        when(transaction.isActive()).thenReturn(true).thenReturn(false);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.getCines();

        // Verify persistence of default cine
        ArgumentCaptor<Cine> cineCaptor = ArgumentCaptor.forClass(Cine.class);
        verify(persistenceManager).makePersistent(cineCaptor.capture());
        assertEquals("CineGP", cineCaptor.getValue().getNombre());
        assertEquals("Av. Universidad 123", cineCaptor.getValue().getDireccion());

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        List<Cine> responseCines = (List<Cine>) response.getEntity();
        assertEquals(1, responseCines.size());
        assertEquals("CineGP", responseCines.get(0).getNombre());
    }

    @Test
    public void testGetEntradasSuccess() {
        // Prepare test data
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password",
                "Calle 123", "123456789", TipoUsuario.CLIENTE);
        List<Entrada> entradas = new ArrayList<>();
        Cine cine = new Cine(1, "CineGP", "Av. Universidad 123", new ArrayList<>());
        Entrada entrada = new Entrada(usuario, cine, 10, 5, TipoAsiento.NORMAL);
        entradas.add(entrada);
        @SuppressWarnings("unchecked")
        Query<Usuario> queryUsuario = mock(Query.class);
        @SuppressWarnings("unchecked")
        Query<Entrada> queryEntrada = mock(Query.class);
        when(persistenceManager.newQuery(Usuario.class, "nombreUsuario == :nombreUsuario")).thenReturn(queryUsuario);
        doNothing().when(queryUsuario).setUnique(true);
        when(queryUsuario.execute("juanperez")).thenReturn(usuario);
        when(persistenceManager.newQuery(Entrada.class, "usuario.nombreUsuario == :nombreUsuario"))
                .thenReturn(queryEntrada);
        when(queryEntrada.execute("juanperez")).thenReturn(entradas);
        when(transaction.isActive()).thenReturn(true).thenReturn(false);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.getEntradas("juanperez");

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals(entradas, response.getEntity());
    }

    @Test
    public void testGetEntradasUsuarioNotFound() {
        // Prepare test data
        @SuppressWarnings("unchecked")
        Query<Usuario> queryUsuario = mock(Query.class);
        when(persistenceManager.newQuery(Usuario.class, "nombreUsuario == :nombreUsuario")).thenReturn(queryUsuario);
        doNothing().when(queryUsuario).setUnique(true);
        when(queryUsuario.execute("juanperez")).thenReturn(null);
        when(transaction.isActive()).thenReturn(true).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.getEntradas("juanperez");

        // Verify response
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Usuario no encontrado", response.getEntity());
    }

    @Test
    public void testComprarEntradasSuccess() {
        // Prepare test data
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password",
                "Calle 123", "123456789", TipoUsuario.CLIENTE);
        Cine cine = new Cine(1, "CineGP", "Av. Universidad 123", new ArrayList<>());
        cine.setId(1);
        Sala sala = new Sala(1, 1, 50, new ArrayList<>(), true);
        sala.setId(1);
        Asiento asiento = new Asiento(1, 1, TipoAsiento.NORMAL, false);
        sala.getAsientos().add(asiento);
        Pelicula pelicula = new Pelicula("Test Movie", "Drama", 120, null, "Test Director", "Test Synopsis", "18:00",
                sala);
        pelicula.setId(1);
        @SuppressWarnings("unchecked")
        Query<Usuario> queryUsuario = mock(Query.class);
        when(persistenceManager.newQuery(Usuario.class, "nombreUsuario == :nombreUsuario")).thenReturn(queryUsuario);
        doNothing().when(queryUsuario).setUnique(true);
        when(queryUsuario.execute("juanperez")).thenReturn(usuario);
        when(persistenceManager.getObjectById(Cine.class, 1L)).thenReturn(cine);
        when(persistenceManager.getObjectById(Pelicula.class, 1L)).thenReturn(pelicula);
        when(transaction.isActive()).thenReturn(true).thenReturn(false);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();
        doNothing().when(persistenceManager).close();

        // Prepare DTO
        Resource.CompraEntradaDTO compraDTO = new Resource.CompraEntradaDTO();
        compraDTO.setNombreUsuario("juanperez");
        compraDTO.setCineId(1L);
        compraDTO.setPeliculaId(1L);
        compraDTO.setHorario("18:00");
        Resource.AsientoDTO asientoDTO = new Resource.AsientoDTO();
        asientoDTO.setNumero(1);
        asientoDTO.setTipo(TipoAsiento.NORMAL);
        asientoDTO.setPrecio(10);
        compraDTO.setAsientos(Arrays.asList(asientoDTO));
        compraDTO.setMetodoPago("Tarjeta");
        compraDTO.setPrecioTotal(10);

        // Call the method
        Response response = resource.comprarEntradas(compraDTO);

        // Verify persistence
        ArgumentCaptor<Entrada> entradaCaptor = ArgumentCaptor.forClass(Entrada.class);
        verify(persistenceManager).makePersistent(entradaCaptor.capture());
        assertEquals(1, entradaCaptor.getValue().getAsiento());
        assertEquals(10, entradaCaptor.getValue().getPrecio());
        assertTrue(asiento.isOcupado());

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = (Map<String, Object>) response.getEntity();
        assertEquals("success", responseMap.get("status"));
        assertEquals(1, responseMap.get("totalEntradas"));
        assertEquals(10, responseMap.get("precioTotal"));
    }

    @Test
    public void testComprarEntradasInvalidData() {
        // Prepare test data
        Resource.CompraEntradaDTO compraDTO = new Resource.CompraEntradaDTO();
        compraDTO.setNombreUsuario(null);
        compraDTO.setAsientos(new ArrayList<>());
        when(transaction.isActive()).thenReturn(true).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.comprarEntradas(compraDTO);

        // Verify no persistence
        verify(persistenceManager, never()).makePersistent(any());

        // Verify response
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("Datos de compra incompletos", response.getEntity());
    }

    @Test
    public void testCancelarEntradaSuccess() {
        // Prepare test data
        List<Sala> salas = new ArrayList<>();
        Sala sala = new Sala(1, 1, 50, new ArrayList<>(), true);
        sala.setId(1);
        Asiento asiento = new Asiento(1, 1, TipoAsiento.NORMAL, true);
        sala.getAsientos().add(asiento);
        salas.add(sala);
        Cine cine = new Cine(1, "CineGP", "Av. Universidad 123", salas);
        cine.setId(1);
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password",
                "Calle 123", "123456789", TipoUsuario.CLIENTE);
        Entrada entrada = new Entrada(usuario, cine, 10, 1, TipoAsiento.NORMAL);
        entrada.setId(1);
        when(persistenceManager.getObjectById(Entrada.class, 1L)).thenReturn(entrada);
        when(transaction.isActive()).thenReturn(true).thenReturn(false);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.cancelarEntrada(1L);

        // Verify deletion
        verify(persistenceManager).deletePersistent(entrada);
        assertFalse(asiento.isOcupado());

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        @SuppressWarnings("unchecked")
        Map<String, Object> responseMap = (Map<String, Object>) response.getEntity();
        assertEquals("success", responseMap.get("status"));
    }

    @Test
    public void testCancelarEntradaNotFound() {
        // Prepare test data
        when(persistenceManager.getObjectById(Entrada.class, 1L)).thenThrow(new JDOObjectNotFoundException());
        when(transaction.isActive()).thenReturn(true).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.cancelarEntrada(1L);

        // Verify no deletion
        verify(persistenceManager, never()).deletePersistent(any());

        // Verify response
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Entrada no encontrada", response.getEntity());
    }

    @Test
    public void testCambiarEstadoSalaSuccess() {
        // Prepare test data
        Sala sala = new Sala(1, 1, 50, new ArrayList<>(), true);
        sala.setId(1);
        when(persistenceManager.getObjectById(Sala.class, 1L)).thenReturn(sala);
        when(transaction.isActive()).thenReturn(true).thenReturn(false);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).commit();
        doNothing().when(persistenceManager).close();

        // Prepare data
        Map<String, Boolean> estado = new HashMap<>();
        estado.put("disponible", false);

        // Call the method
        Response response = resource.cambiarEstadoSala(1L, estado);

        // Verify updated field
        assertFalse(sala.isDisponible());

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testCambiarEstadoSalaNotFound() {
        // Prepare test data
        when(persistenceManager.getObjectById(Sala.class, 1L)).thenThrow(new JDOObjectNotFoundException());
        doNothing().when(persistenceManager).close();

        // Prepare data
        Map<String, Boolean> estado = new HashMap<>();
        estado.put("disponible", false);

        // Call the method
        Response response = resource.cambiarEstadoSala(1L, estado);

        // Verify response
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Error al cambiar el estado de la sala", response.getEntity());
    }

    @Test
    public void testActualizarAsientosSuccess() {
        // Prepare test data
        List<Asiento> asientos = new ArrayList<>();
        Asiento asiento = new Asiento(1, 1, TipoAsiento.NORMAL, false);
        asientos.add(asiento);
        Sala sala = new Sala(1, 1, 50, new ArrayList<>(), true);
        sala.setId(1);
        when(persistenceManager.getObjectById(Sala.class, 1L)).thenReturn(sala);
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.actualizarAsientos(1L, asientos);

        // Verify updated field
        assertEquals(asientos, sala.getAsientos());

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
    }

    @Test
    public void testActualizarAsientosNotFound() {
        // Prepare test data
        List<Asiento> asientos = new ArrayList<>();
        when(persistenceManager.getObjectById(Sala.class, 1L)).thenReturn(null);
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.actualizarAsientos(1L, asientos);

        // Verify response
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Sala no encontrada", response.getEntity());
    }

    @Test
    public void testAgregarSalaSuccess() {
        // Prepare test data
        Sala sala = new Sala(1, 1, 50, null, true);
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.agregarSala(sala);

        // Verify persistence
        ArgumentCaptor<Sala> salaCaptor = ArgumentCaptor.forClass(Sala.class);
        verify(persistenceManager).makePersistent(salaCaptor.capture());
        assertEquals(50, salaCaptor.getValue().getAsientos().size());
        assertEquals(TipoAsiento.VIP, salaCaptor.getValue().getAsientos().get(9).getTipo()); // 10th seat should be VIP

        // Verify response
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        assertEquals("Sala y asientos creados correctamente", response.getEntity());
    }

    @Test
    public void testActualizarSalaIncreaseCapacitySuccess() {
        // Prepare test data
        List<Asiento> asientos = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            asientos.add(new Asiento(i, i, TipoAsiento.NORMAL, false));
        }
        Sala sala = new Sala(1, 1, 50, asientos, true);
        sala.setId(1);
        when(persistenceManager.getObjectById(Sala.class, 1L)).thenReturn(sala);
        doNothing().when(persistenceManager).close();

        // Prepare data
        Map<String, Object> datos = new HashMap<>();
        datos.put("capacidad", 60);

        // Call the method
        Response response = resource.actualizarSala(1L, datos);

        // Verify updated field
        assertEquals(60, sala.getCapacidad());
        assertEquals(60, sala.getAsientos().size());
        assertEquals(TipoAsiento.VIP, sala.getAsientos().get(59).getTipo()); // 60th seat should be VIP

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("Sala actualizada correctamente", response.getEntity());
    }

    @Test
    public void testActualizarSalaDecreaseCapacitySuccess() {
        // Prepare test data
        List<Asiento> asientos = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            asientos.add(new Asiento(i, i, TipoAsiento.NORMAL, false));
        }
        Sala sala = new Sala(1, 1, 50, asientos, true);
        sala.setId(1);
        when(persistenceManager.getObjectById(Sala.class, 1L)).thenReturn(sala);
        doNothing().when(persistenceManager).close();

        // Prepare data
        Map<String, Object> datos = new HashMap<>();
        datos.put("capacidad", 40);

        // Call the method
        Response response = resource.actualizarSala(1L, datos);

        // Verify updated field
        assertEquals(40, sala.getCapacidad());
        assertEquals(40, sala.getAsientos().size());

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("Sala actualizada correctamente", response.getEntity());
    }

    @Test
    public void testActualizarSalaDecreaseCapacityOccupiedSeats() {
        // Prepare test data
        Sala sala = new Sala(1L, 1, 50, new ArrayList<>(), true);
        sala.getAsientos().add(new Asiento(1L, 1, TipoAsiento.NORMAL, true)); // Asiento ocupado
        when(persistenceManager.getObjectById(Sala.class, 1L)).thenReturn(sala);
        doNothing().when(persistenceManager).close();

        // Prepare data
        Map<String, Object> datos = new HashMap<>();
        datos.put("capacidad", 0); // Intento reducir capacidad

        // Call the method
        Response response = resource.actualizarSala(1L, datos);

        // Verify response
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("No se pueden reducir los asientos porque algunos están ocupados", response.getEntity());
    }

    @Test
    public void testActualizarSalaNotFound() {
        // Prepare test data
        when(persistenceManager.getObjectById(Sala.class, 1L)).thenThrow(new JDOObjectNotFoundException());
        doNothing().when(persistenceManager).close();

        // Prepare data
        Map<String, Object> datos = new HashMap<>();
        datos.put("capacidad", 50);

        // Call the method
        Response response = resource.actualizarSala(1L, datos);

        // Verify response
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Error al actualizar la sala", response.getEntity());
    }

    @Test
    public void testActualizarSalaInvalidData() {
        // Prepare test data
        Sala sala = new Sala(1L, 1, 50, new ArrayList<>(), true);
        sala.getAsientos().add(new Asiento(1L, 1, TipoAsiento.NORMAL, true)); // Asiento ocupado
        when(persistenceManager.getObjectById(Sala.class, 1L)).thenReturn(sala);
        doNothing().when(persistenceManager).close();

        // Prepare data with invalid capacity
        Map<String, Object> datos = new HashMap<>();
        datos.put("capacidad", -1);

        // Call the method
        Response response = resource.actualizarSala(1L, datos);

        // Verify response
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        assertEquals("No se pueden reducir los asientos porque algunos están ocupados", response.getEntity());
    }

    @Test
    public void testAddResenyaNullInput() {
        // No configuramos el mock porque el método falla antes
        doNothing().when(persistenceManager).close();

        // Call the method with null input
        Response response = resource.addResenya(null);

        // Verify response
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Error al crear reseña", response.getEntity());
    }

    @Test
    public void testUpdateResenyaNullInput() {
        // No configuramos el mock porque el método falla antes
        doNothing().when(persistenceManager).close();

        // Call the method with null input
        Response response = resource.updateResenya(1L, null);

        // Verify response
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Error al actualizar reseña", response.getEntity());
    }

    @Test
    public void testUpdatePeliculaNullInput() {
        // No configuramos el mock porque el método falla antes
        doNothing().when(persistenceManager).close();

        // Call the method with null input
        Response response = resource.updatePelicula(1L, null);

        // Verify response
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Error al actualizar reseña", response.getEntity());
    }

    @Test
    public void testCambiarEstadoSalaInvalidData() {
        // Prepare test data
        when(persistenceManager.getObjectById(Sala.class, 1L)).thenReturn(new Sala(1L, 1, 50, new ArrayList<>(), true));
        doNothing().when(persistenceManager).close();

        // Prepare invalid data (null map)
        Map<String, Boolean> estado = null;

        // Call the method
        Response response = resource.cambiarEstadoSala(1L, estado);

        // Verify response
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Error al cambiar el estado de la sala", response.getEntity());
    }
    
    @Test
    public void testActualizarAsientosNullInput() {
        // Prepare test data
        Sala sala = new Sala(1L, 1, 50, new ArrayList<>(), true);
        when(persistenceManager.getObjectById(Sala.class, 1L)).thenReturn(sala);
        doNothing().when(persistenceManager).close();

        // Call the method with null input
        Response response = resource.actualizarAsientos(1L, null);

        // Verify response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertNull(response.getEntity());
    }

    @Test
    public void testComprarEntradasPeliculaNotFound() {
        // Prepare test data
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password",
                "Calle 123", "123456789", TipoUsuario.CLIENTE);
        Cine cine = new Cine(1, "CineGP", "Av. Universidad 123", new ArrayList<>());
        cine.setId(1);
        @SuppressWarnings("unchecked")
        Query<Usuario> queryUsuario = mock(Query.class);
        when(persistenceManager.newQuery(Usuario.class, "nombreUsuario == :nombreUsuario")).thenReturn(queryUsuario);
        doNothing().when(queryUsuario).setUnique(true);
        when(queryUsuario.execute("juanperez")).thenReturn(usuario);
        when(persistenceManager.getObjectById(Cine.class, 1L)).thenReturn(cine);
        when(persistenceManager.getObjectById(Pelicula.class, 1L)).thenThrow(new JDOObjectNotFoundException());
        when(transaction.isActive()).thenReturn(true).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();
        doNothing().when(persistenceManager).close();

        // Prepare DTO
        Resource.CompraEntradaDTO compraDTO = new Resource.CompraEntradaDTO();
        compraDTO.setNombreUsuario("juanperez");
        compraDTO.setCineId(1L);
        compraDTO.setPeliculaId(1L);
        compraDTO.setHorario("18:00");
        Resource.AsientoDTO asientoDTO = new Resource.AsientoDTO();
        asientoDTO.setNumero(1);
        asientoDTO.setTipo(TipoAsiento.NORMAL);
        asientoDTO.setPrecio(10);
        compraDTO.setAsientos(Arrays.asList(asientoDTO));
        compraDTO.setMetodoPago("Tarjeta");
        compraDTO.setPrecioTotal(10);

        // Call the method
        Response response = resource.comprarEntradas(compraDTO);

        // Verify no persistence
        verify(persistenceManager, never()).makePersistent(any());

        // Verify response
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Película no encontrada", response.getEntity());
    }

    @Test
    public void testComprarEntradasCineNotFound() {
        // Prepare test data
        Usuario usuario = new Usuario("12345678A", "Juan", "Pérez", "juan@example.com", "juanperez", "password",
                "Calle 123", "123456789", TipoUsuario.CLIENTE);
        @SuppressWarnings("unchecked")
        Query<Usuario> queryUsuario = mock(Query.class);
        when(persistenceManager.newQuery(Usuario.class, "nombreUsuario == :nombreUsuario")).thenReturn(queryUsuario);
        doNothing().when(queryUsuario).setUnique(true);
        when(queryUsuario.execute("juanperez")).thenReturn(usuario);
        when(persistenceManager.getObjectById(Cine.class, 1L)).thenThrow(new JDOObjectNotFoundException());
        when(transaction.isActive()).thenReturn(true).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();
        doNothing().when(persistenceManager).close();

        // Prepare DTO
        Resource.CompraEntradaDTO compraDTO = new Resource.CompraEntradaDTO();
        compraDTO.setNombreUsuario("juanperez");
        compraDTO.setCineId(1L);
        compraDTO.setPeliculaId(1L);
        compraDTO.setHorario("18:00");
        Resource.AsientoDTO asientoDTO = new Resource.AsientoDTO();
        asientoDTO.setNumero(1);
        asientoDTO.setTipo(TipoAsiento.NORMAL);
        asientoDTO.setPrecio(10);
        compraDTO.setAsientos(Arrays.asList(asientoDTO));
        compraDTO.setMetodoPago("Tarjeta");
        compraDTO.setPrecioTotal(10);

        // Call the method
        Response response = resource.comprarEntradas(compraDTO);

        // Verify no persistence
        verify(persistenceManager, never()).makePersistent(any());

        // Verify response
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Cine no encontrado", response.getEntity());
    }

    @Test
    public void testComprarEntradasUsuarioNotFound() {
        // Prepare test data
        @SuppressWarnings("unchecked")
        Query<Usuario> queryUsuario = mock(Query.class);
        when(persistenceManager.newQuery(Usuario.class, "nombreUsuario == :nombreUsuario")).thenReturn(queryUsuario);
        doNothing().when(queryUsuario).setUnique(true);
        when(queryUsuario.execute("juanperez")).thenReturn(null);
        when(transaction.isActive()).thenReturn(true).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();
        doNothing().when(persistenceManager).close();

        // Prepare DTO
        Resource.CompraEntradaDTO compraDTO = new Resource.CompraEntradaDTO();
        compraDTO.setNombreUsuario("juanperez");
        compraDTO.setCineId(1L);
        compraDTO.setPeliculaId(1L);
        compraDTO.setHorario("18:00");
        Resource.AsientoDTO asientoDTO = new Resource.AsientoDTO();
        asientoDTO.setNumero(1);
        asientoDTO.setTipo(TipoAsiento.NORMAL);
        asientoDTO.setPrecio(10);
        compraDTO.setAsientos(Arrays.asList(asientoDTO));
        compraDTO.setMetodoPago("Tarjeta");
        compraDTO.setPrecioTotal(10);

        // Call the method
        Response response = resource.comprarEntradas(compraDTO);

        // Verify no persistence
        verify(persistenceManager, never()).makePersistent(any());

        // Verify response
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertEquals("Usuario no encontrado", response.getEntity());
    }

    @Test
    public void testGetPeliculasException() {
        // Prepare test data
        @SuppressWarnings("unchecked")
        Query<Pelicula> query = mock(Query.class);
        when(persistenceManager.newQuery(Pelicula.class)).thenReturn(query);
        when(query.execute()).thenThrow(new RuntimeException("Database error"));
        when(transaction.isActive()).thenReturn(true).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.getPeliculas();

        // Verify response
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Error al obtener películas", response.getEntity());
    }

    @Test
    public void testGetSalasException() {
        // Prepare test data
        @SuppressWarnings("unchecked")
        Query<Sala> query = mock(Query.class);
        when(persistenceManager.newQuery(Sala.class)).thenReturn(query);
        when(query.execute()).thenThrow(new RuntimeException("Database error"));
        when(transaction.isActive()).thenReturn(true).thenReturn(true);
        doNothing().when(transaction).begin();
        doNothing().when(transaction).rollback();
        doNothing().when(persistenceManager).close();

        // Call the method
        Response response = resource.getSalas();

        // Verify response
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
        assertEquals("Error al obtener salas", response.getEntity());
    }
}