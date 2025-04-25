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
                    <i onClick="editarPelicula(${pelicula.id})" class="material-icons button edit">edit</i>
                    <i onClick="eliminarPelicula(${pelicula.id})"class="material-icons button delete">delete</i>
                </td>
            </tr>
        `;
    });
    
    tablaBody.innerHTML = contenidoTabla;
}

let eliminarPelicula = async (id ) => {
    const peticion = await fetch("http://localhost:8080/rest/resource/eliminarPelicula/"+id,
    {
        method: "DELETE",
        headers: {
            "Acept": "application/json",
            "Content-Type": "application/json"
        }
    });

    listarPeliculas();
}

let editarPelicula = (id) => {
    // Obtener el parámetro nombreUsuario de la URL actual
    const urlParams = new URLSearchParams(window.location.search);
    const nombreUsuario = urlParams.get('nombreUsuario');
    
    // Redireccionar a la página de edición con los parámetros necesarios
    window.location.href = `../html/pelicula/editarPelicula.html?id=${id}&nombreUsuario=${nombreUsuario}`;
}