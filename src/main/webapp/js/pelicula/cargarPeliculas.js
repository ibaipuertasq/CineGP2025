// Agregar al inicio del archivo cargarPeliculas.js
document.addEventListener('DOMContentLoaded', function() {
    // Mostrar el nombre de usuario en la interfaz
    if (userId) {
        document.getElementById('userName').textContent = userId;
    }

    // Cargar las películas al cargar la página
    cargarPeliculas();
    
    // Verificar si venimos de crear una película
    const refresh = urlParams.get('refresh');
    if (refresh === 'true') {
        // Eliminar el parámetro de refresh de la URL sin recargar la página
        const newUrl = window.location.pathname + '?nombreUsuario=' + userId;
        window.history.replaceState({}, document.title, newUrl);
    }
});

// Función para cargar las películas desde el servidor
async function cargarPeliculas() {
    try {
        const response = await fetch('http://localhost:8080/rest/resource/getPeliculas', {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        });

        if (!response.ok) {
            throw new Error('Error al cargar las películas');
        }

        const peliculas = await response.json();
        mostrarPeliculas(peliculas);
    } catch (error) {
        console.error('Error al cargar las películas:', error);
        document.getElementById('tablaPeliculasBody').innerHTML = 
            `<tr><td colspan="5">Error al cargar las películas. Por favor, recarga la página.</td></tr>`;
    }
}

// Función para mostrar las películas en la tabla
function mostrarPeliculas(peliculas) {
    const tablaPeliculasBody = document.getElementById('tablaPeliculasBody');
    tablaPeliculasBody.innerHTML = '';

    if (peliculas && peliculas.length > 0) {
        peliculas.forEach(pelicula => {
            const row = document.createElement('tr');
            
            row.innerHTML = `
                <td>${pelicula.titulo}</td>
                <td>${pelicula.genero}</td>
                <td>${pelicula.duracion} min</td>
                <td>${pelicula.sala ? `Sala ${pelicula.sala.numero}` : 'No asignada'}</td>
                <td>${pelicula.horario || 'No disponible'}</td>
            `;
            
            tablaPeliculasBody.appendChild(row);
        });
    } else {
        tablaPeliculasBody.innerHTML = 
            `<tr><td colspan="5">No hay películas disponibles actualmente.</td></tr>`;
    }
}