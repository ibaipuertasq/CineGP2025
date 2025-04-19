package es.deusto.spq.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import es.deusto.spq.server.jdo.Pelicula;
import es.deusto.spq.server.jdo.TipoUsuario;





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

        // Si el usuario existe, actualiza sus datos
        if (user != null) {
            user.setNombre(usuario.getNombre());
            user.setApellidos(usuario.getApellidos());
            user.setEmail(usuario.getEmail());
            user.setNombreUsuario(usuario.getNombreUsuario());
            user.setContrasenya(usuario.getContrasenya());
            user.setDireccion(usuario.getDireccion());
            user.setTelefono(usuario.getTelefono());
            user.setTipoUsuario(usuario.getTipoUsuario());

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
	@POST
	@Path("/actualizarPelicula")
	public Response actualizarpelicula(Pelicula pelicula) {
		try {
			tx.begin();

			Pelicula peli = null;

			try {
				peli = pm.getObjectById(Pelicula.class, pelicula.getId());
			} catch (javax.jdo.JDOObjectNotFoundException jonfe) {
				logger.info("Exception launched: {}", jonfe.getMessage());
			}

			if (peli != null) {
				Response response = eliminarpelicula(String.valueOf(peli.getId()));
				if (response.getStatus() == 200) {
					peli.setTitulo(pelicula.getTitulo());
					peli.setGenero(pelicula.getGenero());
					peli.setDuracion(pelicula.getDuracion());
					peli.setFechaEstreno(pelicula.getFechaEstreno());
					peli.setDirector(pelicula.getDirector());
					peli.setSinopsis(pelicula.getSinopsis());
					peli.setHorarios(pelicula.getHorarios());
					peli.setSala(pelicula.getSala());

					logger.info("peli updated: {}", peli);
					tx.commit();
					return Response.ok().build();
				} else {
					logger.info("peli not found");
					tx.rollback();
					return Response.status(Response.Status.NOT_FOUND).entity("peli not deleted").build();
				}
			} else {
				logger.info("peli not found");
				tx.rollback();
				return Response.status(Response.Status.NOT_FOUND).entity("peli not found").build();
			}
		} finally {
			if (tx.isActive()) {
				tx.rollback();
			}
			pm.close();
		}
	}
	
	/**
	 * Retrieves a list of events.
	 * 
	 * @return a Response object containing the list of events if found, or an
	 *         unauthorized status with an error message if no events are found.
	 */
	@GET
	@Path("/getPelicula")
	public Response getPelicula() {
		try {
			tx.begin();
			Query<Pelicula> query = pm.newQuery(Pelicula.class);

			@SuppressWarnings("unchecked")
			List<Pelicula> peliculas = (List<Pelicula>) query.execute();

			if (peliculas != null) {
				logger.info("{} events found", peliculas.size());
				tx.commit();
				return Response.ok(peliculas).build();
			} else {
				logger.info("No events found");
				tx.rollback();
				return Response.status(Response.Status.UNAUTHORIZED).entity("No events found").build();
			}
		} finally {
			if (tx.isActive()) {
				tx.rollback();
			}
			pm.close();
		}

	}

}