package org.datanucleus;

import java.util.HashMap;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.datanucleus.jdo.Pelicula;
import org.datanucleus.jdo.TipoUsuario;
import org.datanucleus.jdo.Usuario;


@Path("/resource")
@Produces(MediaType.APPLICATION_JSON)
public class Resource {

	protected static final Logger logger = LogManager.getLogger(Resource.class);

	private PersistenceManager pm = null;
	private Transaction tx = null;

	public static Map<Usuario, Long> tokens = new HashMap<Usuario, Long>();
	public static long token;
	public static Usuario usuario;
	public static Usuario usuario2;

	// Credenciales de Twilio
	public static final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
	public static final String AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN");

	public Resource() {
		PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");
		this.pm = pmf.getPersistenceManager();
		this.tx = pm.currentTransaction();
	}

	/**
	 * Logs in a user and returns a response.
	 *
	 * @param usuario The user to be logged in.
	 * @return A response indicating the login status.
	 */
	@POST
	@Path("/login")
	public Response loginUser(Usuario usuario) {
		try (PersistenceManager pm = JDOHelper.getPersistenceManagerFactory("datanucleus.properties")
				.getPersistenceManager()) {
			Transaction tx = pm.currentTransaction();
			try {
				tx.begin();
				Query<Usuario> query = pm.newQuery(Usuario.class, 
				"nombreUsuario == :nombreUsuario && contrasenya == :contrasenya");
				query.setUnique(true);
				Usuario user = (Usuario) query.execute(usuario.getNombre(), usuario.getContrasenya());
				if (user != null) {
					logger.info("User {} logged in successfully!", user.getNombre());
					long token = System.currentTimeMillis();
					tokens.put(user, token); // Store by user object instead of username
					tx.commit();
					return Response.ok(user).build();
				} else {
					logger.info("Invalid username or password");
					return Response.status(Response.Status.UNAUTHORIZED).entity("Invalid credentials").build();
				}
			} finally {
				if (tx.isActive()) {
					tx.rollback();
				}
			}
		}
	}

	/**
	 * Represents the HTTP response returned by the server.
	 */
	@POST
	@Path("/register")
	public Response registerUser(Usuario usuario) {
		try {
			tx.begin();

			Usuario user = null;
			try {
				user = pm.getObjectById(Usuario.class, usuario.getDni());
			} catch (javax.jdo.JDOObjectNotFoundException jonfe) {
				logger.info("Exception launched: {}", jonfe.getMessage());
			}

			if (user != null) {
				logger.info("User already exists!");
				tx.rollback();
				return Response.status(Response.Status.UNAUTHORIZED).entity("User already exists").build();
			} else {
				logger.info("Creating user: {}", user);
				user = new Usuario(usuario.getDni(), usuario.getNombre(), usuario.getApellidos(),usuario.getEmail(), 
				usuario.getContrasenya(), usuario.getDireccion(), usuario.getTelefono(),
						TipoUsuario.CLIENTE);
				pm.makePersistent(user);
				logger.info("User created: {}", user);
				tx.commit();
				return Response.ok().build();
			}
		} finally {
			if (tx.isActive()) {
				tx.rollback();
			}

		}
	}

	/**
     * Logs out a user by removing their token.
     *
     * @param usuario The user to be logged out.
     * @return A response indicating the logout status.
     */
    @POST
    @Path("/logout")
    public Response logoutUser(Usuario usuario) {
        if (tokens.containsKey(usuario)) {
            tokens.remove(usuario);
            logger.info("User {} logged out successfully!", usuario.getNombre());
            return Response.ok("User logged out successfully").build();
        } else {
            logger.info("Logout attempt failed: User {} not found or not logged in", usuario.getNombre());
            return Response.status(Response.Status.UNAUTHORIZED).entity("User not logged in").build();
        }
    }
    
	/**
	 * Creates a new peli in the system.
	 * 
	 * @param Pelicula The peli object containing the details of the peli to be
	 *               created.
	 * @return A Response object indicating the status of the operation.
	 *         If the peli already exists, returns an UNAUTHORIZED response with an
	 *         error message.
	 *         If the peli is successfully created, returns an OK response.
	 */
	@POST
	@Path("/crearPelicula")
	public Response crearPelicula(Pelicula pelicula) {
		System.out.println(pelicula);
		try {
			tx.begin();
			Pelicula peli = null;

			try {
				peli = pm.getObjectById(Pelicula.class, pelicula.getId());
			} catch (javax.jdo.JDOObjectNotFoundException jonfe) {
				logger.info("Exception launched: {}", jonfe.getMessage());
			}

			if (peli != null) {
				logger.info("film already exists!");
				tx.rollback();
				return Response.status(Response.Status.UNAUTHORIZED).entity("peli already exists").build();
			} else {
				logger.info("Creating peli: {}", peli);
				peli = new Pelicula(pelicula.getTitulo(), pelicula.getGenero(), pelicula.getDuracion(), pelicula.getFechaEstreno(),
						pelicula.getDirector(), pelicula.getSinopsis(), pelicula.getHorarios(), pelicula.getSala());
				pm.makePersistent(peli);
				logger.info("peli created: {}", peli);
				tx.commit();
				return Response.ok().build();
			}
		} finally {
			if (tx.isActive()) {
				tx.rollback();
			}
		}
	}

	/**
	 * Deletes an film from the server.
	 * 
	 * @param id the ID of the film to be deleted
	 * @return a Response object indicating the status of the operation
	 */
	@DELETE
	@Path("/eliminarPelicula/{id}")
	public Response eliminarEvento(@PathParam("id") String id) {
		try {
			tx.begin();

			Pelicula peli = null;

			try {
				peli = pm.getObjectById(Pelicula.class, id);
			} catch (javax.jdo.JDOObjectNotFoundException jonfe) {
				logger.info("Exception launched: {}", jonfe.getMessage());
			}

			if (peli != null) {
				logger.info("Deleting event: {}", peli);
				pm.deletePersistent(peli);
				tx.commit();
				return Response.ok().build();
			} else {
				logger.info("Event not found");
				tx.rollback();
				return Response.status(Response.Status.NOT_FOUND).entity("Film not found").build();
			}
		} finally {
			if (tx.isActive()) {
				tx.rollback();
			}
			pm.close();
		}
	}
    
    
}