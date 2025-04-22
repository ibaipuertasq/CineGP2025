// Obtener parámetros de la URL
const urlParams = new URLSearchParams(window.location.search);
const userId = urlParams.get('nombreUsuario');

// Mostrar el nombre de usuario en la página
document.addEventListener('DOMContentLoaded', async function() {
    // Actualizar el link para volver a la cartelera
    const linkVolver = document.getElementById('volverCartelera');
    if (linkVolver && userId) {
        linkVolver.href = `../cliente.html?nombreUsuario=${userId}`;
    }

    // Mostrar el nombre de usuario en el dropdown
    if (userId) {
        document.getElementById('userName').textContent = userId;
    }

    // Cargar las salas disponibles
    await cargarSalas();

    // Event listener para el botón de cancelar
    document.getElementById('botonCancelar').addEventListener('click', function() {
        window.location.href = `../cliente.html?nombreUsuario=${userId}`;
    });

    // Event listener para editar usuario
    document.getElementById('editarUsuario').addEventListener('click', function(e) {
        e.preventDefault();
        if (userId) {
            window.location.href = `../usuario/editarUsuario.html?nombreUsuario=${userId}`;
        } else {
            console.error("No se encontró el parámetro 'nombreUsuario' en la URL.");
        }
    });

    // Event listener para el formulario
    document.getElementById('formularioPelicula').addEventListener('submit', function(e) {
        e.preventDefault();
        crearPelicula();
    });
});

// Función para cargar las salas disponibles
async function cargarSalas() {
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
        
        // Obtener salas únicas de las películas
        const salasMap = new Map();
        
        peliculas.forEach(pelicula => {
            if (pelicula.sala && pelicula.sala.id) {
                salasMap.set(pelicula.sala.id, pelicula.sala);
            }
        });
        
        const selectSala = document.getElementById('sala');
        selectSala.innerHTML = '<option value="">Selecciona una sala</option>';
        
        // Añadir las salas al select
        salasMap.forEach(sala => {
            const option = document.createElement('option');
            option.value = sala.id;
            option.textContent = `Sala ${sala.numero} (Capacidad: ${sala.capacidad})`;
            selectSala.appendChild(option);
        });
    } catch (error) {
        console.error('Error al cargar las salas:', error);
        mostrarAlerta('error', 'Error al cargar las salas. Por favor, recarga la página.');
    }
}

// Función para crear una nueva película
async function crearPelicula() {
    try {
        // Recoger los datos del formulario
        const titulo = document.getElementById('titulo').value;
        const genero = document.getElementById('genero').value;
        const duracion = parseInt(document.getElementById('duracion').value);
        const fechaEstreno = document.getElementById('fechaEstreno').value;
        const director = document.getElementById('director').value;
        const sinopsis = document.getElementById('sinopsis').value;
        const horario = document.getElementById('horario').value;
        const salaId = document.getElementById('sala').value;

        // Validar que todos los campos estén completos
        if (!titulo || !genero || !duracion || !fechaEstreno || !director || !sinopsis || !horario || !salaId) {
            mostrarAlerta('error', 'Por favor, completa todos los campos del formulario.');
            return;
        }

        // Crear el objeto de sala (simplificado, solo con el ID)
        const sala = { id: parseInt(salaId) };

        // Crear el objeto de película
        const pelicula = {
            titulo: titulo,
            genero: genero,
            duracion: duracion,
            fechaEstreno: new Date(fechaEstreno).toISOString(),
            director: director,
            sinopsis: sinopsis,
            horario: horario,
            sala: sala
        };

        // Enviar la solicitud al servidor
        const response = await fetch('http://localhost:8080/rest/resource/crearPelicula', {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(pelicula)
        });

        if (response.ok) {
            mostrarAlerta('exito', 'La película se ha creado correctamente.');
            // Limpiar el formulario
            document.getElementById('formularioPelicula').reset();
            // Redireccionar después de 2 segundos
            setTimeout(() => {
                window.location.href = `../cliente.html?nombreUsuario=${userId}`;
            }, 2000);
        } else {
            throw new Error('Error al crear la película');
        }
    } catch (error) {
        console.error('Error:', error);
        mostrarAlerta('error', 'Ha ocurrido un error al crear la película. Por favor, inténtalo de nuevo.');
    }
}

// Función para mostrar alertas
function mostrarAlerta(tipo, mensaje) {
    const alertaExito = document.getElementById('alertaExito');
    const alertaError = document.getElementById('alertaError');

    if (tipo === 'exito') {
        alertaExito.textContent = mensaje;
        alertaExito.style.display = 'block';
        alertaError.style.display = 'none';

        setTimeout(() => {
            alertaExito.style.display = 'none';
        }, 5000);
    } else {
        alertaError.textContent = mensaje;
        alertaError.style.display = 'block';
        alertaExito.style.display = 'none';

        setTimeout(() => {
            alertaError.style.display = 'none';
        }, 5000);
    }
}