package es.deusto.spq.server;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilio.Twilio;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import es.deusto.spq.server.jdo.Usuario;
import es.deusto.spq.server.jdo.Asiento;
import es.deusto.spq.server.jdo.Cine;
import es.deusto.spq.server.jdo.Entrada;
import es.deusto.spq.server.jdo.Mensaje;
import es.deusto.spq.server.jdo.Pelicula;
import es.deusto.spq.server.jdo.Resenya;
import es.deusto.spq.server.jdo.Sala;
import es.deusto.spq.server.jdo.TipoAsiento;
import es.deusto.spq.server.jdo.TipoUsuario;


import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;


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

	// public static String recoveryCode = "JTRPEYE5LM2LQNJSVWE3K5NU";

	// Credenciales de Twilio
	public static final String ACCOUNT_SID = "AC7ef1ed1b261d35cc14e53ea6d46b48c0";
  	public static final String AUTH_TOKEN = "92b53b662ff5711cd321223c9ff1b76b";

	public Resource() {
		PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");
		this.pm = pmf.getPersistenceManager();
		this.tx = pm.currentTransaction();
	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("/getResenyas/{peliculaId}")
	public Response getResenyas(@PathParam("peliculaId") long peliculaId) {
		PersistenceManager pmTemp = null;
		Transaction txTemp = null;

		try {
			PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");
			pmTemp = pmf.getPersistenceManager();
			txTemp = pmTemp.currentTransaction();
			txTemp.begin();

			Query<Resenya> query = pmTemp.newQuery(Resenya.class, "pelicula.id == :peliculaId");
			List<Resenya> resenyas = (List<Resenya>) query.execute(peliculaId);

			if (resenyas != null && !resenyas.isEmpty()) {
				logger.info("Se encontraron {} reseñas para la película {}", resenyas.size(), peliculaId);
				txTemp.commit();
				return Response.ok(resenyas).build();
			} else {
				logger.info("No se encontraron reseñas para la película {}", peliculaId);
				txTemp.commit();
				return Response.ok(new ArrayList<Resenya>()).build();
			}
		} catch (Exception e) {
			logger.error("Error al obtener reseñas: {}", e.getMessage());
			if (txTemp != null && txTemp.isActive()) {
				txTemp.rollback();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al obtener reseñas").build();
		} finally {
			if (pmTemp != null && !pmTemp.isClosed()) {
				pmTemp.close();
			}
		}
	}

	@GET
	@Path("/getAllResenyas")
	public Response getAllResenyas() {
		PersistenceManager pmTemp = null;
		Transaction txTemp = null;

		try {
			PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");
			pmTemp = pmf.getPersistenceManager();
			txTemp = pmTemp.currentTransaction();
			txTemp.begin();

			Query<Resenya> query = pmTemp.newQuery(Resenya.class);
			@SuppressWarnings("unchecked")
			List<Resenya> resenyas = (List<Resenya>) query.execute();

			if (resenyas != null && !resenyas.isEmpty()) {
				logger.info("Se encontraron {} reseñas", resenyas.size());
				txTemp.commit();
				return Response.ok(resenyas).build();
			} else {
				logger.info("No se encontraron reseñas");
				txTemp.commit();
				return Response.ok(new ArrayList<Resenya>()).build();
			}
		} catch (Exception e) {
			logger.error("Error al obtener todas las reseñas: {}", e.getMessage());
			if (txTemp != null && txTemp.isActive()) {
				txTemp.rollback();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al obtener reseñas").build();
		} finally {
			if (pmTemp != null && !pmTemp.isClosed()) {
				pmTemp.close();
			}
		}
	}

	@GET
	@Path("/getResenya/{resenyaId}")
	public Response getResenya(@PathParam("resenyaId") long resenyaId) {
		PersistenceManager pmTemp = null;
		Transaction txTemp = null;

		try {
			PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");
			pmTemp = pmf.getPersistenceManager();
			txTemp = pmTemp.currentTransaction();
			txTemp.begin();

			Resenya resenya = null;
			try {
				resenya = pmTemp.getObjectById(Resenya.class, resenyaId);
			} catch (javax.jdo.JDOObjectNotFoundException jonfe) {
				logger.info("Reseña no encontrada con ID: {}", resenyaId);
				txTemp.rollback();
				return Response.status(Response.Status.NOT_FOUND).entity("Reseña no encontrada").build();
			}

			logger.info("Reseña encontrada: {}", resenya);
			txTemp.commit();
			return Response.ok(resenya).build();
		} catch (Exception e) {
			logger.error("Error al obtener reseña: {}", e.getMessage());
			if (txTemp != null && txTemp.isActive()) {
				txTemp.rollback();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al obtener reseña").build();
		} finally {
			if (pmTemp != null && !pmTemp.isClosed()) {
				pmTemp.close();
			}
		}
	}

	@POST
	@Path("/addResenya")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addResenya(Resenya resenya) {
		PersistenceManager pmTemp = null;
		Transaction txTemp = null;

		try {
			PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");
			pmTemp = pmf.getPersistenceManager();
			txTemp = pmTemp.currentTransaction();
			txTemp.begin();

			Pelicula pelicula = null;
			try {
				pelicula = pmTemp.getObjectById(Pelicula.class, resenya.getPelicula().getId());
			} catch (javax.jdo.JDOObjectNotFoundException jonfe) {
				logger.info("Película no encontrada con ID: {}", resenya.getPelicula().getId());
				txTemp.rollback();
				return Response.status(Response.Status.NOT_FOUND).entity("Película no encontrada").build();
			}

			Usuario usuario = null;
			try {
				Query<Usuario> query = pmTemp.newQuery(Usuario.class, "nombreUsuario == :nombreUsuario");
				query.setUnique(true); // Esperamos un único resultado
				usuario = (Usuario) query.execute(resenya.getUsuario().getNombreUsuario());

				if (usuario == null) {
					logger.info("Usuario no encontrado: {}", resenya.getUsuario().getNombreUsuario());
					txTemp.rollback();
					return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado").build();
				}
			} catch (Exception e) {
				logger.error("Error al buscar usuario: {}", e.getMessage());
				txTemp.rollback();
				return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al buscar usuario").build();
			}


			// Asignar la película y el usuario a la reseña
			resenya.setPelicula(pelicula);
			resenya.setUsuario(usuario);
			pmTemp.makePersistent(resenya);
			logger.info("Reseña creada: {}", resenya.getComentario(), resenya.getPuntuacion(), resenya.getPelicula().getTitulo(), resenya.getUsuario().getNombreUsuario());

			txTemp.commit();
			return Response.ok(resenya).build();
		} catch (Exception e) {
			logger.error("Error al crear reseña: {}", e.getMessage());
			if (txTemp != null && txTemp.isActive()) {
				txTemp.rollback();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al crear reseña").build();
		} finally {
			if (pmTemp != null && !pmTemp.isClosed()) {
				pmTemp.close();
			}
		}
	}

	@PUT
	@Path("/updateResenya/{resenyaId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateResenya(@PathParam("resenyaId") long resenyaId, Resenya updatedResenya) {
		PersistenceManager pmTemp = null;
		Transaction txTemp = null;

		try {
			PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");
			pmTemp = pmf.getPersistenceManager();
			txTemp = pmTemp.currentTransaction();
			txTemp.begin();

			Resenya resenya = null;
			try {
				resenya = pmTemp.getObjectById(Resenya.class, resenyaId);
			} catch (javax.jdo.JDOObjectNotFoundException jonfe) {
				logger.info("Reseña no encontrada con ID: {}", resenyaId);
				txTemp.rollback();
				return Response.status(Response.Status.NOT_FOUND).entity("Reseña no encontrada").build();
			}

			resenya.setComentario(updatedResenya.getComentario());
			resenya.setPuntuacion(updatedResenya.getPuntuacion());
			logger.info("Reseña actualizada: {}", resenya.getComentario(), resenya.getPuntuacion(), resenya.getPelicula().getTitulo(), resenya.getUsuario().getNombreUsuario());


			txTemp.commit();
			return Response.ok(resenya).build();
		} catch (Exception e) {
			logger.error("Error al actualizar reseña: {}", e.getMessage());
			if (txTemp != null && txTemp.isActive()) {
				txTemp.rollback();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al actualizar reseña").build();
		} finally {
			if (pmTemp != null && !pmTemp.isClosed()) {
				pmTemp.close();
			}
		}
	}

	@DELETE
	@Path("/deleteResenya/{resenyaId}")
	public Response deleteResenya(@PathParam("resenyaId") long resenyaId) {
		PersistenceManager pmTemp = null;
		Transaction txTemp = null;

		try {
			PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");
			pmTemp = pmf.getPersistenceManager();
			txTemp = pmTemp.currentTransaction();
			txTemp.begin();

			Resenya resenya = null;
			try {
				resenya = pmTemp.getObjectById(Resenya.class, resenyaId);
			} catch (javax.jdo.JDOObjectNotFoundException jonfe) {
				logger.info("Reseña no encontrada con ID: {}", resenyaId);
				txTemp.rollback();
				return Response.status(Response.Status.NOT_FOUND).entity("Reseña no encontrada").build();
			}

			pmTemp.deletePersistent(resenya);
			logger.info("Reseña eliminada: {}", resenyaId);

			txTemp.commit();
			return Response.ok().build();
		} catch (Exception e) {
			logger.error("Error al eliminar reseña: {}", e.getMessage());
			if (txTemp != null && txTemp.isActive()) {
				txTemp.rollback();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al eliminar reseña").build();
		} finally {
			if (pmTemp != null && !pmTemp.isClosed()) {
				pmTemp.close();
			}
		}
	}

	
	

	/**
     * Retrieves a specific movie by its ID.
     * 
     * @param id The ID of the movie to retrieve.
     * @return A Response containing the movie if found, or a NOT_FOUND status if not.
     */
    @GET
	@Path("/getPelicula/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPelicula(@PathParam("id") long id) {
		PersistenceManager pmTemp = null;
		Transaction txTemp = null;

		try {
			PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");
			pmTemp = pmf.getPersistenceManager();
			txTemp = pmTemp.currentTransaction();
			txTemp.begin();

			Pelicula pelicula = null;
			try {
				pelicula = pmTemp.getObjectById(Pelicula.class, id);
				pelicula = pmTemp.detachCopy(pelicula); // Forzar la carga de los campos
			} catch (javax.jdo.JDOObjectNotFoundException jonfe) {
				logger.info("Película no encontrada con ID: {}", id);
				txTemp.rollback();
				return Response.status(Response.Status.NOT_FOUND).entity("Película no encontrada").build();
			}

			logger.info("Película encontrada: {}", pelicula.getTitulo());
			txTemp.commit();
			return Response.ok(pelicula).build();

		} catch (Exception e) {
			logger.error("Error al obtener película: {}", e.getMessage());
			if (txTemp != null && txTemp.isActive()) {
				txTemp.rollback();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al obtener película").build();
		} finally {
			if (pmTemp != null && !pmTemp.isClosed()) {
				pmTemp.close();
			}
		}
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
				Usuario user = (Usuario) query.execute(usuario.getNombreUsuario(), usuario.getContrasenya());
				
				if (user != null) {
					logger.info("User {} logged in successfully!", user.getNombre());
					logger.info("Usuario recuperado: {}", user);
	
					// Asegúrate de que todas las propiedades están configuradas
					Usuario responseUser = new Usuario();
					responseUser.setNombre(user.getNombre());
					responseUser.setApellidos(user.getApellidos());
					responseUser.setEmail(user.getEmail());
					responseUser.setNombreUsuario(user.getNombreUsuario());
					responseUser.setDireccion(user.getDireccion());
					responseUser.setTelefono(user.getTelefono());
					responseUser.setTipoUsuario(user.getTipoUsuario());
					responseUser.setDni(user.getDni());
	
					long token = System.currentTimeMillis();
					tokens.put(user, token);
					tx.commit();
	
					try {
						logger.info("JSON devuelto: {}", new ObjectMapper().writeValueAsString(responseUser));
					} catch (JsonProcessingException e) {
						e.printStackTrace();
					}
					return Response.ok(responseUser).build();
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
 				user = new Usuario(usuario.getDni(), usuario.getNombre(), usuario.getApellidos(),usuario.getEmail(), usuario.getNombreUsuario(), 
 				usuario.getContrasenya(), usuario.getDireccion(), usuario.getTelefono(),
 						TipoUsuario.CLIENTE);
 				logger.info("Creating user: {}", usuario.getNombreUsuario());
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


	@PUT
	@Path("/updateUser")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updateUser(Usuario usuario) {
		try {
			tx.begin();
	
			Usuario user = null;
			try {
				// Busca al usuario por su DNI
				user = pm.getObjectById(Usuario.class, usuario.getDni());
			} catch (javax.jdo.JDOObjectNotFoundException jonfe) {
				logger.info("Usuario no encontrado: {}", jonfe.getMessage());
				tx.rollback();
				return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado").build();
			}
	
			// Si el usuario existe, actualiza solo los campos no vacíos
			if (user != null) {
				// Solo actualiza los campos si no están vacíos en el objeto usuario recibido
				if (usuario.getNombre() != null && !usuario.getNombre().isEmpty()) {
					user.setNombre(usuario.getNombre());
				}
				if (usuario.getApellidos() != null && !usuario.getApellidos().isEmpty()) {
					user.setApellidos(usuario.getApellidos());
				}
				if (usuario.getEmail() != null && !usuario.getEmail().isEmpty()) {
					user.setEmail(usuario.getEmail());
				}
				if (usuario.getNombreUsuario() != null && !usuario.getNombreUsuario().isEmpty()) {
					user.setNombreUsuario(usuario.getNombreUsuario());
				}
				if (usuario.getContrasenya() != null && !usuario.getContrasenya().isEmpty()) {
					user.setContrasenya(usuario.getContrasenya());
				}
				if (usuario.getDireccion() != null && !usuario.getDireccion().isEmpty()) {
					user.setDireccion(usuario.getDireccion());
				}
				if (usuario.getTelefono() != null && !usuario.getTelefono().isEmpty()) {
					user.setTelefono(usuario.getTelefono());
				}
				if (usuario.getTipoUsuario() != null) {
					user.setTipoUsuario(usuario.getTipoUsuario());
				}
	
				logger.info("Usuario actualizado: {}", user);
				tx.commit();
				return Response.ok(user).build(); // Devuelve el usuario actualizado
			} else {
				logger.info("Usuario no encontrado");
				tx.rollback();
				return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado").build();
			}
		} catch (Exception e) {
			logger.error("Error al actualizar el usuario: {}", e.getMessage());
			if (tx.isActive()) {
				tx.rollback();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al actualizar el usuario").build();
		}
	}

	@DELETE
	@Path("/deleteUser/{nombreUsuario}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteUser(@PathParam("nombreUsuario") String nombreUsuario) {
		try {
			tx.begin();

			Usuario user = null;
			try {
				// Usa una consulta para buscar al usuario por nombreUsuario
				Query<Usuario> query = pm.newQuery(Usuario.class, "nombreUsuario == :nombreUsuario");
				query.setUnique(true);
				user = (Usuario) query.execute(nombreUsuario);
			} catch (Exception e) {
				logger.info("Error al buscar el usuario: {}", e.getMessage());
			}

			// Si el usuario existe, lo elimina
			if (user != null) {
				logger.info("Eliminando usuario: {}", user);
				pm.deletePersistent(user);
				tx.commit();
				return Response.ok("Usuario eliminado con éxito").build();
			} else {
				logger.info("Usuario no encontrado");
				tx.rollback();
				return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado").build();
			}
		} catch (Exception e) {
			logger.error("Error al eliminar el usuario: {}", e.getMessage());
			if (tx.isActive()) {
				tx.rollback();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al eliminar el usuario").build();
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
	 * Deletes a user account based on the provided DNI (Documento Nacional de
	 * Identidad).
	 * 
	 * @param dni The DNI of the user to be deleted.
	 * @return A Response object indicating the status of the operation.
	 *         - If the user is found and successfully deleted, returns a Response
	 *         with status 200 (OK).
	 *         - If the user is not found, returns a Response with status 404 (Not
	 *         Found) and an error message.
	 */
	@DELETE
	@Path("/eliminarCuenta/{dni}")
	public Response eliminarCuenta(@PathParam("dni") String dni) {
		try {
			tx.begin();

			Usuario usuario = null;

			try {
				usuario = pm.getObjectById(Usuario.class, dni);
			} catch (javax.jdo.JDOObjectNotFoundException jonfe) {
				logger.info("Exception launched: {}", jonfe.getMessage());
			}

			if (usuario != null) {
				logger.info("Deleting user: {}", usuario);
				pm.deletePersistent(usuario);
				tx.commit();
				return Response.ok().build();
			} else {
				logger.info("User not found");
				tx.rollback();
				return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
			}
		} finally {
			if (tx.isActive()) {
				tx.rollback();
			}
			pm.close();
		}
	}


	/**
	 * Retrieves the user with the specified ID.
	 * 
	 * @param id the ID of the user
	 * @return a Response object containing the user if found, or an error message
	 *         if not found
	 */
	@GET
	@Path("/getUsuarioId/{nombreUsuario}")
	public Response getUsuarioId(@PathParam("nombreUsuario") String nombreUsuario) {
		try {
			tx.begin();
			Usuario user = null;

			try {
				// Usa una consulta para buscar al usuario por nombreUsuario
				Query<Usuario> query = pm.newQuery(Usuario.class, "nombreUsuario == :nombreUsuario");
				query.setUnique(true);
				user = (Usuario) query.execute(nombreUsuario); // Obtiene el usuario completo
			} catch (javax.jdo.JDOObjectNotFoundException jonfe) {
				logger.info("Exception launched: {}", jonfe.getMessage());
			}

			if (user != null) {
				logger.info("User found: {}", user);
				tx.commit();
				return Response.ok(user).build(); // Devuelve el usuario encontrado
			} else {
				logger.info("No user found");
				tx.rollback();
				return Response.status(Response.Status.NOT_FOUND).entity("User not found").build();
			}
		} finally {
			if (tx.isActive()) {
				tx.rollback();
			}
			pm.close();
		}
	}
	
	/**
	 * Obtiene información del usuario por nombre de usuario
	 * @param nombreUsuario Nombre de usuario a buscar
	 * @return Response con los datos del usuario o error si no se encuentra
	 */
	@GET
	@Path("/getUsuario/{nombreUsuario}")
	public Response getUsuario(@PathParam("nombreUsuario") String nombreUsuario) {
		PersistenceManager pmTemp = null;
		Transaction txTemp = null;
		
		try {
			PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");
			pmTemp = pmf.getPersistenceManager();
			txTemp = pmTemp.currentTransaction();
			txTemp.begin();
			
			// Buscar usuario por nombre de usuario
			Query<Usuario> query = pmTemp.newQuery(Usuario.class, "nombreUsuario == :nombreUsuario");
			query.setUnique(true);
			Usuario usuario = (Usuario) query.execute(nombreUsuario);
			
			if (usuario != null) {
				logger.info("Usuario encontrado: {}", usuario);
				txTemp.commit();
				return Response.ok(usuario).build();
			} else {
				logger.info("Usuario no encontrado: {}", nombreUsuario);
				txTemp.rollback();
				return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado").build();
			}
		} catch (Exception e) {
			logger.error("Error al buscar usuario: {}", e.getMessage());
			if (txTemp != null && txTemp.isActive()) {
				txTemp.rollback();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al buscar usuario").build();
		} finally {
			if (pmTemp != null && !pmTemp.isClosed()) {
				pmTemp.close();
			}
		}
	}


	@GET
	@Path("/getSalas")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSalas() {
		PersistenceManager pmTemp = null;
		Transaction txTemp = null;
		
		try {
			PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");
			pmTemp = pmf.getPersistenceManager();
			txTemp = pmTemp.currentTransaction();
			txTemp.begin();
			
			Query<Sala> query = pmTemp.newQuery(Sala.class);

			@SuppressWarnings("unchecked")
			List<Sala> salas = (List<Sala>) query.execute();

			if (salas != null && !salas.isEmpty()) {
				logger.info("{} salas encontradas", salas.size());
				txTemp.commit();
				return Response.ok(salas).build();
			} else {
				logger.info("No se encontraron salas");
				txTemp.rollback();
				return Response.status(Response.Status.NOT_FOUND).entity("No se encontraron salas").build();
			}
		} catch (Exception e) {
			logger.error("Error al obtener salas: {}", e.getMessage());
			if (txTemp != null && txTemp.isActive()) {
				txTemp.rollback();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al obtener salas").build();
		} finally {
			if (pmTemp != null && !pmTemp.isClosed()) {
				pmTemp.close();
			}
		}
	}


	@GET
@Path("/getSala/{salaId}")
@Produces(MediaType.APPLICATION_JSON)
public Response getSala(@PathParam("salaId") long salaId) {
    PersistenceManager pmTemp = null;
    Transaction txTemp = null;

    try {
        PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");
        pmTemp = pmf.getPersistenceManager();
        txTemp = pmTemp.currentTransaction();
        txTemp.begin();

        Sala sala = null;
        try {
            sala = pmTemp.getObjectById(Sala.class, salaId);
            sala = pmTemp.detachCopy(sala); // Detach para evitar problemas de serialización
        } catch (javax.jdo.JDOObjectNotFoundException jonfe) {
            logger.info("Sala no encontrada con ID: {}", salaId);
            txTemp.rollback();
            return Response.status(Response.Status.NOT_FOUND).entity("Sala no encontrada").build();
        }

        logger.info("Sala encontrada: {}", sala.getId());
        txTemp.commit();
        return Response.ok(sala).build();

    } catch (Exception e) {
        logger.error("Error al obtener sala: {}", e.getMessage());
        if (txTemp != null && txTemp.isActive()) {
            txTemp.rollback();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al obtener sala").build();
    } finally {
        if (pmTemp != null && !pmTemp.isClosed()) {
            pmTemp.close();
        }
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
		PersistenceManager pmTemp = null;
		Transaction txTemp = null;
		
		try {
			PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");
			pmTemp = pmf.getPersistenceManager();
			txTemp = pmTemp.currentTransaction();
			txTemp.begin();
			
			logger.info("Intento de crear película: {}", pelicula);
			
			// Verificar si ya existe una película con el mismo título
			Query<Pelicula> query = pmTemp.newQuery(Pelicula.class, "titulo == :titulo");
			query.setUnique(true);
			Pelicula peliculaExistente = (Pelicula) query.execute(pelicula.getTitulo());
			
			if (peliculaExistente != null) {
				logger.info("Ya existe una película con ese título: {}", pelicula.getTitulo());
				txTemp.rollback();
				return Response.status(Response.Status.CONFLICT).entity("Ya existe una película con ese título").build();
			}
			
			// Obtener la referencia a la sala completa
			Sala sala = null;
			if (pelicula.getSala() != null && pelicula.getSala().getId() > 0) {
				try {
					sala = pmTemp.getObjectById(Sala.class, pelicula.getSala().getId());
				} catch (javax.jdo.JDOObjectNotFoundException jonfe) {
					logger.error("No se encontró la sala con ID: {}", pelicula.getSala().getId());
					txTemp.rollback();
					return Response.status(Response.Status.NOT_FOUND).entity("No se encontró la sala seleccionada").build();
				}
			} else {
				logger.error("No se proporcionó una sala válida");
				txTemp.rollback();
				return Response.status(Response.Status.BAD_REQUEST).entity("Se requiere una sala válida").build();
			}
			
			// Crear la nueva película con la sala completa
			Pelicula nuevaPelicula = new Pelicula(
				pelicula.getTitulo(),
				pelicula.getGenero(),
				pelicula.getDuracion(),
				pelicula.getFechaEstreno(),
				pelicula.getDirector(),
				pelicula.getSinopsis(),
				pelicula.getHorario(),
				sala
			);
			
			pmTemp.makePersistent(nuevaPelicula);
			logger.info("Película creada exitosamente: {}", nuevaPelicula);
			
			txTemp.commit();
			return Response.ok().build();
			
		} catch (Exception e) {
			logger.error("Error al crear la película: {}", e.getMessage());
			if (txTemp != null && txTemp.isActive()) {
				txTemp.rollback();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al crear la película: " + e.getMessage()).build();
		} finally {
			if (pmTemp != null && !pmTemp.isClosed()) {
				pmTemp.close();
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
	public Response eliminarpelicula(@PathParam("id") String id) {
		try {
			tx.begin();

			Pelicula peli = null;

			try {
				peli = pm.getObjectById(Pelicula.class, id);
			} catch (javax.jdo.JDOObjectNotFoundException jonfe) {
				logger.info("Exception launched: {}", jonfe.getMessage());
			}

			if (peli != null) {
				logger.info("Deleting peli: {}", peli);
				pm.deletePersistent(peli);
				tx.commit();
				return Response.ok().build();
			} else {
				logger.info("peli not found");
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
    

	/**
	 * This method is used to update an pelicula object and return a Response.
	 * 
	 * @param pelicula The pelicula object containing the updated information.
	 * @return A Response indicating the success or failure of the update operation.
	 */
	@PUT
	@Path("/updatePelicula/{peliculaId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response updatePelicula(@PathParam("peliculaId") long peliculaId, Pelicula updatedPelicula) {
		PersistenceManager pmTemp = null;
		Transaction txTemp = null;

		try {
			PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");
			pmTemp = pmf.getPersistenceManager();
			txTemp = pmTemp.currentTransaction();
			txTemp.begin();

			Pelicula pelicula = null;
			try {
				pelicula = pmTemp.getObjectById(Pelicula.class, peliculaId);
			} catch (javax.jdo.JDOObjectNotFoundException jonfe) {
				logger.info("Reseña no encontrada con ID: {}", peliculaId);
				txTemp.rollback();
				return Response.status(Response.Status.NOT_FOUND).entity("Reseña no encontrada").build();
			}

			pelicula.setDirector(updatedPelicula.getDirector());
			pelicula.setDuracion(updatedPelicula.getDuracion());	
			pelicula.setFechaEstreno(updatedPelicula.getFechaEstreno());
			pelicula.setGenero(updatedPelicula.getGenero());
			pelicula.setHorario(updatedPelicula.getHorario());
			pelicula.setSinopsis(updatedPelicula.getSinopsis());
			pelicula.setTitulo(updatedPelicula.getTitulo());	
			pelicula.setSala(updatedPelicula.getSala());
			logger.info("pelicula actualizada: {}", pelicula.getTitulo(), pelicula.getDirector(), pelicula.getDuracion(), pelicula.getFechaEstreno(), pelicula.getGenero(), pelicula.getHorario(), pelicula.getSinopsis(), pelicula.getSala());

			
			txTemp.commit();
			return Response.ok(pelicula).build();
		} catch (Exception e) {
			logger.error("Error al actualizar reseña: {}", e.getMessage());
			if (txTemp != null && txTemp.isActive()) {
				txTemp.rollback();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al actualizar reseña").build();
		} finally {
			if (pmTemp != null && !pmTemp.isClosed()) {
				pmTemp.close();
			}
		}
	}


	/**
	 * Verifica si hay películas en la base de datos y, si no las hay, inicializa
	 * con un conjunto predeterminado de películas.
	 * 
	 * @return true si se inicializaron nuevas películas, false si ya existían
	 */
	private boolean inicializarPeliculas() {
		boolean inicializado = false;
		PersistenceManager pmTemp = null;
		Transaction txTemp = null;

		try {
			PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");
			pmTemp = pmf.getPersistenceManager();
			txTemp = pmTemp.currentTransaction();
			txTemp.begin();

			// Verificar si hay películas en la base de datos
			Query<Pelicula> query = pmTemp.newQuery(Pelicula.class);
			query.setRange(0, 1); // Solo necesitamos saber si hay al menos una
			
			@SuppressWarnings("unchecked")
			List<Pelicula> peliculas = (List<Pelicula>) query.execute();
			
			if (peliculas == null || peliculas.isEmpty()) {
				logger.info("No se encontraron películas. Inicializando la base de datos con películas predeterminadas.");
				
				// Crear salas con asientos inicializados
				Sala[] salas = crearSalasConAsientos(pmTemp);
				
				// Parsear fechas
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				
				// Crear y persistir películas
				Pelicula[] peliculasIniciales = {
					new Pelicula("El Padrino", "Drama", 175, dateFormat.parse("1972-03-24"), 
						"Francis Ford Coppola", 
						"La historia de la familia Corleone, una de las más poderosas familias de la mafia italoamericana en Nueva York después de la Segunda Guerra Mundial.", 
						"18:00, 21:00", salas[0]),
					
					new Pelicula("Titanic", "Romance", 195, dateFormat.parse("1997-12-19"), 
						"James Cameron", 
						"Un joven artista y una joven aristócrata se enamoran durante el desafortunado viaje inaugural del Titanic.", 
						"17:30, 20:30", salas[1]),
					
					new Pelicula("El Señor de los Anillos: La Comunidad del Anillo", "Fantasía", 178, dateFormat.parse("2001-12-19"), 
						"Peter Jackson", 
						"Un hobbit de la Comarca y ocho compañeros emprenden un viaje para destruir el poderoso Anillo Único y salvar la Tierra Media del Señor Oscuro Sauron.", 
						"16:00, 19:30", salas[2]),
					
					new Pelicula("Pulp Fiction", "Thriller", 154, dateFormat.parse("1994-10-14"), 
						"Quentin Tarantino", 
						"Las vidas de dos mafiosos, un boxeador, la esposa de un gánster y un par de bandidos se entrelazan en una historia de violencia y redención.", 
						"19:00, 22:00", salas[3]),
					
					new Pelicula("El Caballero Oscuro", "Acción", 152, dateFormat.parse("2008-07-18"), 
						"Christopher Nolan", 
						"Batman se enfrenta al Joker, un criminal psicópata que busca sumir Gotham City en el caos.", 
						"17:00, 20:00", salas[4]),
					
					new Pelicula("Forrest Gump", "Drama", 142, dateFormat.parse("1994-07-06"), 
						"Robert Zemeckis", 
						"La vida de un hombre con coeficiente intelectual bajo que logra grandes cosas y es testigo y protagonista de acontecimientos importantes en la historia de EE.UU.", 
						"16:30, 19:30", salas[0]),
					
					new Pelicula("Star Wars: Episodio IV - Una nueva esperanza", "Ciencia ficción", 121, dateFormat.parse("1977-05-25"), 
						"George Lucas", 
						"Un granjero se une a un caballero Jedi, un piloto y otros personajes para salvar a la galaxia del imperio.", 
						"18:30, 21:30", salas[1]),
					
					new Pelicula("Jurassic Park", "Aventura", 127, dateFormat.parse("1993-06-11"), 
						"Steven Spielberg", 
						"Un multimillonario crea un parque temático con dinosaurios clonados que escapan de control.", 
						"17:00, 20:00", salas[2])
				};
				
				for (Pelicula pelicula : peliculasIniciales) {
					pmTemp.makePersistent(pelicula);
				}
				
				inicializado = true;
				logger.info("Base de datos inicializada con {} películas.", peliculasIniciales.length);
			} else {
				logger.info("Ya existen películas en la base de datos.");
			}
			
			txTemp.commit();
		} catch (Exception e) {
			logger.error("Error al inicializar las películas: {}", e.getMessage());
			if (txTemp != null && txTemp.isActive()) {
				txTemp.rollback();
			}
		} finally {
			if (pmTemp != null && !pmTemp.isClosed()) {
				pmTemp.close();
			}
		}
		
		return inicializado;
	}

	/**
	 * Crea 5 salas con asientos inicializados y las persiste en la base de datos.
	 * @param pm PersistenceManager para persistir las salas y asientos
	 * @return Array con las salas creadas
	 */
	private Sala[] crearSalasConAsientos(PersistenceManager pm) {
		Sala[] salas = new Sala[5];
		
		// Crear las 5 salas
		for (int i = 0; i < 5; i++) {
			int capacidad = 80 + (i * 10); // Capacidades: 80, 90, 100, 110, 120
			List<Asiento> asientos = new ArrayList<>();
			
			// Crear asientos para cada sala
			for (int j = 1; j <= capacidad; j++) {
				TipoAsiento tipo;
				
				// Asignar tipo de asiento según posición
				if (j <= capacidad * 0.1) { // 10% VIP
					tipo = TipoAsiento.VIP;
				} else if (j <= capacidad * 0.15) { // 5% para discapacitados
					tipo = TipoAsiento.DISCAPACITADOS;
				} else { // El resto normales
					tipo = TipoAsiento.NORMAL;
				}
				
				// Crear el asiento con un ID único, número, tipo y estado inicial (no ocupado)
				Asiento asiento = new Asiento((i * 1000) + j, j, tipo, false);
				pm.makePersistent(asiento); // Persistir el asiento
				asientos.add(asiento);
			}
			
			// Crear la sala con sus asientos
			salas[i] = new Sala(i + 1, i + 1, capacidad, asientos, true);
			pm.makePersistent(salas[i]); // Persistir la sala
			
			logger.info("Sala {} creada con {} asientos", i + 1, capacidad);
		}
		
		return salas;
	}
	
	/**
	 * Retrieves a list of pelis.
	 * 
	 * @return a Response object containing the list of pelis if found, or an
	 *         unauthorized status with an error message if no pelis are found.
	 */
/**
 * Retrieves a list of películas. If no películas are found in the database,
 * it initializes the database with a default set of películas.
 * 
 * @return a Response object containing the list of películas if found, or an
 *         unauthorized status with an error message if no películas are found.
 */
	@GET
	@Path("/getPeliculas")
	public Response getPeliculas() {
		// Primero, intentar inicializar películas si es necesario
		inicializarPeliculas();
		
		PersistenceManager pmTemp = null;
		Transaction txTemp = null;
		
		try {
			PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");
			pmTemp = pmf.getPersistenceManager();
			txTemp = pmTemp.currentTransaction();
			txTemp.begin();
			
			Query<Pelicula> query = pmTemp.newQuery(Pelicula.class);

			@SuppressWarnings("unchecked")
			List<Pelicula> peliculas = (List<Pelicula>) query.execute();

			if (peliculas != null && !peliculas.isEmpty()) {
				logger.info("{} películas encontradas", peliculas.size());
				txTemp.commit();
				return Response.ok(peliculas).build();
			} else {
				logger.info("No se encontraron películas");
				txTemp.rollback();
				return Response.status(Response.Status.NOT_FOUND).entity("No se encontraron películas").build();
			}
		} catch (Exception e) {
			logger.error("Error al obtener películas: {}", e.getMessage());
			if (txTemp != null && txTemp.isActive()) {
				txTemp.rollback();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al obtener películas").build();
		} finally {
			if (pmTemp != null && !pmTemp.isClosed()) {
				pmTemp.close();
			}
		}
	}
	
	/**
	 * Obtiene los asientos de una sala
	 * @param salaId ID de la sala
	 * @return Response con la lista de asientos o error si no se encuentra
	 */
	@GET
	@Path("/getAsientos/{salaId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAsientos(@PathParam("salaId") long salaId) {
		PersistenceManager pmTemp = null;
		Transaction txTemp = null;
		
		try {
			PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");
			pmTemp = pmf.getPersistenceManager();
			txTemp = pmTemp.currentTransaction();
			txTemp.begin();
			
			// Obtener la sala por ID
			Sala sala = null;
			try {
				sala = pmTemp.getObjectById(Sala.class, salaId);
			} catch (javax.jdo.JDOObjectNotFoundException jonfe) {
				logger.info("Sala no encontrada con ID: {}", salaId);
				txTemp.rollback();
				return Response.status(Response.Status.NOT_FOUND).entity("Sala no encontrada").build();
			}
			
			// Obtener los asientos de la sala
			List<Asiento> asientos = sala.getAsientos();
			
			logger.info("Sala {}: {} asientos encontrados", salaId, asientos.size());
			txTemp.commit();
			return Response.ok(asientos).build();
			
		} catch (Exception e) {
			logger.error("Error al obtener asientos: {}", e.getMessage());
			if (txTemp != null && txTemp.isActive()) {
				txTemp.rollback();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al obtener asientos").build();
		} finally {
			if (pmTemp != null && !pmTemp.isClosed()) {
				pmTemp.close();
			}
		}
	}
	
	/**
	 * Obtiene un cine por su ID
	 * @param cineId ID del cine
	 * @return Response con el cine o error si no se encuentra
	 */
	@GET
	@Path("/getCine/{cineId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCine(@PathParam("cineId") long cineId) {
		PersistenceManager pmTemp = null;
		Transaction txTemp = null;
		
		try {
			PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");
			pmTemp = pmf.getPersistenceManager();
			txTemp = pmTemp.currentTransaction();
			txTemp.begin();
			
			// Obtener el cine por ID
			Cine cine = null;
			try {
				cine = pmTemp.getObjectById(Cine.class, cineId);
			} catch (javax.jdo.JDOObjectNotFoundException jonfe) {
				logger.info("Cine no encontrado con ID: {}", cineId);
				txTemp.rollback();
				return Response.status(Response.Status.NOT_FOUND).entity("Cine no encontrado").build();
			}
			
			logger.info("Cine encontrado: {}", cine);
			txTemp.commit();
			return Response.ok(cine).build();
			
		} catch (Exception e) {
			logger.error("Error al obtener cine: {}", e.getMessage());
			if (txTemp != null && txTemp.isActive()) {
				txTemp.rollback();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al obtener cine").build();
		} finally {
			if (pmTemp != null && !pmTemp.isClosed()) {
				pmTemp.close();
			}
		}
	}
	
	/**
	 * Obtiene todos los cines disponibles
	 * @return Response con la lista de cines o error si no hay
	 */
	@GET
	@Path("/getCines")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCines() {
		PersistenceManager pmTemp = null;
		Transaction txTemp = null;
		
		try {
			PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");
			pmTemp = pmf.getPersistenceManager();
			txTemp = pmTemp.currentTransaction();
			txTemp.begin();
			
			// Buscar todos los cines
			Query<Cine> query = pmTemp.newQuery(Cine.class);
			
			@SuppressWarnings("unchecked")
			List<Cine> cines = (List<Cine>) query.execute();
			
			// Si no hay cines, crear uno por defecto
			if (cines == null || cines.isEmpty()) {
				logger.info("No se encontraron cines. Creando cine por defecto.");
				
				// Crear un cine con las salas existentes
				Query<Sala> querySalas = pmTemp.newQuery(Sala.class);
				@SuppressWarnings("unchecked")
				List<Sala> salas = (List<Sala>) querySalas.execute();
				
				if (salas != null && !salas.isEmpty()) {
					Cine cineDefault = new Cine(1, "CineGP", "Av. Universidad 123", salas);
					pmTemp.makePersistent(cineDefault);
					
					cines = new ArrayList<>();
					cines.add(cineDefault);
					
					logger.info("Cine por defecto creado: {}", cineDefault);
				}
			}
			
			if (cines != null && !cines.isEmpty()) {
				logger.info("{} cines encontrados", cines.size());
				txTemp.commit();
				return Response.ok(cines).build();
			} else {
				logger.info("No se encontraron cines");
				txTemp.rollback();
				return Response.status(Response.Status.NOT_FOUND).entity("No se encontraron cines").build();
			}
			
		} catch (Exception e) {
			logger.error("Error al obtener cines: {}", e.getMessage());
			if (txTemp != null && txTemp.isActive()) {
				txTemp.rollback();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al obtener cines").build();
		} finally {
			if (pmTemp != null && !pmTemp.isClosed()) {
				pmTemp.close();
			}
		}
	}
	
	/**
 * Obtiene las entradas de un usuario específico
 * @param nombreUsuario Nombre de usuario
 * @return Response con las entradas o error si no hay
 */
@GET
@Path("/getEntradas/{nombreUsuario}")
@Produces(MediaType.APPLICATION_JSON)
public Response getEntradas(@PathParam("nombreUsuario") String nombreUsuario) {
    PersistenceManager pmTemp = null;
    Transaction txTemp = null;
    
    try {
        PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");
        pmTemp = pmf.getPersistenceManager();
        txTemp = pmTemp.currentTransaction();
        txTemp.begin();
        
        // Buscar usuario
        Query<Usuario> queryUsuario = pmTemp.newQuery(Usuario.class, "nombreUsuario == :nombreUsuario");
        queryUsuario.setUnique(true);
        Usuario usuario = (Usuario) queryUsuario.execute(nombreUsuario);
        
        if (usuario == null) {
            logger.info("Usuario no encontrado: {}", nombreUsuario);
            txTemp.rollback();
            return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado").build();
        }
        
        // Buscar entradas del usuario
        Query<Entrada> queryEntradas = pmTemp.newQuery(Entrada.class, "usuario.nombreUsuario == :nombreUsuario");

        @SuppressWarnings("unchecked")
        List<Entrada> entradas = (List<Entrada>) queryEntradas.execute(nombreUsuario);
        
        if (entradas != null) {
            logger.info("Se encontraron {} entradas para el usuario {}", entradas.size(), nombreUsuario);
            txTemp.commit();
            return Response.ok(entradas).build();
        } else {
            logger.info("No se encontraron entradas para el usuario {}", nombreUsuario);
            txTemp.commit(); // Aún así, es un resultado válido (lista vacía)
            return Response.ok(new ArrayList<Entrada>()).build();
        }
        
    } catch (Exception e) {
        logger.error("Error al obtener entradas: {}", e.getMessage());
        if (txTemp != null && txTemp.isActive()) {
            txTemp.rollback();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al obtener entradas").build();
    } finally {
        if (pmTemp != null && !pmTemp.isClosed()) {
            pmTemp.close();
        }
    }
}
	
	/**
	 * Clase para representar los datos de la compra de entradas
	 */
	public static class CompraEntradaDTO {
		private String nombreUsuario;
		private long cineId;
		private long peliculaId;
		private String horario;
		private List<AsientoDTO> asientos;
		private String metodoPago;
		private int precioTotal;
		
		// Getters y setters
		public String getNombreUsuario() { return nombreUsuario; }
		public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
		
		public long getCineId() { return cineId; }
		public void setCineId(long cineId) { this.cineId = cineId; }
		
		public long getPeliculaId() { return peliculaId; }
		public void setPeliculaId(long peliculaId) { this.peliculaId = peliculaId; }
		
		public String getHorario() { return horario; }
		public void setHorario(String horario) { this.horario = horario; }
		
		public List<AsientoDTO> getAsientos() { return asientos; }
		public void setAsientos(List<AsientoDTO> asientos) { this.asientos = asientos; }
		
		public String getMetodoPago() { return metodoPago; }
		public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
		
		public int getPrecioTotal() { return precioTotal; }
		public void setPrecioTotal(int precioTotal) { this.precioTotal = precioTotal; }
	}
	
	/**
	 * Clase para representar datos de asientos en la compra
	 */
	public static class AsientoDTO {
		private int numero;
		private TipoAsiento tipo;
		private int precio;
		
		// Getters y setters
		public int getNumero() { return numero; }
		public void setNumero(int numero) { this.numero = numero; }
		
		public TipoAsiento getTipo() { return tipo; }
		public void setTipo(TipoAsiento tipo) { this.tipo = tipo; }
		
		public int getPrecio() { return precio; }
		public void setPrecio(int precio) { this.precio = precio; }
	}
	
	/**
	 * Endpoint para comprar entradas
	 * @param compraDTO Datos de la compra
	 * @return Response con el resultado de la operación
	 */
	@POST
	@Path("/comprarEntradas")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response comprarEntradas(CompraEntradaDTO compraDTO) {
		PersistenceManager pmTemp = null;
		Transaction txTemp = null;
		
		try {
			PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");
			pmTemp = pmf.getPersistenceManager();
			txTemp = pmTemp.currentTransaction();
			txTemp.begin();
			
			// Validar datos de entrada
			if (compraDTO.getNombreUsuario() == null || compraDTO.getAsientos() == null || compraDTO.getAsientos().isEmpty()) {
				logger.error("Datos de compra incompletos o inválidos");
				txTemp.rollback();
				return Response.status(Response.Status.BAD_REQUEST).entity("Datos de compra incompletos").build();
			}
			
			// Buscar usuario
			Query<Usuario> queryUsuario = pmTemp.newQuery(Usuario.class, "nombreUsuario == :nombreUsuario");
			queryUsuario.setUnique(true);
			Usuario usuario = (Usuario) queryUsuario.execute(compraDTO.getNombreUsuario());
			
			if (usuario == null) {
				logger.info("Usuario no encontrado: {}", compraDTO.getNombreUsuario());
				txTemp.rollback();
				return Response.status(Response.Status.NOT_FOUND).entity("Usuario no encontrado").build();
			}
			
			// Buscar cine
			Cine cine = null;
			try {
				cine = pmTemp.getObjectById(Cine.class, compraDTO.getCineId());
			} catch (javax.jdo.JDOObjectNotFoundException jonfe) {
				logger.info("Cine no encontrado con ID: {}", compraDTO.getCineId());
				txTemp.rollback();
				return Response.status(Response.Status.NOT_FOUND).entity("Cine no encontrado").build();
			}
			
			// Buscar película
			Pelicula pelicula = null;
			try {
				pelicula = pmTemp.getObjectById(Pelicula.class, compraDTO.getPeliculaId());
			} catch (javax.jdo.JDOObjectNotFoundException jonfe) {
				logger.info("Película no encontrada con ID: {}", compraDTO.getPeliculaId());
				txTemp.rollback();
				return Response.status(Response.Status.NOT_FOUND).entity("Película no encontrada").build();
			}
			
			// Buscar sala (desde la película)
			Sala sala = pelicula.getSala();
			if (sala == null) {
				logger.error("La película no tiene sala asignada");
				txTemp.rollback();
				return Response.status(Response.Status.BAD_REQUEST).entity("La película no tiene sala asignada").build();
			}
			
			// Crear y guardar entradas
			List<Entrada> entradasCreadas = new ArrayList<>();
			
			for (AsientoDTO asientoDTO : compraDTO.getAsientos()) {
				// Buscar el asiento en la sala
				Asiento asientoSala = null;
				for (Asiento a : sala.getAsientos()) {
					if (a.getNumero() == asientoDTO.getNumero()) {
						asientoSala = a;
						break;
					}
				}
				
				// Verificar que el asiento existe y está disponible
				if (asientoSala == null) {
					logger.error("Asiento {} no encontrado en la sala {}", asientoDTO.getNumero(), sala.getId());
					txTemp.rollback();
					return Response.status(Response.Status.BAD_REQUEST)
						.entity("Asiento " + asientoDTO.getNumero() + " no encontrado en la sala").build();
				}
				
				if (asientoSala.isOcupado()) {
					logger.error("Asiento {} ya está ocupado", asientoDTO.getNumero());
					txTemp.rollback();
					return Response.status(Response.Status.BAD_REQUEST)
						.entity("Asiento " + asientoDTO.getNumero() + " ya está ocupado").build();
				}
				
				// Marcar el asiento como ocupado
				asientoSala.setOcupado(true);
				
				// Crear la entrada
				Entrada entrada = new Entrada();
				entrada.setUsuario(usuario);
				entrada.setCine(cine);
				entrada.setPrecio(asientoDTO.getPrecio());
				entrada.setAsiento(asientoDTO.getNumero());
				entrada.setTipoAsiento(asientoDTO.getTipo());
				
				// Persistir la entrada
				pmTemp.makePersistent(entrada);
				entradasCreadas.add(entrada);
								
				logger.info("Entrada creada: {}", entrada);
			}
			List<Integer> asientosComprados = new ArrayList<>();
			for (AsientoDTO asiento : compraDTO.getAsientos()) {
				asientosComprados.add(asiento.getNumero());
			}

			Mensaje mensaje = new Mensaje();
			String telefono = usuario.getTelefono();
			if (!telefono.startsWith("+34")) {
				telefono = "+34" + telefono;
			}
			mensaje.setTelefono(telefono);

			int diasASumar = 2;
			LocalDate fechaPelicula = LocalDate.now().plusDays(diasASumar);
			String diaPelicula = fechaPelicula.format(DateTimeFormatter.ofPattern("d"));

			String texto;
			if (asientosComprados.size() == 1) {
				texto = "¡Hola! Has comprado 1 entrada en CineGP para la película: "
					+ pelicula.getTitulo()
					+ " el día " + diaPelicula + " a las " + (compraDTO.getHorario() != null ? compraDTO.getHorario() : "Horario no especificado")
					+ ", en la sala: " + sala.getNumero()
					+ ". Tu asiento es el " + asientosComprados
					+ ". ¡Disfruta de la película!";
			} else {
				texto = "¡Hola! Has comprado " + asientosComprados.size() + " entradas en CineGP para la película: "
					+ pelicula.getTitulo()
					+ " el día " + diaPelicula + " a las " + (compraDTO.getHorario() != null ? compraDTO.getHorario() : "Horario no especificado")
					+ ", en la sala: " + sala.getNumero()
					+ ". Tus asientos son: " + asientosComprados
					+ ". ¡Disfruta de la película!";
			}

			mensaje.setMensaje(texto);
			sendMSG(mensaje);
			
			txTemp.commit();
			
			// Construir respuesta con resumen de la compra
			Map<String, Object> respuesta = new HashMap<>();
			respuesta.put("status", "success");
			respuesta.put("message", "Compra realizada con éxito");
			respuesta.put("totalEntradas", entradasCreadas.size());
			respuesta.put("precioTotal", compraDTO.getPrecioTotal());
			
			return Response.ok(respuesta).build();
			
		} catch (Exception e) {
			logger.error("Error al procesar la compra: {}", e.getMessage());
			if (txTemp != null && txTemp.isActive()) {
				txTemp.rollback();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al procesar la compra: " + e.getMessage()).build();
		} finally {
			if (pmTemp != null && !pmTemp.isClosed()) {
				pmTemp.close();
			}
		}
	}
	@POST
	@Path("/sendMSG")
	public Response sendMSG(Mensaje mensaje) {
		Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
		try {
			@SuppressWarnings("unused")
			Message message = Message.creator(
					new PhoneNumber("whatsapp:" + mensaje.getTelefono()),
					new PhoneNumber("whatsapp:+14155238886"), // Este es el número de Twilio sandbox para WhatsApp
					mensaje.getMensaje())
					.create();

			logger.info("Msg sended to: {}", mensaje.getTelefono());
			return Response.ok().build();
		} catch (Exception e) {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error sending message").build();
		}
	}

	
	
	/**
	 * Endpoint para cancelar una entrada
	 * @param entradaId ID de la entrada a cancelar
	 * @return Response con el resultado de la operación
	 */
	@DELETE
	@Path("/cancelarEntrada/{entradaId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response cancelarEntrada(@PathParam("entradaId") long entradaId) {
		PersistenceManager pmTemp = null;
		Transaction txTemp = null;
		
		try {
			PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");
			pmTemp = pmf.getPersistenceManager();
			txTemp = pmTemp.currentTransaction();
			txTemp.begin();
			
			// Buscar la entrada
			Entrada entrada = null;
			try {
				entrada = pmTemp.getObjectById(Entrada.class, entradaId);
			} catch (javax.jdo.JDOObjectNotFoundException jonfe) {
				logger.info("Entrada no encontrada con ID: {}", entradaId);
				txTemp.rollback();
				return Response.status(Response.Status.NOT_FOUND).entity("Entrada no encontrada").build();
			}
			
			// Liberar el asiento correspondiente
			Sala sala = null;
			if (entrada.getCine() != null && !entrada.getCine().getSalas().isEmpty()) {
				// Buscar la sala correspondiente
				for (Sala s : entrada.getCine().getSalas()) {
					if (s.getAsientos() != null) {
						for (Asiento a : s.getAsientos()) {
							if (a.getNumero() == entrada.getAsiento()) {
								// Marcar el asiento como libre
								a.setOcupado(false);
								sala = s;
								break;
							}
						}
					}
					if (sala != null) break;
				}
			}
			
			// Eliminar la entrada
			pmTemp.deletePersistent(entrada);
			
			logger.info("Entrada {} cancelada", entradaId);
			if (sala != null) {
				logger.info("Asiento {} liberado en sala {}", entrada.getAsiento(), sala.getId());
			}
			
			txTemp.commit();
			
			Map<String, Object> respuesta = new HashMap<>();
			respuesta.put("status", "success");
			respuesta.put("message", "Entrada cancelada con éxito");
			
			return Response.ok(respuesta).build();
			
		} catch (Exception e) {
			logger.error("Error al cancelar la entrada: {}", e.getMessage());
			if (txTemp != null && txTemp.isActive()) {
				txTemp.rollback();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al cancelar la entrada: " + e.getMessage()).build();
		} finally {
			if (pmTemp != null && !pmTemp.isClosed()) {
				pmTemp.close();
			}
		}
	}


	@PUT
	@Path("/cambiarEstadoSala/{salaId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response cambiarEstadoSala(@PathParam("salaId") long salaId, Map<String, Boolean> estado) {
		PersistenceManager pmTemp = null;
		Transaction txTemp = null;

		try {
			PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");
			pmTemp = pmf.getPersistenceManager();
			txTemp = pmTemp.currentTransaction();
			txTemp.begin();

			// Obtener la sala por ID
			Sala sala = pmTemp.getObjectById(Sala.class, salaId);
			if (sala == null) {
				logger.info("Sala no encontrada con ID: {}", salaId);
				txTemp.rollback();
				return Response.status(Response.Status.NOT_FOUND).entity("Sala no encontrada").build();
			}

			// Cambiar el estado de la sala
			boolean nuevoEstado = estado.get("disponible");
			sala.setDisponible(nuevoEstado);
			logger.info("Estado de la sala {} cambiado a {}", salaId, nuevoEstado ? "Disponible" : "No Disponible");

			txTemp.commit();
			return Response.ok().build();
		} catch (Exception e) {
			logger.error("Error al cambiar el estado de la sala: {}", e.getMessage());
			if (txTemp != null && txTemp.isActive()) {
				txTemp.rollback();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al cambiar el estado de la sala").build();
		} finally {
			if (pmTemp != null && !pmTemp.isClosed()) {
				pmTemp.close();
			}
		}
	}

	@PUT
	@Path("/actualizarAsientos/{salaId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response actualizarAsientos(@PathParam("salaId") long salaId, List<Asiento> asientosActualizados) {
		try (PersistenceManager pm = JDOHelper.getPersistenceManagerFactory("datanucleus.properties").getPersistenceManager()) {
			Sala sala = pm.getObjectById(Sala.class, salaId);
			if (sala == null) {
				return Response.status(Response.Status.NOT_FOUND).entity("Sala no encontrada").build();
			}

			sala.setAsientos(asientosActualizados);
			return Response.ok().build();
		} catch (Exception e) {
			logger.error("Error al actualizar los asientos: {}", e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al actualizar los asientos").build();
		}
	}


	@POST
	@Path("/agregarSala")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response agregarSala(Sala nuevaSala) {
		try (PersistenceManager pm = JDOHelper.getPersistenceManagerFactory("datanucleus.properties").getPersistenceManager()) {
			// Crear una lista para los asientos
			List<Asiento> asientos = new ArrayList<>();

			// Crear los asientos asociados
			for (int i = 1; i <= nuevaSala.getCapacidad(); i++) {
				TipoAsiento tipo = (i % 10 == 0) ? TipoAsiento.VIP : TipoAsiento.NORMAL; // Ejemplo: cada 10 asientos es VIP
				Asiento asiento = new Asiento(0, i, tipo, false); // Asientos inicialmente libres
				asientos.add(asiento); // Agregar el asiento a la lista
			}

			// Asociar los asientos a la sala
			nuevaSala.setAsientos(asientos);

			// Persistir la sala (junto con los asientos)
			pm.makePersistent(nuevaSala);

			return Response.status(Response.Status.CREATED).entity("Sala y asientos creados correctamente").build();
		} catch (Exception e) {
			logger.error("Error al agregar la sala: {}", e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al agregar la sala").build();
		}
	}

		
	@PUT
	@Path("/actualizarSala/{salaId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response actualizarSala(@PathParam("salaId") long salaId, Map<String, Object> datos) {
		try (PersistenceManager pm = JDOHelper.getPersistenceManagerFactory("datanucleus.properties").getPersistenceManager()) {
			Sala sala = pm.getObjectById(Sala.class, salaId);
			if (sala == null) {
				return Response.status(Response.Status.NOT_FOUND).entity("Sala no encontrada").build();
			}

			// Obtener la nueva capacidad desde los datos
			int nuevaCapacidad = (int) datos.get("capacidad");

			// Obtener los asientos actualizados desde los datos
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> asientosActualizadosData = (List<Map<String, Object>>) datos.get("asientos");
			List<Asiento> asientosActualizados = new ArrayList<>();

			// Convertir los datos de los asientos en objetos Asiento
			for (Map<String, Object> asientoData : asientosActualizadosData) {
				int numero = (int) asientoData.get("numero");
				TipoAsiento tipo = TipoAsiento.valueOf((String) asientoData.get("tipo"));
				boolean ocupado = (boolean) asientoData.get("ocupado");

				Asiento asiento = new Asiento(0, numero, tipo, ocupado);
				asientosActualizados.add(asiento);
			}

			// Validar la capacidad y ajustar los asientos si es necesario
			if (nuevaCapacidad > asientosActualizados.size()) {
				for (int i = asientosActualizados.size() + 1; i <= nuevaCapacidad; i++) {
					TipoAsiento tipo = (i % 10 == 0) ? TipoAsiento.VIP : TipoAsiento.NORMAL;
					Asiento nuevoAsiento = new Asiento(0, i, tipo, false); // Asientos inicialmente libres
					asientosActualizados.add(nuevoAsiento);
				}
			} else if (nuevaCapacidad < asientosActualizados.size()) {
				asientosActualizados = asientosActualizados.subList(0, nuevaCapacidad);
			}

			// Actualizar la sala con la nueva capacidad y los asientos actualizados
			sala.setCapacidad(nuevaCapacidad);
			sala.setAsientos(asientosActualizados);

			return Response.ok("Sala actualizada correctamente").build();
		} catch (Exception e) {
			logger.error("Error al actualizar la sala: {}", e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al actualizar la sala").build();
		}
	}

	/**
	 * Obtiene un resumen de ventas por película
	 * @return Response con los datos de ventas por película
	 */
	@GET
	@Path("/ventasPorPelicula")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVentasPorPelicula() {
		PersistenceManager pmTemp = null;
		Transaction txTemp = null;
		
		try {
			PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");
			pmTemp = pmf.getPersistenceManager();
			txTemp = pmTemp.currentTransaction();
			txTemp.begin();
			
			// Obtener todas las entradas
			Query<Entrada> queryEntradas = pmTemp.newQuery(Entrada.class);
			@SuppressWarnings("unchecked")
			List<Entrada> entradas = (List<Entrada>) queryEntradas.execute();
			
			// Obtener todas las películas
			Query<Pelicula> queryPeliculas = pmTemp.newQuery(Pelicula.class);
			@SuppressWarnings("unchecked")
			List<Pelicula> peliculas = (List<Pelicula>) queryPeliculas.execute();
			
			// Crear un mapa para almacenar los datos de ventas por película
			Map<Long, Map<String, Object>> ventasPorPelicula = new HashMap<>();
			
			// Inicializar el mapa con todas las películas
			for (Pelicula pelicula : peliculas) {
				Map<String, Object> datosPelicula = new HashMap<>();
				datosPelicula.put("id", pelicula.getId());
				datosPelicula.put("titulo", pelicula.getTitulo());
				datosPelicula.put("totalEntradas", 0);
				datosPelicula.put("totalIngresos", 0);
				datosPelicula.put("salaId", pelicula.getSala() != null ? pelicula.getSala().getId() : 0);
				datosPelicula.put("numeroSala", pelicula.getSala() != null ? pelicula.getSala().getNumero() : 0);
				
				ventasPorPelicula.put(pelicula.getId(), datosPelicula);
			}
			
			// Recorrer todas las entradas y sumar por película
			for (Entrada entrada : entradas) {
				// Para cada entrada, necesitamos encontrar la película asociada
				// Esto requiere buscar la sala primero y luego la película
				if (entrada.getCine() != null) {
					for (Sala sala : entrada.getCine().getSalas()) {
						// Buscar la película que está en esta sala
						for (Pelicula pelicula : peliculas) {
							if (pelicula.getSala() != null && pelicula.getSala().getId() == sala.getId()) {
								// Esta entrada corresponde a esta película
								Map<String, Object> datosPelicula = ventasPorPelicula.get(pelicula.getId());
								if (datosPelicula != null) {
									int totalEntradas = (int) datosPelicula.get("totalEntradas");
									int totalIngresos = (int) datosPelicula.get("totalIngresos");
									
									datosPelicula.put("totalEntradas", totalEntradas + 1);
									datosPelicula.put("totalIngresos", totalIngresos + entrada.getPrecio());
								}
								break; // Solo puede haber una película por sala
							}
						}
					}
				}
			}
			
			// Convertir el mapa a una lista para devolver
			List<Map<String, Object>> resultado = new ArrayList<>(ventasPorPelicula.values());
			
			txTemp.commit();
			return Response.ok(resultado).build();
			
		} catch (Exception e) {
			logger.error("Error al obtener ventas por película: {}", e.getMessage());
			if (txTemp != null && txTemp.isActive()) {
				txTemp.rollback();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al obtener ventas por película").build();
		} finally {
			if (pmTemp != null && !pmTemp.isClosed()) {
				pmTemp.close();
			}
		}
	}

	/**
	 * Obtiene un resumen de ventas por tipo de asiento
	 * @return Response con los datos de ventas por tipo de asiento
	 */
	@GET
	@Path("/ventasPorTipoAsiento")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVentasPorTipoAsiento() {
		PersistenceManager pmTemp = null;
		Transaction txTemp = null;
		
		try {
			PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");
			pmTemp = pmf.getPersistenceManager();
			txTemp = pmTemp.currentTransaction();
			txTemp.begin();
			
			// Obtener todas las entradas
			Query<Entrada> queryEntradas = pmTemp.newQuery(Entrada.class);
			@SuppressWarnings("unchecked")
			List<Entrada> entradas = (List<Entrada>) queryEntradas.execute();
			
			// Crear un mapa para almacenar los datos de ventas por tipo de asiento
			Map<TipoAsiento, Map<String, Object>> ventasPorTipoAsiento = new HashMap<>();
			
			// Inicializar el mapa con todos los tipos de asiento
			for (TipoAsiento tipo : TipoAsiento.values()) {
				Map<String, Object> datosTipo = new HashMap<>();
				datosTipo.put("tipo", tipo.name());
				datosTipo.put("totalEntradas", 0);
				datosTipo.put("totalIngresos", 0);
				
				ventasPorTipoAsiento.put(tipo, datosTipo);
			}
			
			// Recorrer todas las entradas y sumar por tipo de asiento
			for (Entrada entrada : entradas) {
				TipoAsiento tipo = entrada.getTipoAsiento();
				if (tipo != null) {
					Map<String, Object> datosTipo = ventasPorTipoAsiento.get(tipo);
					if (datosTipo != null) {
						int totalEntradas = (int) datosTipo.get("totalEntradas");
						int totalIngresos = (int) datosTipo.get("totalIngresos");
						
						datosTipo.put("totalEntradas", totalEntradas + 1);
						datosTipo.put("totalIngresos", totalIngresos + entrada.getPrecio());
					}
				}
			}
			
			// Convertir el mapa a una lista para devolver
			List<Map<String, Object>> resultado = new ArrayList<>(ventasPorTipoAsiento.values());
			
			txTemp.commit();
			return Response.ok(resultado).build();
			
		} catch (Exception e) {
			logger.error("Error al obtener ventas por tipo de asiento: {}", e.getMessage());
			if (txTemp != null && txTemp.isActive()) {
				txTemp.rollback();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al obtener ventas por tipo de asiento").build();
		} finally {
			if (pmTemp != null && !pmTemp.isClosed()) {
				pmTemp.close();
			}
		}
	}

	/**
	 * Obtiene un resumen de ventas por día
	 * @return Response con los datos de ventas por día
	 */
	@GET
	@Path("/ventasPorDia")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVentasPorDia() {
		PersistenceManager pmTemp = null;
		Transaction txTemp = null;
		
		try {
			PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");
			pmTemp = pmf.getPersistenceManager();
			txTemp = pmTemp.currentTransaction();
			txTemp.begin();
			
			// Obtener todas las entradas
			Query<Entrada> queryEntradas = pmTemp.newQuery(Entrada.class);
			@SuppressWarnings("unchecked")
			List<Entrada> entradas = (List<Entrada>) queryEntradas.execute();
			
			// Crear un mapa para almacenar los datos de ventas por día (usando la fecha de estreno de la película como aproximación)
			Map<String, Map<String, Object>> ventasPorDia = new HashMap<>();
			
			// Crear un objeto SimpleDateFormat para formatear las fechas
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			
			// Obtener todas las películas para buscar sus fechas de estreno
			Query<Pelicula> queryPeliculas = pmTemp.newQuery(Pelicula.class);
			@SuppressWarnings("unchecked")
			List<Pelicula> peliculas = (List<Pelicula>) queryPeliculas.execute();
			
			// Usaremos un mapa para relacionar cada sala con su película
			Map<Long, Pelicula> peliculasPorSala = new HashMap<>();
			for (Pelicula pelicula : peliculas) {
				if (pelicula.getSala() != null) {
					peliculasPorSala.put(pelicula.getSala().getId(), pelicula);
				}
			}
			
			// Recorrer todas las entradas
			for (Entrada entrada : entradas) {
				// Para cada entrada, buscamos la sala y luego la película
				if (entrada.getCine() != null) {
					for (Sala sala : entrada.getCine().getSalas()) {
						Pelicula pelicula = peliculasPorSala.get(sala.getId());
						if (pelicula != null && pelicula.getFechaEstreno() != null) {
							String fechaKey = sdf.format(pelicula.getFechaEstreno());
							
							// Verificar si ya existe una entrada para esta fecha
							if (!ventasPorDia.containsKey(fechaKey)) {
								Map<String, Object> datosDia = new HashMap<>();
								datosDia.put("fecha", fechaKey);
								datosDia.put("totalEntradas", 0);
								datosDia.put("totalIngresos", 0);
								
								ventasPorDia.put(fechaKey, datosDia);
							}
							
							// Actualizar los datos de ventas para esta fecha
							Map<String, Object> datosDia = ventasPorDia.get(fechaKey);
							int totalEntradas = (int) datosDia.get("totalEntradas");
							int totalIngresos = (int) datosDia.get("totalIngresos");
							
							datosDia.put("totalEntradas", totalEntradas + 1);
							datosDia.put("totalIngresos", totalIngresos + entrada.getPrecio());
						}
					}
				}
			}
			
			// Convertir el mapa a una lista para devolver
			List<Map<String, Object>> resultado = new ArrayList<>(ventasPorDia.values());
			
			// Ordenar por fecha
			resultado.sort((a, b) -> ((String) a.get("fecha")).compareTo((String) b.get("fecha")));
			
			txTemp.commit();
			return Response.ok(resultado).build();
			
		} catch (Exception e) {
			logger.error("Error al obtener ventas por día: {}", e.getMessage());
			if (txTemp != null && txTemp.isActive()) {
				txTemp.rollback();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al obtener ventas por día").build();
		} finally {
			if (pmTemp != null && !pmTemp.isClosed()) {
				pmTemp.close();
			}
		}
	}

	/**
	 * Obtiene un resumen general de ventas
	 * @return Response con los datos generales de ventas
	 */
	@GET
	@Path("/resumenVentas")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResumenVentas() {
		PersistenceManager pmTemp = null;
		Transaction txTemp = null;
		
		try {
			PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");
			pmTemp = pmf.getPersistenceManager();
			txTemp = pmTemp.currentTransaction();
			txTemp.begin();
			
			// Obtener todas las entradas
			Query<Entrada> queryEntradas = pmTemp.newQuery(Entrada.class);
			@SuppressWarnings("unchecked")
			List<Entrada> entradas = (List<Entrada>) queryEntradas.execute();
			
			// Crear un objeto para almacenar el resumen
			Map<String, Object> resumen = new HashMap<>();
			resumen.put("totalEntradas", entradas.size());
			
			// Calcular el total de ingresos
			int totalIngresos = 0;
			for (Entrada entrada : entradas) {
				totalIngresos += entrada.getPrecio();
			}
			resumen.put("totalIngresos", totalIngresos);
			
			// Calcular el promedio de precio por entrada
			double promedioPrecio = entradas.isEmpty() ? 0 : totalIngresos / (double) entradas.size();
			resumen.put("promedioPrecio", Math.round(promedioPrecio * 100.0) / 100.0);
			
			// Obtener el número de usuarios únicos
			Set<String> usuariosUnicos = new HashSet<>();
			for (Entrada entrada : entradas) {
				if (entrada.getUsuario() != null) {
					usuariosUnicos.add(entrada.getUsuario().getNombreUsuario());
				}
			}
			resumen.put("totalUsuariosUnicos", usuariosUnicos.size());
			
			txTemp.commit();
			return Response.ok(resumen).build();
			
		} catch (Exception e) {
			logger.error("Error al obtener resumen de ventas: {}", e.getMessage());
			if (txTemp != null && txTemp.isActive()) {
				txTemp.rollback();
			}
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al obtener resumen de ventas").build();
		} finally {
			if (pmTemp != null && !pmTemp.isClosed()) {
				pmTemp.close();
			}
		}
	}

}