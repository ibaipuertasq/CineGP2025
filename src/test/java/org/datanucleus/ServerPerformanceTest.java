package org.datanucleus;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status.Family;

import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.github.noconnor.junitperf.JUnitPerfRule;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.reporting.providers.HtmlReportGenerator;

import es.deusto.spq.server.Main;
import es.deusto.spq.server.jdo.Pelicula;
import es.deusto.spq.server.jdo.TipoUsuario;
import es.deusto.spq.server.jdo.Usuario;

import org.datanucleus.categories.PerformanceTest;

/**
 * Performance tests for the server, testing HTTP endpoints under load with a real database.
 */
@Category(PerformanceTest.class)
public class ServerPerformanceTest {
    private static final PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");

    private static HttpServer server;
    private WebTarget target;

    private static Date fecha;
    private static Pelicula pelicula;
    private static Usuario usuario;

    @Rule
    public JUnitPerfRule perfTestRule = new JUnitPerfRule(new HtmlReportGenerator("target/junitperf/report.html"));

    @BeforeClass
    public static void prepareTests() throws Exception {
        // Verify PersistenceManagerFactory
        if (pmf == null) {
            throw new IllegalStateException("PersistenceManagerFactory is null. Check datanucleus.properties.");
        }

        // Parsear la fecha
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        fecha = dateFormat.parse("2025-04-23");

        // Iniciar el servidor
        server = Main.startServer();

        // Preparar datos de prueba en la base de datos
        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();

            // Limpiar la base de datos primero
            System.out.println("Cleaning database...");
            pm.newQuery(Usuario.class).deletePersistentAll();
            pm.newQuery(Pelicula.class).deletePersistentAll();

            // Crear un usuario de prueba
            System.out.println("Persisting test user...");
            usuario = new Usuario("12345678A", "Test", "User", "test@example.com", "testuser", "password", "Calle 123", "987654321", TipoUsuario.CLIENTE);
            pm.makePersistent(usuario);

            // Crear una película de prueba (sin Sala)
            System.out.println("Persisting test movie...");
            pelicula = new Pelicula("Test Movie", "Drama", 120, fecha, "Test Director", "Test Synopsis", "18:00", null);
            pm.makePersistent(pelicula);

            tx.commit();
            System.out.println("Test data persisted successfully.");
        } catch (Exception e) {
            System.err.println("Error persisting test data: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            if (tx.isActive()) {
                tx.rollback();
            }
            pm.close();
        }
    }

    @Before
    public void setUp() {
        Client c = ClientBuilder.newClient();
        target = c.target(Main.BASE_URI).path("resource");
    }

    @AfterClass
    public static void tearDownServer() throws Exception {
        // Apagar el servidor
        if (server != null) {
            server.shutdown();
            System.out.println("Server shut down.");
        }

        // Limpiar la base de datos
        PersistenceManager pm = pmf.getPersistenceManager();
        Transaction tx = pm.currentTransaction();
        try {
            tx.begin();
            System.out.println("Cleaning database in tearDown...");
            pm.newQuery(Usuario.class).deletePersistentAll();
            pm.newQuery(Pelicula.class).deletePersistentAll();
            tx.commit();
            System.out.println("Database cleaned in tearDown.");
        } catch (Exception e) {
            System.err.println("Error cleaning database in tearDown: " + e.getMessage());
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
    @JUnitPerfTest(threads = 10, durationMs = 1000)
    public void testLoginUser() {
        System.out.println("Running testLoginUser...");
        Usuario user = new Usuario("testuser", "password");

        Response response = target.path("login")
            .request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(user, MediaType.APPLICATION_JSON));

        assertEquals(Family.SUCCESSFUL, response.getStatusInfo().getFamily());
        System.out.println("testLoginUser passed.");
    }

    @Test
    @JUnitPerfTest(threads = 10, durationMs = 1000)
    public void testGetPeliculas() {
        System.out.println("Running testGetPeliculas...");
        Response response = target.path("getPeliculas")
            .request(MediaType.APPLICATION_JSON)
            .get();

        assertEquals(Family.SUCCESSFUL, response.getStatusInfo().getFamily());
        System.out.println("testGetPeliculas passed.");
    }
}