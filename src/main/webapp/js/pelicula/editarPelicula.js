// Obtener el ID de la película y el nombre de usuario de la URL
const urlParams = new URLSearchParams(window.location.search);
const peliculaId = urlParams.get('id');
const userId = urlParams.get('nombreUsuario');
const apiBaseUrl = 'http://localhost:8080/rest/resource';

// Configurar el enlace para volver a la página de administrador
document.getElementById('volverAdmin').href = "../../html/administrador.html?nombreUsuario=" + userId;

// Cuando se carga la página
window.onload = async function() {
    if (!peliculaId) {
        alert("No se ha especificado ninguna película para editar");
        window.location.href = "../../html/administrador.html?nombreUsuario=" + userId;
        return;
    }

    await cargarDatosPelicula();
    // Cargar las salas disponibles
    await cargarSalas();
    
    // Cargar los datos de la película
    
    
    // Configurar el botón cancelar
    document.getElementById('btnCancelar').addEventListener('click', function() {
        window.location.href = "../../html/administrador.html?nombreUsuario=" + userId;
    });
    
    // Configurar el formulario para enviar los datos
    document.getElementById('formEditarPelicula').addEventListener('submit', guardarCambios);
    document.getElementById('btn-primary').addEventListener('submit', guardarCambios);
};

// Función para cargar las salas disponibles
async function cargarSalas() {
    try {
        const respuesta = await fetch("http://localhost:8080/rest/resource/getSalas", {
            method: "GET",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/json"
            }
        });
        
        if (!respuesta.ok) {
            throw new Error("Error al cargar las salas");
        }
        
        const salas = await respuesta.json();
        const selectSala = document.getElementById('sala');
        
        // Limpiar opciones existentes
        selectSala.innerHTML = "";
        
        // Agregar las salas al select
        salas.forEach(sala => {
            if (sala.disponible) {
                const opcion = document.createElement('option');
                opcion.value = sala.id;
                opcion.textContent = `Sala ${sala.numero} (${sala.capacidad} asientos)`;
                selectSala.appendChild(opcion);
            }
        });
    } catch (error) {
        console.error("Error al cargar salas:", error);
        alert("No se pudieron cargar las salas disponibles");
    }
}

// Función para cargar los datos de la película
async function cargarDatosPelicula() {

    console.log("ID de la película:", peliculaId);
    try {
        const respuesta = await fetch(`http://localhost:8080/rest/resource/getPelicula/${peliculaId}`, {
            method: "GET",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/json"
            }
        });
    
        console.log("Estado de la respuesta:", respuesta.status);
        if (!respuesta.ok) {
            throw new Error("Error al cargar los datos de la película.");
        }
    
        const pelicula = await respuesta.json();
        console.log("Película recibida:", pelicula);
    
        // Rellenar el formulario con los datos de la película
        document.getElementById('peliculaId').value = pelicula.id;
        document.getElementById('titulo').value = pelicula.titulo;
        document.getElementById('genero').value = pelicula.genero;
        document.getElementById('duracion').value = pelicula.duracion;
    
        if (pelicula.fechaEstreno) {
            const fecha = new Date(pelicula.fechaEstreno);
            const fechaFormateada = fecha.toISOString().split('T')[0];
            document.getElementById('fechaEstreno').value = fechaFormateada;
        }
    
        document.getElementById('director').value = pelicula.director;
        document.getElementById('sinopsis').value = pelicula.sinopsis;
        document.getElementById('horario').value = pelicula.horario;
    
        const selectSala = document.getElementById('sala');
        for (let i = 0; i < selectSala.options.length; i++) {
            if (selectSala.options[i].value == pelicula.sala.id) {
                selectSala.selectedIndex = i;
                break;
            }
        }


    } catch (error) {
        console.error("Error al cargar datos de la película:", error);
        alert("No se pudieron cargar los datos de la película");
    }
}
async function guardarCambios(event) {
    event.preventDefault();

    // Obtener el ID de la sala seleccionada
    const idSalaSeleccionada = document.getElementById('sala').value;

    // Obtener los datos de la sala seleccionada desde el backend
    let salaSeleccionada = null;
    try {
        const respuestaSala = await fetch(`${apiBaseUrl}/getSala/${idSalaSeleccionada}`, {
            method: "GET",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/json"
            }
        });

        if (!respuestaSala.ok) {
            throw new Error(`Error al obtener la sala: ${respuestaSala.status}`);
        }

        salaSeleccionada = await respuestaSala.json();
        console.log("Sala seleccionada:", salaSeleccionada);
    } catch (error) {
        console.error("Error al obtener la sala seleccionada:", error);
        alert("No se pudo obtener la sala seleccionada. Por favor, inténtalo de nuevo.");
        return;
    }

    // Recoger los datos del formulario
    const peliculaActualizada = {
        titulo: document.getElementById('titulo').value,
        genero: document.getElementById('genero').value,
        duracion: parseInt(document.getElementById('duracion').value),
        fechaEstreno: document.getElementById('fechaEstreno').value,
        director: document.getElementById('director').value,
        sinopsis: document.getElementById('sinopsis').value,
        horario: document.getElementById('horario').value,
        sala: salaSeleccionada // Incluir la sala seleccionada
    };

    // Imprimir los datos de la película en la consola
    console.log("Datos de la película que se enviarán al backend:", peliculaActualizada);

    // Llamar al backend para actualizar la película
    try {
        const respuesta = await fetch(`${apiBaseUrl}/updatePelicula/${peliculaId}`, {
            method: "PUT",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/json"
            },
            body: JSON.stringify(peliculaActualizada)
        });

        if (!respuesta.ok) {
            throw new Error(`Error al actualizar la película: ${respuesta.status}`);
        }

        alert("Película actualizada correctamente");
        window.location.href = "../../html/administrador.html?nombreUsuario=" + userId;
    } catch (error) {
        console.error("Error al guardar cambios:", error);
        alert("No se pudo actualizar la película");
    }
}

async function actualizarResenya(resenyaId, resenya) {
    try {
        const response = await fetch(`${apiBaseUrl}/updatePelicula/${resenyaId}`, {
            method: 'PUT',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(resenya)
        });
        
        if (!response.ok) {
            throw new Error(`Error al actualizar reseña: ${response.status}`);
        }
        
        const resenyaActualizada = await response.json();
        return resenyaActualizada;
    } catch (error) {
        console.error('Error al actualizar reseña:', error);
        throw error;
    }
}