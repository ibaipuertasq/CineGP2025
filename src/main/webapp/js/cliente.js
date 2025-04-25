// Variables globales
const urlParams = new URLSearchParams(window.location.search);
const userId = urlParams.get('nombreUsuario');

// Inicialización de la página
document.addEventListener('DOMContentLoaded', function() {
    // Verificar autenticación del usuario
    if (!userId) {
        console.error("No se encontró el parámetro 'nombreUsuario' en la URL.");
        window.location.href = '../index.html';
        return;
    }
    
    // Mostrar el nombre de usuario en el dropdown
    document.getElementById('userName').textContent = userId;
    
    // Configurar eventos de la interfaz
    configurarEventos();
    
    // Cargar datos de usuario para personalizar la experiencia
    cargarDatosUsuario();
    
    // Comprobar si debemos desplazarnos a la sección entradas (desde url hash)
    if (window.location.hash === '#entradas') {
        setTimeout(() => {
            document.querySelector('#entradas').scrollIntoView({ behavior: 'smooth' });
        }, 500);
    }
});

// Configurar eventos de la página
function configurarEventos() {
    // Evento para editar usuario
    document.getElementById('editarUsuario').addEventListener('click', function(e) {
        e.preventDefault();
        window.location.href = `usuario/editarUsuario.html?nombreUsuario=${userId}`;
    });
    
    // Evento para el botón de comprar entradas
    document.getElementById('btnComprarEntradas').addEventListener('click', function() {
        window.location.href = `entrada/comprarEntradas.html?nombreUsuario=${userId}`;
    });
    
    // Evento para el botón de ver todas las entradas
    document.getElementById('btnVerTodasEntradas').addEventListener('click', function() {
        window.location.href = `entrada/verEntradas.html?nombreUsuario=${userId}`;
    });
    
    // Configurar eventos para el modal de película
    const modal = document.getElementById('peliculaModal');
    const span = document.getElementsByClassName('close')[0];
    
    // Cerrar modal al hacer clic en la X
    span.onclick = function() {
        modal.style.display = 'none';
    };
    
    // Cerrar modal al hacer clic fuera
    window.onclick = function(event) {
        if (event.target == modal) {
            modal.style.display = 'none';
        }
    };
    
    // Botón para comprar entradas en el modal
    document.getElementById('btnComprarPelicula').addEventListener('click', function() {
        const peliculaId = this.dataset.peliculaId;
        if (peliculaId) {
            window.location.href = `entrada/comprarEntradas.html?nombreUsuario=${userId}&peliculaId=${peliculaId}`;
        }
    });
}

// Cargar datos del usuario desde el backend
async function cargarDatosUsuario() {
    try {
        const response = await fetch(`http://localhost:8080/rest/resource/getUsuario/${userId}`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error(`Error al cargar datos de usuario: ${response.status}`);
        }
        
        const usuario = await response.json();
        
        
        // Cargar datos adicionales: entradas y recomendaciones personalizadas
        cargarEntradasUsuario();
        
    } catch (error) {
        console.error('Error:', error);
        // No redireccionamos al login para evitar bucles infinitos si hay un error temporal
        mostrarMensajeError('Error al cargar los datos del usuario. Por favor, recarga la página o inicia sesión nuevamente.');
    }
}

// Mostrar un mensaje de error en la página
function mostrarMensajeError(mensaje) {
    // Verificar si ya existe un elemento de alerta
    let alertaElement = document.getElementById('alertaGlobal');
    
    if (!alertaElement) {
        // Crear el elemento si no existe
        alertaElement = document.createElement('div');
        alertaElement.id = 'alertaGlobal';
        alertaElement.className = 'alerta-error';
        
        // Insertar después del header
        const headerElement = document.querySelector('header');
        headerElement.parentNode.insertBefore(alertaElement, headerElement.nextSibling);
    }
    
    // Actualizar el mensaje
    alertaElement.textContent = mensaje;
    
    // Mostrar el mensaje
    alertaElement.style.display = 'block';
    
    // Ocultar después de 5 segundos
    setTimeout(() => {
        alertaElement.style.display = 'none';
    }, 5000);
}

