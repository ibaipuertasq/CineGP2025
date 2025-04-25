// Variables globales
const apiBaseUrl = 'http://localhost:8080/rest/resource';
// Obtener userId de la URL (usado en varios lugares del script)
const urlParams = new URLSearchParams(window.location.search);
const userId = urlParams.get('nombreUsuario');

// Función para cargar reseñas de una película
async function cargarResenyas(peliculaId) {
    try {
        const response = await fetch(`${apiBaseUrl}/getResenyas/${peliculaId}`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error(`Error al cargar reseñas: ${response.status}`);
        }
        
        const resenyas = await response.json();
        return resenyas;
    } catch (error) {
        console.error('Error al cargar reseñas:', error);
        throw error;
    }
}

// Función para crear una nueva reseña
async function crearResenya(resenya) {
    try {
        console.log('Enviando reseña:', JSON.stringify(resenya)); // Para depuración
        
        const response = await fetch(`${apiBaseUrl}/addResenya`, {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(resenya)
        });
        
        if (!response.ok) {
            const errorText = await response.text();
            console.error('Error de respuesta del servidor:', errorText);
            throw new Error(`Error al crear reseña: ${response.status} - ${errorText}`);
        }
        
        const nuevaResenya = await response.json();
        return nuevaResenya;
    } catch (error) {
        console.error('Error al crear reseña:', error);
        throw error;
    }
}

