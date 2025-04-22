package es.deusto.spq.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

}


/*

@GET
@Path("/getHorarios/{peliculaId}")
@Produces(MediaType.APPLICATION_JSON)
public Response getHorarios(@PathParam("peliculaId") Long peliculaId) {
    try (PersistenceManager pm = JDOHelper.getPersistenceManagerFactory("datanucleus.properties").getPersistenceManager()) {
        // Busca la película por su ID
        Pelicula pelicula = pm.getObjectById(Pelicula.class, peliculaId);

        if (pelicula != null) {
            // Devuelve los horarios asociados a la película
            List<Horario> horarios = pelicula.getHorarios();
            return Response.ok(horarios).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Película no encontrada").build();
        }
    } catch (Exception e) {
        logger.error("Error al obtener los horarios: {}", e.getMessage());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Error al obtener los horarios").build();
    }
}


	/**
	 * This method is used to handle the request for buying a ticket.
	 * It creates a new ticket based on the provided peli ID, tipoAsiento, and quantity.
	 * If the ticket already exists, it returns an unauthorized response.
	 * Otherwise, it creates the ticket, persists it, and returns a success
	 * response.
	 *
	 * @param peliId  The ID of the peli for which the ticket is being purchased.
	 * @param tipoAsiento   The tipoAsiento of the peli where the ticket will be located.
	 * @param cantidad The quantity of tickets being purchased.
	 * @return A Response object indicating the success or failure of the ticket
	 *         purchase.
	 */ /* 
	@SuppressWarnings("null")
	@GET
	@Path("/comprarEntrada/{idPelicula}/{tipoAsiento}/{cantidad}")
	public Response comprarEntrada(@PathParam("idPelicula") String peliId, @PathParam("tipoAsiento") String tipoAsiento,
			@PathParam("cantidad") String cantidad) {
		try {
			tx.begin();

			Entrada ticket = null;
			Pelicula peli = null;

			try {
				peli = pm.getObjectById(Pelicula.class, peliId);
			} catch (javax.jdo.JDOObjectNotFoundException jonfe) {
				logger.info("Exception launched: {}", jonfe.getMessage());
			}

			Entrada entrada = null;

			System.out.println(tipoAsiento);

			if (TipoAsiento.VIP.toString().equals(tipoAsiento.toUpperCase())) {
				//entrada = new Entrada(usuario, peli, 100, TipoAsiento.VIP);	// usuario, cine, precio, asiento, tipoAsiento
			} else if (TipoAsiento.NORMAL.toString().equals(tipoAsiento.toUpperCase())) {
				//entrada = new Entrada(usuario, peli, 20, TipoAsiento.NORMAL);
			} else if (TipoAsiento.DISCAPACITADOS.toString().equals(tipoAsiento.toUpperCase())) {
				//entrada = new Entrada(usuario, peli, 30, TipoAsiento.DISCAPACITADOS);
			} 

			try {
				ticket = pm.getObjectById(Entrada.class, entrada.getId());
			} catch (javax.jdo.JDOObjectNotFoundException jonfe) {
				logger.info("Exception launched: {}", jonfe.getMessage());
			}

			if (ticket != null) {
				logger.info("Ticket already exists!");
				tx.rollback();
				return Response.status(Response.Status.UNAUTHORIZED).entity("Ticket already exists").build();
			} else {
				if (tipoAsientoesPelicula.VIP.toString().equals(tipoAsiento.toUpperCase())) {
					ticket = new Entrada(null, peli, 100, tipoAsientoesPelicula.VIP);
					Mensaje mensaje = new Mensaje();
					if (usuario.getTelefono().contains("+34")) {
						mensaje.setTelefono(usuario.getTelefono());
						mensaje.setMensaje("¡Hola! Has comprado una entrada VIP para el Pelicula " + peli.getNombre()
								+ " en la fecha " + peli.getFecha() + " en " + peli.getLugar()
								+ ". ¡Disfruta del Pelicula!");
						sendMSG(mensaje);
					} else {
						mensaje.setTelefono("+34" + usuario.getTelefono());
						mensaje.setMensaje("¡Hola! Has comprado una entrada VIP para el Pelicula " + peli.getNombre()
								+ " en la fecha " + peli.getFecha() + " en " + peli.getLugar()
								+ ". ¡Disfruta del Pelicula!");
						sendMSG(mensaje);
					}
				} else if (tipoAsientoesPelicula.GRADA_ALTA.toString().equals(tipoAsiento.toUpperCase())) {
					ticket = new Entrada(null, peli, 20, tipoAsientoesPelicula.GRADA_ALTA);
					Mensaje mensaje = new Mensaje();
					if (usuario.getTelefono().contains("+34")) {
						mensaje.setTelefono(usuario.getTelefono());
						mensaje.setMensaje("¡Hola! Has comprado una entrada en el tipoAsiento GRADA ALTA para el Pelicula "
								+ peli.getNombre() + " en la fecha " + peli.getFecha() + " en " + peli.getLugar()
								+ ". ¡Disfruta del Pelicula!");
						sendMSG(mensaje);
					} else {
						mensaje.setTelefono("+34" + usuario.getTelefono());
						mensaje.setMensaje("¡Hola! Has comprado una entrada en el tipoAsiento GRADA ALTA para el Pelicula "
								+ peli.getNombre() + " en la fecha " + peli.getFecha() + " en " + peli.getLugar()
								+ ". ¡Disfruta del Pelicula!");
						sendMSG(mensaje);
					}
				} else if (tipoAsientoesPelicula.GRADA_MEDIA.toString().equals(tipoAsiento.toUpperCase())) {
					ticket = new Entrada(null, peli, 30, tipoAsientoesPelicula.GRADA_MEDIA);
					Mensaje mensaje = new Mensaje();
					if (usuario.getTelefono().contains("+34")) {
						mensaje.setTelefono(usuario.getTelefono());
						mensaje.setMensaje("¡Hola! Has comprado una entrada en el tipoAsiento GRADA MEDIA para el Pelicula "
								+ peli.getNombre() + " en la fecha " + peli.getFecha() + " en " + peli.getLugar()
								+ ". ¡Disfruta del Pelicula!");
						sendMSG(mensaje);
					} else {
						mensaje.setTelefono("+34" + usuario.getTelefono());
						mensaje.setMensaje("¡Hola! Has comprado una entrada en el tipoAsiento GRADA MEDIA para el Pelicula "
								+ peli.getNombre() + " en la fecha " + peli.getFecha() + " en " + peli.getLugar()
								+ ". ¡Disfruta del Pelicula!");
						sendMSG(mensaje);
					}
				} 

				logger.info("Creating ticket: {}", ticket);

				pm.makePersistent(ticket);
				logger.info("Ticket created: {}", ticket);
				tx.commit();
				return Response.ok().build();
			}
		} finally {
			if (tx.isActive()) {
				tx.rollback();
			}

		}

	}

	 */