window.onload = function() {
    //cargarPeliculas();
    cargarPeliculas2();
}

document.addEventListener('DOMContentLoaded', () => {
    cargarPeliculas2(); 
});

peliculasSelect = document.getElementById('peliculas');

peliculasSelect.addEventListener('change', async () => {
    const peliculaId = peliculasSelect.value;

    if (!peliculaId) {
        fechaInput.value = ''; // Limpia el campo de fecha si no hay película seleccionada
        return;
    }

    try {
        // Realiza una solicitud al backend para obtener los horarios de la película seleccionada
        const response = await fetch(`/rest/resource/getHorarios/${peliculaId}`, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        });

        if (response.ok) {
            const horarios = await response.json();

            // Limpia el campo de fecha
            fechaInput.value = '';

            // Si hay horarios disponibles, muestra el primer horario como predeterminado
            if (horarios.length > 0) {
                fechaInput.value = horarios[0].fecha; // Asume que `fecha` es un campo en el objeto `Horario`
            } else {
                alert('No hay horarios disponibles para esta película.');
            }
        } else {
            console.error('Error al obtener los horarios:', await response.text());
        }
    } catch (error) {
        console.error('Error al conectar con el servidor:', error);
    }
});


cargarPeliculas = async () => {
    const peliculasSelect = document.getElementById('peliculas');

    try {
        // Realiza una solicitud al backend para obtener las películas
        const response = await fetch('/rest/resource/getPeliculas', {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        });

        if (response.ok) {
            const peliculas = await response.json();

            // Rellena el <select> con las películas obtenidas
            peliculas.forEach(pelicula => {
                const option = document.createElement('option');
                option.value = pelicula.id; // Usa el ID de la película como valor
                option.textContent = pelicula.nombre; // Usa el nombre de la película como texto
                peliculasSelect.appendChild(option);
                console.log(`Opción añadida: ${pelicula.nombre} (ID: ${pelicula.id})`);
            });
        } else {
            console.error('Error al obtener las películas:', await response.text());
        }
    } catch (error) {
        console.error('Error al conectar con el servidor:', error);
    }
};






let cargarPeliculas2 = async () => {
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
                <td>${pelicula.id}</td>
                <td>${pelicula.titulo}</td>
                <td>${pelicula.fechaEstreno}</td>
                <td>${pelicula.idSala}</td>
                <td>
                    <i class="material-icons button edit">edit</i>
                    <i onClick="eliminarEvento(${pelicula.id})"class="material-icons button delete">delete</i>
                </td>
            <tr>`
    
            contenidoTabla += contenidoFila;
        }
    } else {
        contenidoTabla += 
        `<tr>
            <td colspan="5">No hay peliculas registrados</td>
        </tr>`
    }

    document.querySelector("#tabla tbody").outerHTML = contenidoTabla; 
}



cargarAsientosDisponibles(idSala, horario) = async () => {
    const asientosSelect = document.getElementById('asientosDisponibles');
    asientosSelect.innerHTML = ''; // Limpiar opciones anteriores

    try {
        // Realiza una solicitud al backend para obtener los asientos disponibles
        const response = await fetch(`/rest/resource/getAsientosDisponibles/${idSala}/${horario}`, {
            method: 'GET',
            headers: { 'Content-Type': 'application/json' }
        });

        if (response.ok) {
            const asientos = await response.json();

            // Rellena el <select> con los asientos disponibles
            asientos.forEach(asiento => {
                const option = document.createElement('option');
                option.value = asiento.id; // Usa el ID del asiento como valor
                option.textContent = `Asiento ${asiento.numero}`; // Usa el número del asiento como texto
                asientosSelect.appendChild(option);
            });
        } else {
            console.error('Error al obtener los asientos:', await response.text());
        }
    } catch (error) {
        console.error('Error al conectar con el servidor:', error);
    }
}



