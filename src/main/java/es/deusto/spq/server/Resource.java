package es.deusto.spq.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import es.deusto.spq.server.jdo.Asiento;
import es.deusto.spq.server.jdo.Cine;
import es.deusto.spq.server.jdo.Entrada;
import es.deusto.spq.server.jdo.Pelicula;
import es.deusto.spq.server.jdo.Sala;
import es.deusto.spq.server.jdo.TipoAsiento;
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
					peli.setHorario(pelicula.getHorario());
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
			salas[i] = new Sala(i + 1, i + 1, capacidad, asientos);
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
			queryEntradas.setParameters(nombreUsuario);
			
			@SuppressWarnings("unchecked")
			List<Entrada> entradas = (List<Entrada>) queryEntradas.execute();
			
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
}