package org.datanucleus;

import static org.junit.Assert.assertEquals;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import es.deusto.spq.server.jdo.Pelicula;
import es.deusto.spq.server.jdo.TipoUsuario;
import es.deusto.spq.server.jdo.Usuario;

/**
 * Tests for JDO persistence layer, verifying CRUD operations for entities.
 */
public class JDOTest {
    private static final PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");
    private static Date fecha;

    @BeforeClass
    public static void setUp() throws Exception {
        // Verify PersistenceManagerFactory
        if (pmf == null) {
            throw new IllegalStateException("PersistenceManagerFactory is null. Check datanucleus.properties.");
        }

        // Parsear la fecha
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        fecha = dateFormat.parse("2025-04-23");

        // Limpiar la base de datos
        cleanDatabase();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        // Limpiar la base de datos
        cleanDatabase();

        // Cerrar PersistenceManagerFactory
        if (pmf != null && !pmf.isClosed()) {
            pmf.close();
        }
    }

    private static void cleanDatabase() {
        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            System.out.println("Cleaning database...");
            pm.newQuery(Usuario.class).deletePersistentAll();
            pm.newQuery(Pelicula.class).deletePersistentAll();
            tx.commit();
            System.out.println("Database cleaned successfully.");
        } catch (Exception e) {
            System.err.println("Error cleaning database: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }

    @Test
    public void testCreateUsuario() {
        System.out.println("Running testCreateUsuario...");
        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            Usuario usuario = new Usuario("12345678A", "Test", "User", "test@example.com", "testuser", "password", "Calle 123", "987654321", TipoUsuario.CLIENTE);
            pm.makePersistent(usuario);
            tx.commit();
            System.out.println("Usuario created successfully.");

            // Verify
            pm = pmf.getPersistenceManager();
            tx = pm.currentTransaction();
            tx.begin();
            Query<Usuario> query = pm.newQuery(Usuario.class, "nombreUsuario == :nombreUsuario");
            Map<String, Object> params = new HashMap<>();
            params.put("nombreUsuario", "testuser");
            query.setNamedParameters(params);
            List<Usuario> results = query.executeList();
            assertEquals(1, results.size());
            assertEquals("testuser", results.get(0).getNombreUsuario());
            tx.commit();
            System.out.println("testCreateUsuario passed.");
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }

    @Test
    public void testReadUsuario() {
        System.out.println("Running testReadUsuario...");
        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try {
            // Create a user first
            tx.begin();
            Usuario usuario = new Usuario("87654321B", "Read", "User", "read@example.com", "readuser", "password", "Calle 456", "123456789", TipoUsuario.ADMINISTRADOR);
            pm.makePersistent(usuario);
            tx.commit();

            // Read the user
            pm = pmf.getPersistenceManager();
            tx = pm.currentTransaction();
            tx.begin();
            Query<Usuario> query = pm.newQuery(Usuario.class, "nombreUsuario == :nombreUsuario");
            Map<String, Object> params = new HashMap<>();
            params.put("nombreUsuario", "readuser");
            query.setNamedParameters(params);
            List<Usuario> results = query.executeList();
            assertEquals(1, results.size());
            assertEquals("readuser", results.get(0).getNombreUsuario());
            assertEquals("Read", results.get(0).getNombre());
            tx.commit();
            System.out.println("testReadUsuario passed.");
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }

    @Test
    public void testUpdateUsuario() {
        System.out.println("Running testUpdateUsuario...");
        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try {
            // Create a user
            tx.begin();
            Usuario usuario = new Usuario("11111111C", "Update", "User", "update@example.com", "updateuser", "password", "Calle 789", "111222333", TipoUsuario.CLIENTE);
            pm.makePersistent(usuario);
            tx.commit();

            // Update the user
            pm = pmf.getPersistenceManager();
            tx = pm.currentTransaction();
            tx.begin();
            Query<Usuario> query = pm.newQuery(Usuario.class, "nombreUsuario == :nombreUsuario");
            Map<String, Object> params = new HashMap<>();
            params.put("nombreUsuario", "updateuser");
            query.setNamedParameters(params);
            List<Usuario> results = query.executeList();
            assertEquals(1, results.size());
            Usuario updatedUsuario = results.get(0);
            updatedUsuario.setNombre("Updated");
            updatedUsuario.setEmail("updated@example.com");
            pm.makePersistent(updatedUsuario);
            tx.commit();

            // Verify update
            pm = pmf.getPersistenceManager();
            tx = pm.currentTransaction();
            tx.begin();
            query = pm.newQuery(Usuario.class, "nombreUsuario == :nombreUsuario");
            params = new HashMap<>();
            params.put("nombreUsuario", "updateuser");
            query.setNamedParameters(params);
            results = query.executeList();
            assertEquals(1, results.size());
            assertEquals("Updated", results.get(0).getNombre());
            assertEquals("updated@example.com", results.get(0).getEmail());
            tx.commit();
            System.out.println("testUpdateUsuario passed.");
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }

    @Test
    public void testDeleteUsuario() {
        System.out.println("Running testDeleteUsuario...");
        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try {
            // Create a user
            tx.begin();
            Usuario usuario = new Usuario("22222222D", "Delete", "User", "delete@example.com", "deleteuser", "password", "Calle 101", "444555666", TipoUsuario.CLIENTE);
            pm.makePersistent(usuario);
            tx.commit();

            // Delete the user
            pm = pmf.getPersistenceManager();
            tx = pm.currentTransaction();
            tx.begin();
            Query<Usuario> query = pm.newQuery(Usuario.class, "nombreUsuario == :nombreUsuario");
            Map<String, Object> params = new HashMap<>();
            params.put("nombreUsuario", "deleteuser");
            query.setNamedParameters(params);
            List<Usuario> results = query.executeList();
            assertEquals(1, results.size());
            pm.deletePersistent(results.get(0));
            tx.commit();

            // Verify deletion
            pm = pmf.getPersistenceManager();
            tx = pm.currentTransaction();
            tx.begin();
            query = pm.newQuery(Usuario.class, "nombreUsuario == :nombreUsuario");
            params = new HashMap<>();
            params.put("nombreUsuario", "deleteuser");
            query.setNamedParameters(params);
            results = query.executeList();
            assertEquals(0, results.size());
            tx.commit();
            System.out.println("testDeleteUsuario passed.");
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }

    @Test
    public void testCreatePelicula() {
        System.out.println("Running testCreatePelicula...");
        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            Pelicula pelicula = new Pelicula("Test Movie", "Drama", 120, fecha, "Test Director", "Test Synopsis", "18:00", null);
            pm.makePersistent(pelicula);
            tx.commit();
            System.out.println("Pelicula created successfully.");

            // Verify
            pm = pmf.getPersistenceManager();
            tx = pm.currentTransaction();
            tx.begin();
            Query<Pelicula> query = pm.newQuery(Pelicula.class, "titulo == :titulo");
            Map<String, Object> params = new HashMap<>();
            params.put("titulo", "Test Movie");
            query.setNamedParameters(params);
            List<Pelicula> results = query.executeList();
            assertEquals(1, results.size());
            assertEquals("Test Movie", results.get(0).getTitulo());
            tx.commit();
            System.out.println("testCreatePelicula passed.");
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }

    @Test
    public void testReadPelicula() {
        System.out.println("Running testReadPelicula...");
        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try {
            // Create a movie
            tx.begin();
            Pelicula pelicula = new Pelicula("Read Movie", "Action", 110, fecha, "Read Director", "Read Synopsis", "20:00", null);
            pm.makePersistent(pelicula);
            tx.commit();

            // Read the movie
            pm = pmf.getPersistenceManager();
            tx = pm.currentTransaction();
            tx.begin();
            Query<Pelicula> query = pm.newQuery(Pelicula.class, "titulo == :titulo");
            Map<String, Object> params = new HashMap<>();
            params.put("titulo", "Read Movie");
            query.setNamedParameters(params);
            List<Pelicula> results = query.executeList();
            assertEquals(1, results.size());
            assertEquals("Read Movie", results.get(0).getTitulo());
            assertEquals("Action", results.get(0).getGenero());
            tx.commit();
            System.out.println("testReadPelicula passed.");
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }

    @Test
    public void testUpdatePelicula() {
        System.out.println("Running testUpdatePelicula...");
        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try {
            // Create a movie
            tx.begin();
            Pelicula pelicula = new Pelicula("Update Movie", "Comedy", 100, fecha, "Update Director", "Update Synopsis", "22:00", null);
            pm.makePersistent(pelicula);
            tx.commit();

            // Update the movie
            pm = pmf.getPersistenceManager();
            tx = pm.currentTransaction();
            tx.begin();
            Query<Pelicula> query = pm.newQuery(Pelicula.class, "titulo == :titulo");
            Map<String, Object> params = new HashMap<>();
            params.put("titulo", "Update Movie");
            query.setNamedParameters(params);
            List<Pelicula> results = query.executeList();
            assertEquals(1, results.size());
            Pelicula updatedPelicula = results.get(0);
            updatedPelicula.setTitulo("Updated Movie");
            updatedPelicula.setGenero("Sci-Fi");
            pm.makePersistent(updatedPelicula);
            tx.commit();

            // Verify update
            pm = pmf.getPersistenceManager();
            tx = pm.currentTransaction();
            tx.begin();
            query = pm.newQuery(Pelicula.class, "titulo == :titulo");
            params = new HashMap<>();
            params.put("titulo", "Updated Movie");
            query.setNamedParameters(params);
            results = query.executeList();
            assertEquals(1, results.size());
            assertEquals("Updated Movie", results.get(0).getTitulo());
            assertEquals("Sci-Fi", results.get(0).getGenero());
            tx.commit();
            System.out.println("testUpdatePelicula passed.");
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }

    @Test
    public void testDeletePelicula() {
        System.out.println("Running testDeletePelicula...");
        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try {
            // Create a movie
            tx.begin();
            Pelicula pelicula = new Pelicula("Delete Movie", "Horror", 90, fecha, "Delete Director", "Delete Synopsis", "23:00", null);
            pm.makePersistent(pelicula);
            tx.commit();

            // Delete the movie
            pm = pmf.getPersistenceManager();
            tx = pm.currentTransaction();
            tx.begin();
            Query<Pelicula> query = pm.newQuery(Pelicula.class, "titulo == :titulo");
            Map<String, Object> params = new HashMap<>();
            params.put("titulo", "Delete Movie");
            query.setNamedParameters(params);
            List<Pelicula> results = query.executeList();
            assertEquals(1, results.size());
            pm.deletePersistent(results.get(0));
            tx.commit();

            // Verify deletion
            pm = pmf.getPersistenceManager();
            tx = pm.currentTransaction();
            tx.begin();
            query = pm.newQuery(Pelicula.class, "titulo == :titulo");
            params = new HashMap<>();
            params.put("titulo", "Delete Movie");
            query.setNamedParameters(params);
            results = query.executeList();
            assertEquals(0, results.size());
            tx.commit();
            System.out.println("testDeletePelicula passed.");
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }
}