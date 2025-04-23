// package org.datanucleus;

// import static org.junit.Assert.assertEquals;

// import java.text.SimpleDateFormat;
// import java.util.ArrayList;
// import java.util.Date;
// import java.util.List;

// import javax.jdo.JDOHelper;
// import javax.jdo.PersistenceManager;
// import javax.jdo.PersistenceManagerFactory;
// import javax.jdo.Transaction;

// import javax.ws.rs.client.Client;
// import javax.ws.rs.client.ClientBuilder;
// import javax.ws.rs.client.Entity;
// import javax.ws.rs.client.WebTarget;
// import javax.ws.rs.core.MediaType;
// import javax.ws.rs.core.Response;
// import javax.ws.rs.core.Response.Status.Family;

// import org.glassfish.grizzly.http.server.HttpServer;
// import org.junit.AfterClass;
// import org.junit.Before;
// import org.junit.BeforeClass;
// import org.junit.Test;
// import org.junit.experimental.categories.Category;

// import es.deusto.spq.server.Main;
// import es.deusto.spq.server.jdo.Asiento;
// import es.deusto.spq.server.jdo.Entrada;
// import es.deusto.spq.server.jdo.Pelicula;
// import es.deusto.spq.server.jdo.Sala;
// import es.deusto.spq.server.jdo.TipoAsiento;
// import es.deusto.spq.server.jdo.TipoUsuario;
// import es.deusto.spq.server.jdo.Usuario;

// import org.datanucleus.categories.IntegrationTest;

// /**
//  * Integration tests for the server, testing HTTP endpoints with a real database.
//  */
// @Category(IntegrationTest.class)
// public class ServerIntegrationTest {
//     private static final PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");

//     private static HttpServer server;
//     private WebTarget target;

//     private static Date fecha;
//     private static Sala sala;
//     private static Pelicula pelicula;

//     @BeforeClass
//     public static void prepareTests() throws Exception {
//         // Parsear la fecha
//         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//         fecha = dateFormat.parse("2025-04-23");

//         // Iniciar el servidor
//         server = Main.startServer();

//         // Preparar datos de prueba en la base de datos
//         PersistenceManager pm = pmf.getPersistenceManager();
//         Transaction tx = pm.currentTransaction();

//         try {
//             tx.begin();
        
//             // Limpiar la base de datos primero
//             pm.newQuery(Usuario.class).deletePersistentAll();
//             pm.newQuery(Pelicula.class).deletePersistentAll();
//             pm.newQuery(Sala.class).deletePersistentAll();
//             pm.newQuery(Asiento.class).deletePersistentAll();
//             pm.newQuery(Entrada.class).deletePersistentAll();
        
//             tx.commit();
//         } finally {
//             if (tx.isActive()) {
//                 tx.rollback();
//             }
//             pm.close();
//         }

//         try {
//             tx.begin();

//             // Crear asientos para la sala
//             List<Asiento> asientos = new ArrayList<>();
//             asientos.add(new Asiento(0, 1, TipoAsiento.VIP, false)); // ID 0 para que JDO lo genere
//             asientos.add(new Asiento(0, 2, TipoAsiento.NORMAL, false));
//             asientos.add(new Asiento(0, 3, TipoAsiento.DISCAPACITADOS, false));
//             for (Asiento asiento : asientos) {
//                 pm.makePersistent(asiento);
//             }

//             // Crear una sala con asientos
//             sala = new Sala(0, 1, 80, asientos); // ID 0 para que JDO lo genere
//             pm.makePersistent(sala);

//             // Crear un usuario de prueba
//             pm.makePersistent(new Usuario("33546567B", "Test", "User", "test@example.com", "testuser", "password", "Calle 123", "123456789", TipoUsuario.CLIENTE));

//             // Crear una película de prueba
//             pelicula = new Pelicula("Test Movie", "Drama", 120, fecha, "Test Director", "Test Synopsis", "18:00", sala);
//             pm.makePersistent(pelicula);

//             tx.commit();
//         } catch (Exception e) {
//             e.printStackTrace();
//             throw e;
//         } finally {
//             if (tx.isActive()) {
//                 tx.rollback();
//             }
//             pm.close();
//         }
//     }

//     @Before
//     public void setUp() {
//         Client c = ClientBuilder.newClient();
//         target = c.target(Main.BASE_URI).path("resource");
//     }

//     @AfterClass
//     public static void tearDownServer() throws Exception {
//         // Apagar el servidor
//         if (server != null) {
//             server.shutdown();
//         }

//         // Limpiar la base de datos
//         PersistenceManager pm = pmf.getPersistenceManager();
//         Transaction tx = pm.currentTransaction();
//         try {
//             tx.begin();
//             pm.newQuery(Usuario.class).deletePersistentAll();
//             pm.newQuery(Pelicula.class).deletePersistentAll();
//             pm.newQuery(Sala.class).deletePersistentAll();
//             pm.newQuery(Asiento.class).deletePersistentAll();
//             pm.newQuery(Entrada.class).deletePersistentAll();
//             tx.commit();
//         } finally {
//             if (tx.isActive()) {
//                 tx.rollback();
//             }
//             pm.close();
//         }
//     }

//     @Test
//     public void testLoginUser() {
//         Usuario user = new Usuario(null, null, null, null, "testuser", "password", null, null, null);

//         Response response = target.path("login")
//             .request(MediaType.APPLICATION_JSON)
//             .post(Entity.entity(user, MediaType.APPLICATION_JSON));

//         assertEquals(Family.SUCCESSFUL, response.getStatusInfo().getFamily());
//     }

//     @Test
//     public void testCrearPelicula() {
//         Pelicula newPelicula = new Pelicula("Test Movie 2", "Action", 110, fecha, "Test Director 2", "Test Synopsis 2", "20:00", sala);

//         Response response = target.path("crearPelicula")
//             .request(MediaType.APPLICATION_JSON)
//             .post(Entity.entity(newPelicula, MediaType.APPLICATION_JSON));

//         assertEquals(Family.SUCCESSFUL, response.getStatusInfo().getFamily());
//     }

//     @Test
//     public void testGetPeliculas() {
//         // Primero creamos una película para asegurarnos de que haya datos
//         Pelicula newPelicula = new Pelicula("Test Movie 3", "Action", 110, fecha, "Test Director 3", "Test Synopsis 3", "20:00", sala);

//         Response createResponse = target.path("crearPelicula")
//             .request(MediaType.APPLICATION_JSON)
//             .post(Entity.entity(newPelicula, MediaType.APPLICATION_JSON));
//         assertEquals(Family.SUCCESSFUL, createResponse.getStatusInfo().getFamily());

//         // Ahora obtenemos la lista de películas
//         Response response = target.path("getPeliculas")
//             .request(MediaType.APPLICATION_JSON)
//             .get();

//         assertEquals(Family.SUCCESSFUL, response.getStatusInfo().getFamily());
//     }

//     @Test
//     public void testComprarEntrada() {
//         Response response = target.path("comprarEntrada/" + pelicula.getId() + "/VIP/1")
//             .request(MediaType.APPLICATION_JSON)
//             .get();

//         assertEquals(Family.SUCCESSFUL, response.getStatusInfo().getFamily());
//     }
// }