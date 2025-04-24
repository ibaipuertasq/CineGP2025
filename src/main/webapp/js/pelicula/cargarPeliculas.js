// Obtener el nombre de usuario de la URL
const peliculasUrlParams = new URLSearchParams(window.location.search);
const peliculasUserId = peliculasUrlParams.get('nombreUsuario');

// Cargar películas al iniciar la página
document.addEventListener('DOMContentLoaded', function() {
    listarPeliculas();
});

// Función para listar todas las películas
async function listarPeliculas() {
    try {
        const response = await fetch("http://localhost:8080/rest/resource/getPeliculas", {
            method: "GET",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/json"
            }
        });
        
        if (!response.ok) {
            throw new Error(`Error al cargar películas: ${response.status}`);
        }
        
        const peliculas = await response.json();
        mostrarPeliculasEnTabla(peliculas);
        
    } catch (error) {
        console.error('Error al cargar películas:', error);
        document.getElementById('tablaPeliculasBody').innerHTML = `
            <tr>
                <td colspan="6">Error al cargar películas: ${error.message}</td>
            </tr>
        `;
    }
}

// Función para mostrar las películas en la tabla
function mostrarPeliculasEnTabla(peliculas) {
    const tablaBody = document.getElementById('tablaPeliculasBody');
    
    if (peliculas.length === 0) {
        tablaBody.innerHTML = `
            <tr>
                <td colspan="6">No hay películas en cartelera actualmente</td>
            </tr>
        `;
        return;
    }
    
    let contenidoTabla = '';
    
    peliculas.forEach(pelicula => {
        const peliculaJson = JSON.stringify(pelicula)
            .replace(/"/g, '&quot;') // Escapar comillas para evitar problemas en los atributos HTML
            .replace(/'/g, '&#39;'); // Escapar comillas simples también
        
        contenidoTabla += `
            <tr>
                <td>${pelicula.titulo}</td>
                <td>${pelicula.genero}</td>
                <td>${pelicula.duracion} min</td>
                <td>${pelicula.sala ? pelicula.sala.numero : 'No asignada'}</td>
                <td>${pelicula.horario || 'No disponible'}</td>
                <td>
                    <button onclick="mostrarDetallesPelicula(${peliculaJson})" class="btn-detalles">
                        Ver detalles
                    </button>
                    <button onclick="comprarEntradas(${pelicula.id})" class="btn-detalles" style="background-color: #4CAF50;">
                        Comprar
                    </button>
                </td>
            </tr>
        `;
    });
    
    tablaBody.innerHTML = contenidoTabla;
}

// Función para mostrar detalles de la película en el modal
function mostrarDetallesPelicula(pelicula) {
    // Llenar el modal con los datos de la película
    document.getElementById('modalTitulo').textContent = pelicula.titulo;
    document.getElementById('modalGenero').textContent = pelicula.genero || 'No especificado';
    document.getElementById('modalDuracion').textContent = pelicula.duracion || 'No especificado';
    document.getElementById('modalDirector').textContent = pelicula.director || 'No especificado';
    
    // Formatear la fecha si existe
    if (pelicula.fechaEstreno) {
        const fecha = new Date(pelicula.fechaEstreno);
        document.getElementById('modalFechaEstreno').textContent = fecha.toLocaleDateString();
    } else {
        document.getElementById('modalFechaEstreno').textContent = 'No especificada';
    }
    
    // Información de sala y horario
    document.getElementById('modalSala').textContent = pelicula.sala ? pelicula.sala.numero : 'No asignada';
    document.getElementById('modalHorario').textContent = pelicula.horario || 'No disponible';
    document.getElementById('modalSinopsis').textContent = pelicula.sinopsis || 'No disponible';
    
    // Guardar ID de la película en el botón de compra
    document.getElementById('btnComprarPelicula').dataset.peliculaId = pelicula.id;
    
    // Mostrar el modal
    document.getElementById('peliculaModal').style.display = 'block';
}

// Función para ir a la página de compra de entradas
function comprarEntradas(peliculaId) {
    window.location.href = `entrada/comprarEntradas.html?nombreUsuario=${peliculasUserId}&peliculaId=${peliculaId}`;
}