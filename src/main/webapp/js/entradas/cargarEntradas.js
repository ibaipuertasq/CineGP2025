// Variables globales
const urlParams = new URLSearchParams(window.location.search);
const userId = urlParams.get('nombreUsuario');

// Inicialización al cargar la página
document.addEventListener('DOMContentLoaded', async function() {
    // Asegurarse de que el usuario está autenticado
    if (!userId) {
        console.error("No se encontró el parámetro 'nombreUsuario' en la URL.");
        window.location.href = '../index.html';
        return;
    }
    
    // Mostrar el nombre de usuario en el dropdown si existe
    document.getElementById('userName').textContent = userId;
    
    // Configurar eventos de la página
    configurarEventos();
    
    // Cargar las entradas del usuario
    await cargarEntradas();
});

// Configurar eventos de la página
function configurarEventos() {
    // Evento para el botón de comprar entradas
    document.getElementById('btnComprarEntradas').addEventListener('click', function() {
        window.location.href = `entrada/comprarEntradas.html?nombreUsuario=${userId}`;
    });
    
    // Evento para el botón de ver todas las entradas
    document.getElementById('btnVerTodasEntradas').addEventListener('click', function() {
        window.location.href = `entrada/verEntradas.html?nombreUsuario=${userId}`;
    });
    
    // Evento para editar usuario
    document.getElementById('editarUsuario').addEventListener('click', function() {
        window.location.href = `usuario/editarUsuario.html?nombreUsuario=${userId}`;
    });
}

// Cargar las entradas del usuario desde la API
async function cargarEntradas() {
    try {
        // Mostrar indicador de carga
        mostrarEstadoCarga(true, "Cargando tus entradas...");
        
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
        
        const entradas = await response.json();
        mostrarEntradas(entradas);
        
        // Ocultar indicador de carga
        mostrarEstadoCarga(false);
    } catch (error) {
        console.error('Error al cargar entradas:', error);
        mostrarError(`No se pudieron cargar tus entradas: ${error.message}`);
    }
}

// Mostrar las entradas en la tabla
function mostrarEntradas(entradas) {
    const tablaBody = document.getElementById('tablaEntradasBody');
    
    if (!entradas || entradas.length === 0) {
        tablaBody.innerHTML = `
            <tr>
                <td colspan="5" class="text-center">
                    No tienes entradas compradas. ¡Compra tu primera entrada ahora!
                </td>
            </tr>
        `;
        return;
    }
    
    let html = '';
    
    // Mostrar solo las 5 entradas más recientes en la tabla
    const entradasRecientes = entradas.slice(0, 5);
    
    entradasRecientes.forEach(entrada => {
        html += `
            <tr>
                <td>${entrada.id}</td>
                <td>${entrada.cine ? entrada.cine.nombre : 'CineGP'}</td>
                <td>Asiento ${entrada.asiento} (${entrada.tipoAsiento})</td>
                <td>${entrada.precio} €</td>
                <td>
                    <button class="btn-detalles" onclick="verDetallesEntrada(${entrada.id})">
                        <span class="material-icons">visibility</span>
                    </button>
                    <button class="btn-detalles" style="background-color: #f44336;" onclick="confirmarCancelarEntrada(${entrada.id})">
                        <span class="material-icons">close</span>
                    </button>
                </td>
            </tr>
        `;
    });
    
    tablaBody.innerHTML = html;
}

// Ver detalles de una entrada
function verDetallesEntrada(entradaId) {
    window.location.href = `entrada/verEntradas.html?nombreUsuario=${userId}&entradaId=${entradaId}`;
}

// Confirmar cancelación de entrada
function confirmarCancelarEntrada(entradaId) {
    if (confirm("¿Estás seguro de que deseas cancelar esta entrada? Esta acción no se puede deshacer.")) {
        cancelarEntrada(entradaId);
    }
}

// Cancelar entrada
async function cancelarEntrada(entradaId) {
    try {
        mostrarEstadoCarga(true, "Cancelando tu entrada...");
        
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
        
        alert("Entrada cancelada con éxito");
        
        // Recargar las entradas
        await cargarEntradas();
    } catch (error) {
        console.error('Error al cancelar entrada:', error);
        mostrarError(`No se pudo cancelar la entrada: ${error.message}`);
    } finally {
        mostrarEstadoCarga(false);
    }
}

// Mostrar estado de carga
function mostrarEstadoCarga(mostrar, mensaje = "Cargando...") {
    const tablaBody = document.getElementById('tablaEntradasBody');
    
    if (mostrar) {
        tablaBody.innerHTML = `
            <tr>
                <td colspan="5" class="text-center">
                    <div class="loading-spinner"></div>
                    <p>${mensaje}</p>
                </td>
            </tr>
        `;
    }
}

// Mostrar mensaje de error
function mostrarError(mensaje) {
    const tablaBody = document.getElementById('tablaEntradasBody');
    
    tablaBody.innerHTML = `
        <tr>
            <td colspan="5" class="text-center error-message">
                <span class="material-icons">error_outline</span>
                <p>${mensaje}</p>
                <button onclick="cargarEntradas()" class="boton">Reintentar</button>
            </td>
        </tr>
    `;
}