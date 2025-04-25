// Obtener el ID de la película y el nombre de usuario de la URL
const urlParams = new URLSearchParams(window.location.search);
const peliculaId = urlParams.get('id');
const userId = urlParams.get('nombreUsuario');

// Configurar el enlace para volver a la página de administrador
document.getElementById('volverAdmin').href = "../../html/administrador.html?nombreUsuario=" + userId;

// Cuando se carga la página
window.onload = async function() {
    if (!peliculaId) {
        alert("No se ha especificado ninguna película para editar");
        window.location.href = "../../html/administrador.html?nombreUsuario=" + userId;
        return;
    }

    // Cargar las salas disponibles
    await cargarSalas();
    
    // Cargar los datos de la película
    await cargarDatosPelicula();
    
    // Configurar el botón cancelar
    document.getElementById('btnCancelar').addEventListener('click', function() {
        window.location.href = "../../html/administrador.html?nombreUsuario=" + userId;
    });
    
    // Configurar el formulario para enviar los datos
    document.getElementById('formEditarPelicula').addEventListener('submit', guardarCambios);
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
    try {
        const respuesta = await fetch(`http://localhost:8080/rest/resource/getPelicula/${peliculaId}`, {
            method: "GET",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/json"
            }
        });
        
        if (!respuesta.ok) {
            throw new Error("Error al cargar los datos de la película");
        }
        
        const pelicula = await respuesta.json();
        
        // Rellenar el formulario con los datos de la película
        document.getElementById('peliculaId').value = pelicula.id;
        document.getElementById('titulo').value = pelicula.titulo;
        document.getElementById('genero').value = pelicula.genero;
        document.getElementById('duracion').value = pelicula.duracion;
        
        // Formatear la fecha para el input date (YYYY-MM-DD)
        if (pelicula.fechaEstreno) {
            const fecha = new Date(pelicula.fechaEstreno);
            const fechaFormateada = fecha.toISOString().split('T')[0];
            document.getElementById('fechaEstreno').value = fechaFormateada;
        }
        
        document.getElementById('director').value = pelicula.director;
        document.getElementById('sinopsis').value = pelicula.sinopsis;
        document.getElementById('horario').value = pelicula.horario;
        
        // Seleccionar la sala correcta
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

// Función para guardar los cambios
async function guardarCambios(event) {
    event.preventDefault();
    
    // Recoger los datos del formulario
    const peliculaActualizada = {
        id: document.getElementById('peliculaId').value,
        titulo: document.getElementById('titulo').value,
        genero: document.getElementById('genero').value,
        duracion: parseInt(document.getElementById('duracion').value),
        fechaEstreno: document.getElementById('fechaEstreno').value,
        director: document.getElementById('director').value,
        sinopsis: document.getElementById('sinopsis').value,
        horario: document.getElementById('horario').value,
        sala: {
            id: document.getElementById('sala').value
        }
    };
    
    try {
        const respuesta = await fetch(`http://localhost:8080/rest/resource/actualizarPelicula`, {
            method: "PUT",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/json"
            },
            body: JSON.stringify(peliculaActualizada)
        });
        
        if (!respuesta.ok) {
            throw new Error("Error al actualizar la película");
        }
        
        alert("Película actualizada correctamente");
        window.location.href = "../../html/administrador.html?nombreUsuario=" + userId;
    } catch (error) {
        console.error("Error al guardar cambios:", error);
        alert("No se pudo actualizar la película");
    }
}