window.onload = function() {
    listarPeliculas();
}


let listarPeliculas = async () => {
    const peticion = await fetch("http://localhost:8080/rest/resource/getPeliculas",
    {
        method: "GET",
        headers: {
            "Accept": "application/json",
            "Content-Type": "application/json"
        }
    });

    const peliculas = await peticion.json();

    let contenidoTabla = "";

    if(peliculas.length > 0) {
        for(let pelicula of peliculas) {
            let contenidoFila = 
            `<tr>
                <td>${pelicula.titulo}</td>
                <td>${pelicula.genero}</td>
                <td>${pelicula.duracion}</td>
                <td>${pelicula.sala.numero}</td>
                <td>${pelicula.horario}</td>
                <td>
                    <button onclick="mostrarDetallesPelicula(${JSON.stringify(pelicula).replace(/"/g, '&quot;')})" class="boton-info">Más info</button>
                </td>
            <tr>`
    
            contenidoTabla += contenidoFila;
        }
    } else {
        contenidoTabla += 
        `<tr>
            <td colspan="6">No hay películas registradas</td>
        </tr>`
    }

    document.querySelector("#tablaPeliculasBody").innerHTML = contenidoTabla;
}

// Función para mostrar el modal con los detalles de la película
function mostrarDetallesPelicula(pelicula) {
    // Llenar el modal con la información de la película
    document.getElementById('modalTitulo').textContent = pelicula.titulo;
    document.getElementById('modalGenero').textContent = pelicula.genero;
    document.getElementById('modalDuracion').textContent = pelicula.duracion;
    document.getElementById('modalDirector').textContent = pelicula.director;
    
    // Formatear la fecha de estreno
    let fechaEstreno = new Date(pelicula.fechaEstreno);
    let fechaFormateada = fechaEstreno.toLocaleDateString();
    document.getElementById('modalFechaEstreno').textContent = fechaFormateada;
    
    document.getElementById('modalSala').textContent = pelicula.sala.numero;
    document.getElementById('modalHorario').textContent = pelicula.horario;
    document.getElementById('modalSinopsis').textContent = pelicula.sinopsis;
    
    // Mostrar el modal
    const modal = document.getElementById('peliculaModal');
    modal.style.display = 'block';
}

// Función para cerrar el modal
document.querySelector('.close').addEventListener('click', () => {
    document.getElementById('peliculaModal').style.display = 'none';
});

// Cerrar el modal al hacer clic fuera de él
window.addEventListener('click', (event) => {
    const modal = document.getElementById('peliculaModal');
    if (event.target === modal) {
        modal.style.display = 'none';
    }
});