// Función para eliminar una reseña
async function eliminarResenya(resenyaId) {
    try {
        const response = await fetch(`${apiBaseUrl}/deleteResenya/${resenyaId}`, {
            method: 'DELETE',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error(`Error al eliminar reseña: ${response.status}`);
        }
        
        return true;
    } catch (error) {
        console.error('Error al eliminar reseña:', error);
        throw error;
    }
}

// Función para actualizar una reseña
async function actualizarResenya(resenyaId, resenya) {
    try {
        const response = await fetch(`${apiBaseUrl}/updateResenya/${resenyaId}`, {
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

// Función para mostrar las reseñas en la página
function mostrarResenyas(resenyas, contenedor) {
    // Asegurarnos de que contenedor exista
    if (!contenedor) {
        console.error('Contenedor de reseñas no encontrado');
        return;
    }
    
    contenedor.innerHTML = '';
    
    if (!resenyas || resenyas.length === 0) {
        contenedor.innerHTML = '<p class="no-resenyas">No hay reseñas para esta película todavía. ¡Sé el primero en opinar!</p>';
        return;
    }
    
    resenyas.forEach(resenya => {
        const resenyaElement = document.createElement('div');
        resenyaElement.className = 'resenya-item';
        
        // Crear la representación de estrellas
        let estrellas = '';
        for (let i = 1; i <= 5; i++) {
            if (i <= resenya.puntuacion) {
                estrellas += '<span class="material-icons estrella-llena">star</span>';
            } else {
                estrellas += '<span class="material-icons estrella-vacia">star_border</span>';
            }
        }
        
        // Crear el elemento de reseña con los datos
        // Protección contra valores nulos
        const nombreUsuario = resenya.usuario && resenya.usuario.nombre ? resenya.usuario.nombre : 'Usuario anónimo';
        const comentario = resenya.comentario || 'Sin comentario';
        
        resenyaElement.innerHTML = `
        <div class="resenya-header">
            <h4>${nombreUsuario}</h4>
            <div class="estrellas">${estrellas}</div>
        </div>
        <p class="resenya-comentario">${comentario}</p>
        <div class="resenya-acciones">
            ${userId === (resenya.usuario && resenya.usuario.nombreUsuario) ?
                `<button class="btn-editar" data-id="${resenya.id}">
                    <span class="material-icons">edit</span>
                </button>
                <button class="btn-eliminar" data-id="${resenya.id}">
                    <span class="material-icons">delete</span>
                </button>` : ''}
        </div>
        `;
        
        contenedor.appendChild(resenyaElement);
    });
    
    // Añadir eventos a los botones de editar y eliminar
    document.querySelectorAll('.btn-editar').forEach(btn => {
        btn.addEventListener('click', function() {
            const resenyaId = this.getAttribute('data-id');
            mostrarFormularioEdicion(resenyaId, resenyas);
        });
    });
    
    document.querySelectorAll('.btn-eliminar').forEach(btn => {
        btn.addEventListener('click', function() {
            const resenyaId = this.getAttribute('data-id');
            confirmarEliminarResenya(resenyaId);
        });
    });
}

// Función para mostrar el formulario de edición de una reseña
function mostrarFormularioEdicion(resenyaId, resenyas) {
    const resenya = resenyas.find(r => r.id == resenyaId);
    if (!resenya) {
        console.error(`No se encontró la reseña con ID ${resenyaId}`);
        return;
    }
    
    // Mostrar el modal de edición
    const modal = document.getElementById('resenyaEditModal');
    if (!modal) {
        console.error('Modal de edición no encontrado');
        return;
    }
    
    const comentarioInput = document.getElementById('editComentario');
    const puntuacionRadios = document.querySelectorAll('input[name="editPuntuacion"]');
    
    if (comentarioInput) {
        comentarioInput.value = resenya.comentario || '';
    }
    
    // Seleccionar el radio button correspondiente a la puntuación
    puntuacionRadios.forEach(radio => {
        if (parseInt(radio.value) === resenya.puntuacion) {
            radio.checked = true;
        }
    });
    
    // Establecer el ID de la reseña para la edición
    const guardarBtn = document.getElementById('guardarEdicionBtn');
    if (guardarBtn) {
        guardarBtn.setAttribute('data-id', resenyaId);
    }
    
    // Mostrar el modal
    modal.style.display = 'block';
}

// Función para confirmar la eliminación de una reseña
function confirmarEliminarResenya(resenyaId) {
    if (confirm('¿Estás seguro de que deseas eliminar esta reseña? Esta acción no se puede deshacer.')) {
        eliminarResenya(resenyaId)
            .then(() => {
                // Recargar las reseñas de la película actual
                const btnEnviar = document.getElementById('btnEnviarResenya');
                if (!btnEnviar) {
                    throw new Error('No se encontró el botón de enviar reseña');
                }
                const peliculaId = btnEnviar.getAttribute('data-pelicula-id');
                if (!peliculaId) {
                    throw new Error('No hay película seleccionada');
                }
                return cargarResenyas(peliculaId);
            })
            .then(resenyas => {
                const contenedor = document.getElementById('resenyas-container');
                if (!contenedor) {
                    throw new Error('Contenedor de reseñas no encontrado');
                }
                mostrarResenyas(resenyas, contenedor);
                mostrarMensajeExito('La reseña se ha eliminado correctamente.');
            })
            .catch(error => {
                mostrarMensajeError(`Error al eliminar la reseña: ${error.message}`);
            });
    }
}

// Función para cargar y mostrar reseñas de una película
function cargarYMostrarResenyas(peliculaId) {
    const contenedor = document.getElementById('resenyas-container');
    contenedor.innerHTML = '<div class="loading-spinner"></div><p>Cargando reseñas...</p>';
    
    cargarResenyas(peliculaId)
        .then(resenyas => {
            mostrarResenyas(resenyas, contenedor);
        })
        .catch(error => {
            contenedor.innerHTML = `<p class="error-message">Error al cargar las reseñas: ${error.message}</p>`;
        });
}

// Función para mostrar mensajes de éxito
function mostrarMensajeExito(mensaje) {
    const alertaElement = document.getElementById('alertaResenyas') || document.createElement('div');
    alertaElement.id = 'alertaResenyas';
    alertaElement.className = 'alerta-exito';
    alertaElement.textContent = mensaje;
    
    const seccionResenyas = document.getElementById('resenyas-section');
    if (!seccionResenyas) {
        console.error('Sección de reseñas no encontrada');
        return;
    }
    
    seccionResenyas.insertBefore(alertaElement, seccionResenyas.firstChild);
    
    setTimeout(() => {
        alertaElement.remove();
    }, 3000);
}

// Función para mostrar mensajes de error
function mostrarMensajeError(mensaje) {
    const alertaElement = document.getElementById('alertaResenyas') || document.createElement('div');
    alertaElement.id = 'alertaResenyas';
    alertaElement.className = 'alerta-error';
    alertaElement.textContent = mensaje;
    
    const seccionResenyas = document.getElementById('resenyas-section');
    if (!seccionResenyas) {
        console.error('Sección de reseñas no encontrada');
        return;
    }
    
    seccionResenyas.insertBefore(alertaElement, seccionResenyas.firstChild);
    
    setTimeout(() => {
        alertaElement.remove();
    }, 3000);
}

// Inicializar el formulario de reseñas cuando se muestra el modal de película
function inicializarFormularioResenyas(peliculaId) {
    // Limpiar formulario
    const comentarioElement = document.getElementById('comentario');
    if (comentarioElement) {
        comentarioElement.value = '';
    }
    
    document.querySelectorAll('input[name="puntuacion"]').forEach(radio => {
        radio.checked = false;
    });
    
    // Establecer el ID de la película para el formulario
    const btnEnviar = document.getElementById('btnEnviarResenya');
    if (btnEnviar) {
        btnEnviar.setAttribute('data-pelicula-id', peliculaId);
    }
    
    // Cargar reseñas existentes
    cargarYMostrarResenyas(peliculaId);
}

// Evento para enviar una nueva reseña
document.addEventListener('DOMContentLoaded', function() {
    const btnEnviarResenya = document.getElementById('btnEnviarResenya');
    if (btnEnviarResenya) {
        btnEnviarResenya.addEventListener('click', function() {
            const peliculaId = this.getAttribute('data-pelicula-id');
            if (!peliculaId) {
                mostrarMensajeError('Por favor, selecciona una película primero.');
                return;
            }
            
            const comentarioElement = document.getElementById('comentario');
            if (!comentarioElement) {
                mostrarMensajeError('Error: No se encontró el campo de comentario.');
                return;
            }
            
            const comentario = comentarioElement.value;
            const puntuacionRadios = document.querySelectorAll('input[name="puntuacion"]');
            let puntuacion = 0;
            
            puntuacionRadios.forEach(radio => {
                if (radio.checked) {
                    puntuacion = parseInt(radio.value);
                }
            });
            
            // Validar los datos
            if (!comentario || comentario.trim() === '') {
                mostrarMensajeError('Por favor, escribe un comentario para tu reseña.');
                return;
            }
            
            if (puntuacion === 0) {
                mostrarMensajeError('Por favor, selecciona una puntuación para tu reseña.');
                return;
            }
            
            // Verificar que el usuario está autenticado
            if (!userId) {
                mostrarMensajeError('Debes iniciar sesión para publicar una reseña.');
                return;
            }
            
            // Crear objeto de reseña
            const resenya = {
                comentario: comentario,
                puntuacion: puntuacion,
                usuario: {
                    nombreUsuario: userId  // Cambiar 'nombre' por 'nombreUsuario'
                },
                pelicula: {
                    id: parseInt(peliculaId)
                }
            };
            
            // Enviar reseña al servidor
            crearResenya(resenya)
                .then(() => {
                    // Limpiar formulario
                    document.getElementById('comentario').value = '';
                    puntuacionRadios.forEach(radio => {
                        radio.checked = false;
                    });
                    
                    // Recargar reseñas
                    return cargarResenyas(peliculaId);
                })
                .then(resenyas => {
                    const nombreUsuario = resenya.usuario && resenya.usuario.nombreUsuario ? resenya.usuario.nombreUsuario : 'Usuario anónimo';                    if (contenedor) {
                        mostrarResenyas(resenyas, contenedor);
                        mostrarMensajeExito('Tu reseña se ha publicado correctamente.');
                    }
                })
                .catch(error => {
                    mostrarMensajeError(`Error al publicar tu reseña: ${error.message}`);
                });
        });
    }
    
    // Evento para guardar la edición de una reseña
    const guardarEdicionBtn = document.getElementById('guardarEdicionBtn');
    if (guardarEdicionBtn) {
        guardarEdicionBtn.addEventListener('click', function() {
            const resenyaId = this.getAttribute('data-id');
            if (!resenyaId) {
                alert('Error: ID de reseña no encontrado');
                return;
            }
            
            const comentarioElement = document.getElementById('editComentario');
            if (!comentarioElement) {
                alert('Error: Campo de comentario no encontrado');
                return;
            }
            
            const comentario = comentarioElement.value;
            const puntuacionRadios = document.querySelectorAll('input[name="editPuntuacion"]');
            let puntuacion = 0;
            
            puntuacionRadios.forEach(radio => {
                if (radio.checked) {
                    puntuacion = parseInt(radio.value);
                }
            });
            
            // Validar los datos
            if (!comentario || comentario.trim() === '') {
                alert('Por favor, escribe un comentario para tu reseña.');
                return;
            }
            
            if (puntuacion === 0) {
                alert('Por favor, selecciona una puntuación para tu reseña.');
                return;
            }
            
            // Crear objeto de reseña actualizada
            const resenya = {
                comentario: comentario,
                puntuacion: puntuacion
            };
            
            // Enviar actualización al servidor
            actualizarResenya(resenyaId, resenya)
                .then(() => {
                    // Cerrar el modal
                    const modal = document.getElementById('resenyaEditModal');
                    if (modal) {
                        modal.style.display = 'none';
                    }
                    
                    // Recargar reseñas
                    const btnEnviar = document.getElementById('btnEnviarResenya');
                    if (!btnEnviar) {
                        throw new Error('Botón de enviar reseña no encontrado');
                    }
                    
                    const peliculaId = btnEnviar.getAttribute('data-pelicula-id');
                    if (!peliculaId) {
                        throw new Error('No hay película seleccionada');
                    }
                    
                    return cargarResenyas(peliculaId);
                })
                .then(resenyas => {
                    const contenedor = document.getElementById('resenyas-container');
                    if (!contenedor) {
                        throw new Error('Contenedor de reseñas no encontrado');
                    }
                    
                    mostrarResenyas(resenyas, contenedor);
                    mostrarMensajeExito('Tu reseña se ha actualizado correctamente.');
                })
                .catch(error => {
                    mostrarMensajeError(`Error al actualizar tu reseña: ${error.message}`);
                });
        });
    }
    
    // Evento para cerrar el modal de edición
    const cancelarEdicionBtn = document.getElementById('cancelarEdicionBtn');
    if (cancelarEdicionBtn) {
        cancelarEdicionBtn.addEventListener('click', function() {
            const modal = document.getElementById('resenyaEditModal');
            if (modal) {
                modal.style.display = 'none';
            }
        });
    }
    
    // Evento para cerrar el modal de edición al hacer clic fuera
    window.addEventListener('click', function(event) {
        const modal = document.getElementById('resenyaEditModal');
        if (modal && event.target === modal) {
            modal.style.display = 'none';
        }
    });
});

// Hacer la función disponible globalmente
window.cargarYMostrarResenyas = cargarYMostrarResenyas;

// Exportar funciones para su uso en otros archivos
export {
    cargarResenyas,
    crearResenya,
    eliminarResenya,
    actualizarResenya,
    mostrarResenyas,
    inicializarFormularioResenyas,
    cargarYMostrarResenyas
};