// Variables globales
const urlParams = new URLSearchParams(window.location.search);
const userId = urlParams.get('nombreUsuario');
const entradaIdParam = urlParams.get('entradaId');
let entradas = [];
let entradaSeleccionada = null;

// Inicialización al cargar la página
document.addEventListener('DOMContentLoaded', async function() {
    // Verificar autenticación
    if (!userId) {
        console.error("No se encontró el parámetro 'nombreUsuario' en la URL.");
        window.location.href = '../../index.html';
        return;
    }
    
    // Actualizar los enlaces con el nombre de usuario actual
    actualizarEnlaces();
    
    // Mostrar el nombre de usuario en el dropdown
    document.getElementById('userName').textContent = userId;
    
    // Configurar eventos
    configurarEventos();
    
    // Cargar entradas del usuario
    await cargarEntradas();
    
    // Si hay un ID de entrada específico en la URL, resaltarla
    if (entradaIdParam) {
        resaltarEntrada(entradaIdParam);
    }
});

// Actualizar enlaces con el nombre de usuario
function actualizarEnlaces() {
    const links = document.querySelectorAll('.nav-links a');
    links.forEach(link => {
        const href = link.getAttribute('href');
        if (href.includes('NOMBRE_PLACEHOLDER')) {
            link.setAttribute('href', href.replace('NOMBRE_PLACEHOLDER', userId));
        }
    });
}

// Configurar eventos de la página
function configurarEventos() {
    // Evento para editar usuario
    document.getElementById('editarUsuario').addEventListener('click', function() {
        window.location.href = `../usuario/editarUsuario.html?nombreUsuario=${userId}`;
    });
    
    // Eventos para el modal de cancelación
    const modal = document.getElementById('modalCancelar');
    
    // Cerrar modal al hacer clic fuera
    window.addEventListener('click', function(event) {
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    });
    
    // Botón para cancelar la cancelación (valga la redundancia)
    document.getElementById('btnCancelarCancelar').addEventListener('click', function() {
        modal.style.display = 'none';
    });
    
    // Botón para confirmar la cancelación
    document.getElementById('btnConfirmarCancelar').addEventListener('click', async function() {
        modal.style.display = 'none';
        if (entradaSeleccionada) {
            await cancelarEntrada(entradaSeleccionada);
        }
    });
}

// Cargar entradas del usuario desde la API
async function cargarEntradas() {
    try {
        mostrarAlerta('info', 'Cargando tus entradas...');
        
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
        
        entradas = await response.json();
        
        // Mostrar las entradas en la interfaz
        mostrarEntradas(entradas);
        
        ocultarAlerta();
    } catch (error) {
        console.error('Error:', error);
        mostrarAlerta('error', 'Error al cargar tus entradas. Por favor, recarga la página.');
        
        // Mostrar mensaje de error en el contenedor
        const contenedorEntradas = document.getElementById('contenedorEntradas');
        contenedorEntradas.innerHTML = `
            <div class="entradas-empty">
                <i class="material-icons">error_outline</i>
                <h3>Error al cargar las entradas</h3>
                <p>${error.message}</p>
                <button class="boton" onclick="window.location.reload()">Reintentar</button>
            </div>
        `;
    }
}