// Agregar opciones específicas para administradores
function agregarOpcionesAdministrador() {
    // Agregar opciones al menú de usuario
    const dropdownContent = document.querySelector('.dropdown-content');
    
    // Añadir opciones de administración antes del último elemento (cerrar sesión)
    const cerrarSesion = dropdownContent.lastElementChild;
    
    // Opción para gestionar películas
    const gestionPeliculas = document.createElement('button');
    gestionPeliculas.type = 'button';
    gestionPeliculas.textContent = 'Gestionar películas';
    gestionPeliculas.addEventListener('click', function() {
        window.location.href = `pelicula/gestionarPeliculas.html?nombreUsuario=${userId}`;
    });
    
    // Opción para gestionar salas
    const gestionSalas = document.createElement('button');
    gestionSalas.type = 'button';
    gestionSalas.textContent = 'Gestionar salas';
    gestionSalas.addEventListener('click', function() {
        window.location.href = `sala/gestionarSalas.html?nombreUsuario=${userId}`;
    });
    
    // Opción para gestionar usuarios
    const gestionUsuarios = document.createElement('button');
    gestionUsuarios.type = 'button';
    gestionUsuarios.textContent = 'Gestionar usuarios';
    gestionUsuarios.addEventListener('click', function() {
        window.location.href = `admin/gestionarUsuarios.html?nombreUsuario=${userId}`;
    });
    
    // Insertar antes de cerrar sesión
    dropdownContent.insertBefore(gestionPeliculas, cerrarSesion);
    dropdownContent.insertBefore(gestionSalas, cerrarSesion);
    dropdownContent.insertBefore(gestionUsuarios, cerrarSesion);
    
    // Añadir botón para crear nueva película en la sección de cartelera
    const seccionCartelera = document.querySelector('#eventos');
    const contenedorBotones = seccionCartelera.querySelector('.button-container');
    
    // Si no existe el contenedor de botones, lo creamos
    if (!contenedorBotones) {
        const nuevoContenedor = document.createElement('div');
        nuevoContenedor.className = 'button-container';
        seccionCartelera.appendChild(nuevoContenedor);
        
        const btnNuevaPelicula = document.createElement('button');
        btnNuevaPelicula.className = 'boton';
        btnNuevaPelicula.type = 'button';
        btnNuevaPelicula.textContent = 'Añadir nueva película';
        btnNuevaPelicula.addEventListener('click', function() {
            window.location.href = `pelicula/crearPelicula.html?nombreUsuario=${userId}`;
        });
        
        nuevoContenedor.appendChild(btnNuevaPelicula);
    } else {
        // Si ya existe, añadimos un botón adicional
        const btnNuevaPelicula = document.createElement('button');
        btnNuevaPelicula.className = 'boton';
        btnNuevaPelicula.style.marginLeft = '10px';
        btnNuevaPelicula.type = 'button';
        btnNuevaPelicula.textContent = 'Añadir nueva película';
        btnNuevaPelicula.addEventListener('click', function() {
            window.location.href = `pelicula/crearPelicula.html?nombreUsuario=${userId}`;
        });
        
        contenedorBotones.appendChild(btnNuevaPelicula);
    }
}

// Cargar entradas del usuario para mostrar en la sección de entradas
async function cargarEntradasUsuario() {
    try {
        const response = await fetch(`http://localhost:8080/rest/resource/getEntradas/${userId}`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error(`Error al cargar entradas: ${response.status}`);
        }
        
        const entradas = await response.json();
        mostrarEntradasEnTabla(entradas);
        
    } catch (error) {
        console.error('Error al cargar entradas:', error);
        
        // Mostrar mensaje de error en la tabla
        const tablaBody = document.getElementById('tablaEntradasBody');
        if (tablaBody) {
            tablaBody.innerHTML = `
                <tr>
                    <td colspan="5" class="text-center error-message">
                        Error al cargar tus entradas. Por favor, recarga la página.
                    </td>
                </tr>
            `;
        }
    }
}

// Mostrar entradas en la tabla de la sección Mis Entradas
function mostrarEntradasEnTabla(entradas) {
    const tablaBody = document.getElementById('tablaEntradasBody');
    
    if (!entradas || entradas.length === 0) {
        tablaBody.innerHTML = `
            <tr>
                <td colspan="5" class="text-center">
                    No tienes entradas compradas. ¡Compra tu primera entrada ahora!
                </td>
            </tr>
        `;
        return;
    }
    
    let html = '';
    
    // Mostrar solo las 5 entradas más recientes
    const entradasRecientes = entradas.slice(0, 5);
    
    entradasRecientes.forEach(entrada => {
        const cineNombre = entrada.cine && entrada.cine.nombre ? entrada.cine.nombre : 'CineGP';
        
        html += `
            <tr>
                <td>${entrada.id}</td>
                <td>${cineNombre}</td>
                <td>Asiento ${entrada.asiento} (${entrada.tipoAsiento})</td>
                <td>${entrada.precio} €</td>
                <td>
                    <button class="btn-detalles" onclick="verDetallesEntrada(${entrada.id})">
                        <span class="material-icons">visibility</span>
                    </button>
                    <button class="btn-detalles" style="background-color: #f44336;" onclick="confirmarCancelarEntrada(${entrada.id})">
                        <span class="material-icons">close</span>
                    </button>
                </td>
            </tr>
        `;
    });
    
    tablaBody.innerHTML = html;
}

// Ver detalles de una entrada
function verDetallesEntrada(entradaId) {
    window.location.href = `entrada/verEntradas.html?nombreUsuario=${userId}&entradaId=${entradaId}`;
}

// Confirmar cancelación de entrada
function confirmarCancelarEntrada(entradaId) {
    if (confirm("¿Estás seguro de que deseas cancelar esta entrada? Esta acción no se puede deshacer.")) {
        cancelarEntrada(entradaId);
    }
}

// Cancelar entrada
async function cancelarEntrada(entradaId) {
    try {
        const response = await fetch(`http://localhost:8080/rest/resource/cancelarEntrada/${entradaId}`, {
            method: 'DELETE',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Error al cancelar la entrada: ${response.status} - ${errorText}`);
        }
        
        alert("Entrada cancelada con éxito");
        
        // Recargar las entradas
        cargarEntradasUsuario();
    } catch (error) {
        console.error('Error al cancelar entrada:', error);
        alert(`Error al cancelar la entrada: ${error.message}`);
    }
}