// Mostrar entradas en la interfaz
function mostrarEntradas(entradas) {
    const contenedorEntradas = document.getElementById('contenedorEntradas');
    
    if (entradas.length === 0) {
        // No hay entradas
        contenedorEntradas.innerHTML = `
            <div class="entradas-empty">
                <i class="material-icons">local_movies</i>
                <h3>No tienes entradas</h3>
                <p>¡Compra tu primera entrada para disfrutar del mejor cine!</p>
                <a href="../entrada/comprarEntradas.html?nombreUsuario=${userId}" class="boton">Comprar entradas</a>
            </div>
        `;
        return;
    }
    
    // Hay entradas para mostrar
    let html = '';
    
    entradas.forEach(entrada => {
        // Determinar el tipo de asiento para mostrar un color específico
        let colorBorde = '#4CAF50'; // Por defecto, verde para asientos normales
        if (entrada.tipoAsiento === 'VIP') {
            colorBorde = '#9C27B0'; // Púrpura para VIP
        } else if (entrada.tipoAsiento === 'DISCAPACITADOS') {
            colorBorde = '#FF9800'; // Naranja para discapacitados
        }
        
        const cineNombre = entrada.cine && entrada.cine.nombre ? entrada.cine.nombre : 'CineGP';
        
        html += `
            <div class="entrada-card" id="entrada-${entrada.id}" data-id="${entrada.id}" style="border-left: 5px solid ${colorBorde}">
                <div class="entrada-header">
                    <h3 class="entrada-title">Entrada para ${cineNombre}</h3>
                    <span class="entrada-id">ID: ${entrada.id}</span>
                </div>
                <div class="entrada-details">
                    <div class="entrada-detail">
                        <span class="detail-label">CINE</span>
                        <span class="detail-value">${cineNombre}</span>
                    </div>
                    <div class="entrada-detail">
                        <span class="detail-label">ASIENTO</span>
                        <span class="detail-value">${entrada.asiento} (${entrada.tipoAsiento})</span>
                    </div>
                    <div class="entrada-detail">
                        <span class="detail-label">PRECIO</span>
                        <span class="detail-value">${entrada.precio} €</span>
                    </div>
                    <div class="entrada-detail">
                        <span class="detail-label">SALA</span>
                        <span class="detail-value">Sala 1</span>
                    </div>
                </div>
                <div class="codigo-qr">
                    <div class="qr-image">
                        <span>Código QR de la entrada</span>
                    </div>
                </div>
                <div class="entrada-actions">
                    <button class="boton boton-descargar" onclick="descargarEntrada(${entrada.id})">
                        <i class="material-icons">download</i> Descargar
                    </button>
                    <button class="boton boton-cancelar" onclick="mostrarModalCancelar(${entrada.id})">
                        <i class="material-icons">close</i> Cancelar
                    </button>
                </div>
            </div>
        `;
    });
    
    contenedorEntradas.innerHTML = html;
}

// Resaltar una entrada específica
function resaltarEntrada(entradaId) {
    // Esperar un poco para asegurarse de que el DOM está actualizado
    setTimeout(() => {
        const entradaElement = document.getElementById(`entrada-${entradaId}`);
        if (entradaElement) {
            entradaElement.scrollIntoView({ behavior: 'smooth', block: 'center' });
            
            // Añadir y luego quitar una clase para resaltar la entrada
            entradaElement.classList.add('entrada-destacada');
            
            // Quitar la clase después de unos segundos
            setTimeout(() => {
                entradaElement.classList.remove('entrada-destacada');
            }, 3000);
        }
    }, 500);
}

// Mostrar modal para confirmar cancelación
function mostrarModalCancelar(entradaId) {
    entradaSeleccionada = entradaId;
    document.getElementById('modalCancelar').style.display = 'block';
}

// Función para descargar una entrada (simulada)
function descargarEntrada(entradaId) {
    mostrarAlerta('info', 'Preparando la descarga de tu entrada...');
    
    // Simulamos una descarga (en un entorno real, generaríamos un PDF)
    setTimeout(() => {
        mostrarAlerta('exito', 'Entrada descargada con éxito.');
        setTimeout(() => {
            ocultarAlerta();
        }, 3000);
    }, 1500);
}

// Función para cancelar una entrada
async function cancelarEntrada(entradaId) {
    try {
        mostrarAlerta('info', 'Procesando la cancelación...');
        
        // Llamada a la API para cancelar la entrada
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
        
        // Entrada cancelada con éxito
        mostrarAlerta('exito', 'La entrada ha sido cancelada con éxito.');
        
        // Actualizar la lista de entradas (eliminar la cancelada)
        entradas = entradas.filter(e => e.id !== entradaId);
        mostrarEntradas(entradas);
        
        setTimeout(() => {
            ocultarAlerta();
        }, 3000);
        
    } catch (error) {
        console.error('Error:', error);
        mostrarAlerta('error', 'Error al cancelar la entrada: ' + error.message);
    }
}

// Funciones de utilidad para mostrar alertas
function mostrarAlerta(tipo, mensaje) {
    const alertaExito = document.getElementById('alertaExito');
    const alertaError = document.getElementById('alertaError');
    
    if (tipo === 'exito') {
        alertaExito.textContent = mensaje;
        alertaExito.style.display = 'block';
        alertaError.style.display = 'none';
    } else if (tipo === 'error') {
        alertaError.textContent = mensaje;
        alertaError.style.display = 'block';
        alertaExito.style.display = 'none';
    } else if (tipo === 'info') {
        alertaExito.textContent = mensaje;
        alertaExito.style.display = 'block';
        alertaError.style.display = 'none';
    }
    
    // Desplazar la página hacia arriba para mostrar la alerta
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function ocultarAlerta() {
    document.getElementById('alertaExito').style.display = 'none';
    document.getElementById('alertaError').style.display = 'none';
}