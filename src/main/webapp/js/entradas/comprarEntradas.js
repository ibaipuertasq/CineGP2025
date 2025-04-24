// Variables globales
const urlParams = new URLSearchParams(window.location.search);
const userId = urlParams.get('nombreUsuario');
const peliculaIdFromParams = urlParams.get('peliculaId');
let peliculaSeleccionada = null;
let asientosSeleccionados = [];
let precioNormal = 8;
let precioVIP = 12;
let precioDiscapacitados = 6;
let salaSeleccionada = null;

// Inicialización al cargar la página
document.addEventListener('DOMContentLoaded', async function() {
    // Actualizar los enlaces con el nombre de usuario actual
    actualizarEnlaces();
    
    // Mostrar el nombre de usuario en el dropdown
    if (userId) {
        document.getElementById('userName').textContent = userId;
    } else {
        // Redireccionar al login si no hay usuario
        window.location.href = '../../index.html';
        return;
    }
    
    // Mostrar mensaje de carga
    mostrarAlerta('info', 'Cargando datos...');
    
    // Cargar películas disponibles
    await cargarPeliculas();
    
    // Si hay un ID de película en la URL, seleccionarla automáticamente
    if (peliculaIdFromParams) {
        const selectPelicula = document.getElementById('selectPelicula');
        selectPelicula.value = peliculaIdFromParams;
        
        // Disparar el evento change para actualizar la interfaz
        const event = new Event('change');
        selectPelicula.dispatchEvent(event);
    }
    
    // Configurar todos los eventos
    configurarEventos();
    
    // Ocultar mensaje de carga
    ocultarAlerta();
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

// Cargar películas desde la API
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
            throw new Error(`Error al cargar películas: ${response.status}`);
        }
        
        const peliculas = await response.json();
        
        if (peliculas.length === 0) {
            mostrarAlerta('error', 'No hay películas disponibles actualmente.');
            return;
        }
        
        // Llenar el selector de películas
        const selectPelicula = document.getElementById('selectPelicula');
        selectPelicula.innerHTML = '<option value="">Selecciona una película</option>';
        
        peliculas.forEach(pelicula => {
            const option = document.createElement('option');
            option.value = pelicula.id;
            option.textContent = pelicula.titulo;
            option.dataset.pelicula = JSON.stringify(pelicula);
            selectPelicula.appendChild(option);
        });
    } catch (error) {
        console.error('Error:', error);
        mostrarAlerta('error', 'Error al cargar las películas. Por favor, recarga la página.');
    }
}

// Configurar todos los eventos necesarios
function configurarEventos() {
    // Evento para selección de película
    document.getElementById('selectPelicula').addEventListener('change', function() {
        const peliculaId = this.value;
        if (peliculaId) {
            const selectedOption = this.options[this.selectedIndex];
            peliculaSeleccionada = JSON.parse(selectedOption.dataset.pelicula);
            salaSeleccionada = peliculaSeleccionada.sala;
            mostrarInfoPelicula(peliculaSeleccionada);
            cargarHorarios(peliculaSeleccionada);
            document.getElementById('btnContinuarPaso2').disabled = false;
        } else {
            document.getElementById('infoPelicula').style.display = 'none';
            document.getElementById('selectHorario').disabled = true;
            document.getElementById('selectHorario').innerHTML = '<option value="">Primero selecciona una película</option>';
            document.getElementById('btnContinuarPaso2').disabled = true;
            peliculaSeleccionada = null;
            salaSeleccionada = null;
        }
    });
    
    // Evento para botón Continuar del paso 1
    document.getElementById('btnContinuarPaso2').addEventListener('click', function() {
        if (!peliculaSeleccionada) {
            mostrarAlerta('error', 'Por favor, selecciona una película para continuar.');
            return;
        }
        
        const horario = document.getElementById('selectHorario').value;
        if (!horario) {
            mostrarAlerta('error', 'Por favor, selecciona un horario para continuar.');
            return;
        }
        
        // Transición al paso 2
        document.getElementById('step1').classList.remove('active');
        document.getElementById('step2').classList.add('active');
        document.getElementById('step1-content').classList.remove('active');
        document.getElementById('step2-content').classList.add('active');
        
        // Actualizar título del paso 2 con información de la película
        document.querySelector('#step2-content .section-header h2').textContent = 
            `Selecciona tus asientos para ${peliculaSeleccionada.titulo}`;
        
        // Cargar asientos
        cargarAsientos(peliculaSeleccionada);
    });
    
    // Evento para botón Volver del paso 2
    document.getElementById('btnVolverPaso1').addEventListener('click', function() {
        document.getElementById('step2').classList.remove('active');
        document.getElementById('step1').classList.add('active');
        document.getElementById('step2-content').classList.remove('active');
        document.getElementById('step1-content').classList.add('active');
        
        // Limpiar selecciones de asientos
        asientosSeleccionados = [];
        document.getElementById('asientosSeleccionados').style.display = 'none';
        document.getElementById('btnContinuarPaso3').disabled = true;
    });
    
    // Evento para botón Continuar del paso 2
    document.getElementById('btnContinuarPaso3').addEventListener('click', function() {
        if (asientosSeleccionados.length === 0) {
            mostrarAlerta('error', 'Por favor, selecciona al menos un asiento para continuar.');
            return;
        }
        
        // Transición al paso 3
        document.getElementById('step2').classList.remove('active');
        document.getElementById('step3').classList.add('active');
        document.getElementById('step2-content').classList.remove('active');
        document.getElementById('step3-content').classList.add('active');
        
        // Mostrar resumen
        mostrarResumenCompra();
    });
    
    // Evento para botón Volver del paso 3
    document.getElementById('btnVolverPaso2').addEventListener('click', function() {
        document.getElementById('step3').classList.remove('active');
        document.getElementById('step2').classList.add('active');
        document.getElementById('step3-content').classList.remove('active');
        document.getElementById('step2-content').classList.add('active');
    });
    
    // Evento para cambio en método de pago
    document.getElementById('metodoPago').addEventListener('change', function() {
        const metodoPago = this.value;
        const formularioPago = document.getElementById('formularioPago');
        
        if (metodoPago === 'tarjeta') {
            formularioPago.style.display = 'block';
        } else {
            formularioPago.style.display = 'none';
        }
    });
    
    // Evento para botón Finalizar compra
    document.getElementById('btnFinalizarCompra').addEventListener('click', async function() {
        const metodoPago = document.getElementById('metodoPago').value;
        
        if (!metodoPago) {
            mostrarAlerta('error', 'Por favor, selecciona un método de pago.');
            return;
        }
        
        if (metodoPago === 'tarjeta') {
            const numeroTarjeta = document.getElementById('numeroTarjeta').value;
            const titularTarjeta = document.getElementById('titularTarjeta').value;
            const fechaExpiracion = document.getElementById('fechaExpiracion').value;
            const cvv = document.getElementById('cvv').value;
            
            if (!numeroTarjeta || !titularTarjeta || !fechaExpiracion || !cvv) {
                mostrarAlerta('error', 'Por favor, completa todos los campos de la tarjeta.');
                return;
            }
            
            // Validar formato de tarjeta
            if (!/^\d{16}$/.test(numeroTarjeta.replace(/\s/g, ''))) {
                mostrarAlerta('error', 'El número de tarjeta debe tener 16 dígitos.');
                return;
            }
            
            if (!/^\d{3}$/.test(cvv)) {
                mostrarAlerta('error', 'El CVV debe tener 3 dígitos.');
                return;
            }
        }
        
        // Procesar la compra
        try {
            procesarCompra();
        } catch (error) {
            console.error('Error al procesar la compra:', error);
            mostrarAlerta('error', 'Error al procesar la compra: ' + error.message);
        }
    });
    
    // Evento para botón Cancelar
    document.getElementById('btnCancelarCompra').addEventListener('click', function() {
        if (confirm('¿Estás seguro de que deseas cancelar la compra?')) {
            window.location.href = `../cliente.html?nombreUsuario=${userId}`;
        }
    });
    
    // Evento para editar usuario
    document.getElementById('editarUsuario').addEventListener('click', function() {
        window.location.href = `../usuario/editarUsuario.html?nombreUsuario=${userId}`;
    });
}

// Mostrar información de la película seleccionada
function mostrarInfoPelicula(pelicula) {
    document.getElementById('infoPeliculaTitulo').textContent = pelicula.titulo;
    document.getElementById('infoPeliculaGenero').textContent = pelicula.genero;
    document.getElementById('infoPeliculaDirector').textContent = pelicula.director;
    document.getElementById('infoPeliculaDuracion').textContent = pelicula.duracion + ' minutos';
    document.getElementById('infoPeliculaSala').textContent = pelicula.sala.numero;
    document.getElementById('infoPelicula').style.display = 'block';
}

// Cargar horarios disponibles para la película seleccionada
function cargarHorarios(pelicula) {
    const selectHorario = document.getElementById('selectHorario');
    selectHorario.innerHTML = '<option value="">Selecciona un horario</option>';
    selectHorario.disabled = false;
    
    // Dividir la cadena de horarios y crear opciones
    if (pelicula.horario) {
        const horarios = pelicula.horario.split(',').map(h => h.trim());
        
        horarios.forEach(horario => {
            if (horario) {
                const option = document.createElement('option');
                option.value = horario;
                option.textContent = horario;
                selectHorario.appendChild(option);
            }
        });
        
        if (horarios.length === 0 || (horarios.length === 1 && !horarios[0])) {
            selectHorario.innerHTML = '<option value="">No hay horarios disponibles</option>';
            selectHorario.disabled = true;
        }
    } else {
        selectHorario.innerHTML = '<option value="">No hay horarios disponibles</option>';
        selectHorario.disabled = true;
    }
}

// Cargar asientos para la sala de la película seleccionada
async function cargarAsientos(pelicula) {
    try {
        mostrarAlerta('info', 'Cargando disposición de asientos...');
        
        // Obtener la sala de la película
        const sala = pelicula.sala;
        if (!sala || !sala.id) {
            throw new Error('La película no tiene sala asignada');
        }
        
        // Obtener asientos de la sala desde la API
        const response = await fetch(`http://localhost:8080/rest/resource/getAsientos/${sala.id}`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        });
        
        if (!response.ok) {
            throw new Error(`Error al cargar asientos: ${response.status}`);
        }
        
        const asientos = await response.json();
        
        // Mostrar los asientos en la interfaz
        mostrarAsientosEnInterfaz(asientos, sala);
        
        ocultarAlerta();
    } catch (error) {
        console.error('Error:', error);
        mostrarAlerta('error', 'Error al cargar los asientos: ' + error.message);
        
        // Fallback: Cargar asientos simulados si no podemos obtenerlos del servidor
        cargarAsientosSimulados(pelicula.sala);
    }
}

// Mostrar asientos en la interfaz
function mostrarAsientosEnInterfaz(asientos, sala) {
    const filasAsientos = document.getElementById('filasAsientos');
    filasAsientos.innerHTML = '';
    
    if (!asientos || asientos.length === 0) {
        filasAsientos.innerHTML = '<p class="no-asientos">No hay asientos disponibles para esta sala.</p>';
        return;
    }
    
    // Ordenar asientos por número
    asientos.sort((a, b) => a.numero - b.numero);
    
    // Calcular filas y columnas (asumiendo 10 asientos por fila)
    const asitosPorFila = 10;
    
    for (let i = 0; i < asientos.length; i++) {
        const asiento = asientos[i];
        const fila = Math.floor(i / asitosPorFila) + 1;
        const columna = (i % asitosPorFila) + 1;
        
        const asientoElement = document.createElement('div');
        asientoElement.classList.add('asiento');
        asientoElement.textContent = asiento.numero;
        asientoElement.dataset.numero = asiento.numero;
        asientoElement.dataset.fila = fila;
        asientoElement.dataset.columna = columna;
        asientoElement.dataset.tipo = asiento.tipo;
        
        // Determinar el precio según el tipo
        let precio = precioNormal;
        if (asiento.tipo === 'VIP') {
            precio = precioVIP;
            asientoElement.classList.add('vip');
        } else if (asiento.tipo === 'DISCAPACITADOS') {
            precio = precioDiscapacitados;
            asientoElement.classList.add('discapacitados');
        }
        
        asientoElement.dataset.precio = precio;
        
        if (asiento.ocupado) {
            asientoElement.classList.add('ocupado');
        } else {
            // Solo añadir evento click a los asientos no ocupados
            asientoElement.addEventListener('click', function() {
                seleccionarAsiento(this);
            });
        }
        
        filasAsientos.appendChild(asientoElement);
    }
}

// Método de respaldo para cargar asientos simulados
function cargarAsientosSimulados(sala) {
    const filasAsientos = document.getElementById('filasAsientos');
    filasAsientos.innerHTML = '';
    
    // Simulamos la carga de asientos
    const totalAsientos = sala.capacidad || 80;
    const asientosPorFila = 10;
    
    for (let i = 0; i < totalAsientos; i++) {
        const fila = Math.floor(i / asientosPorFila) + 1;
        const columna = (i % asientosPorFila) + 1;
        const numeroAsiento = i + 1;
        
        // Determinar el tipo de asiento
        let tipoAsiento = 'NORMAL';
        
        // Los últimos 5 asientos son para discapacitados
        if (i >= totalAsientos - 5) {
            tipoAsiento = 'DISCAPACITADOS';
        } 
        // El 20% de los asientos (excluyendo los de discapacitados) son VIP
        else if (i < (totalAsientos - 5) * 0.2) {
            tipoAsiento = 'VIP';
        }
        
        // Determinar si el asiento está ocupado (simulado)
        const ocupado = Math.random() < 0.3; // 30% de probabilidad
        
        const asiento = document.createElement('div');
        asiento.classList.add('asiento');
        asiento.textContent = numeroAsiento;
        asiento.dataset.numero = numeroAsiento;
        asiento.dataset.fila = fila;
        asiento.dataset.columna = columna;
        asiento.dataset.tipo = tipoAsiento;
        asiento.dataset.precio = tipoAsiento === 'NORMAL' ? precioNormal : 
                                 tipoAsiento === 'VIP' ? precioVIP : precioDiscapacitados;
        
        if (tipoAsiento === 'VIP') {
            asiento.classList.add('vip');
        } else if (tipoAsiento === 'DISCAPACITADOS') {
            asiento.classList.add('discapacitados');
        }
        
        if (ocupado) {
            asiento.classList.add('ocupado');
        } else {
            // Solo añadir evento click a los asientos no ocupados
            asiento.addEventListener('click', function() {
                seleccionarAsiento(this);
            });
        }
        
        filasAsientos.appendChild(asiento);
    }
}

// Manejar la selección de un asiento
function seleccionarAsiento(asientoElement) {
    // No permitir seleccionar asientos ocupados
    if (asientoElement.classList.contains('ocupado')) {
        return;
    }
    
    const numeroAsiento = asientoElement.dataset.numero;
    const tipoAsiento = asientoElement.dataset.tipo;
    const precio = parseInt(asientoElement.dataset.precio);
    
    // Verificar si ya está seleccionado
    if (asientoElement.classList.contains('seleccionado')) {
        // Deseleccionar
        asientoElement.classList.remove('seleccionado');
        
        // Eliminar de la lista de asientos seleccionados
        asientosSeleccionados = asientosSeleccionados.filter(a => a.numero != numeroAsiento);
    } else {
        // Seleccionar
        asientoElement.classList.add('seleccionado');
        
        // Añadir a la lista de asientos seleccionados
        asientosSeleccionados.push({
            numero: parseInt(numeroAsiento),
            tipo: tipoAsiento,
            precio: precio,
            fila: asientoElement.dataset.fila,
            columna: asientoElement.dataset.columna
        });
    }
    
    // Actualizar lista de asientos y precio total
    actualizarResumenAsientos();
}

// Actualizar el resumen de asientos seleccionados
function actualizarResumenAsientos() {
    const listaAsientos = document.getElementById('listaAsientos');
    const precioTotal = document.getElementById('precioTotal');
    const btnContinuar = document.getElementById('btnContinuarPaso3');
    
    if (asientosSeleccionados.length > 0) {
        let html = '<ul>';
        let total = 0;
        
        asientosSeleccionados.forEach(asiento => {
            html += `<li>Asiento ${asiento.numero} (${asiento.tipo}) - ${asiento.precio} €</li>`;
            total += asiento.precio;
        });
        
        html += '</ul>';
        listaAsientos.innerHTML = html;
        precioTotal.textContent = total;
        document.getElementById('asientosSeleccionados').style.display = 'block';
        btnContinuar.disabled = false;
    } else {
        document.getElementById('asientosSeleccionados').style.display = 'none';
        btnContinuar.disabled = true;
    }
}

// Mostrar resumen completo de la compra
function mostrarResumenCompra() {
    const pelicula = peliculaSeleccionada;
    const horario = document.getElementById('selectHorario').value;
    let totalPrecio = 0;
    let listaAsientos = '';
    
    asientosSeleccionados.forEach(asiento => {
        if (listaAsientos) listaAsientos += ', ';
        listaAsientos += asiento.numero;
        totalPrecio += asiento.precio;
    });
    
    // Actualizar el resumen
    document.getElementById('resumenPelicula').textContent = pelicula.titulo;
    document.getElementById('resumenSala').textContent = `Sala ${pelicula.sala.numero}`;
    document.getElementById('resumenHorario').textContent = horario;
    document.getElementById('resumenAsientos').textContent = listaAsientos;
    document.getElementById('resumenTotal').textContent = `${totalPrecio} €`;
}

// Procesar la compra final
async function procesarCompra() {
    try {
        mostrarAlerta('info', 'Procesando tu compra...');
        
        // Obtener datos para la compra
        const horario = document.getElementById('selectHorario').value;
        const metodoPago = document.getElementById('metodoPago').value;
        const response1 = await fetch("http://localhost:8080/rest/resource/getCines", {
            method: "GET",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/json"
            }
        });
        
        if (!response1.ok) {
            throw new Error(`Error al cargar películas: ${response.status}`);
        }
        
        // Preparar datos para enviar al servidor
        const datosCompra = {
            nombreUsuario: userId,
            cineId: 1, // ID del cine (asumiendo que hay un solo cine)
            peliculaId: peliculaSeleccionada.id,
            horario: horario,
            asientos: asientosSeleccionados.map(a => ({
                numero: parseInt(a.numero),
                tipo: a.tipo,
                precio: a.precio
            })),
            metodoPago: metodoPago,
            precioTotal: asientosSeleccionados.reduce((total, a) => total + a.precio, 0)
        };
        
        console.log('Enviando datos de compra:', datosCompra);
        
        // Llamada a la API para guardar la compra
        const response = await fetch('http://localhost:8080/rest/resource/comprarEntradas', {
            method: 'POST',
            headers: {
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(datosCompra)
        });
        
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`Error al procesar la compra: ${response.status} - ${errorText}`);
        }
        
        const resultado = await response.json();
        console.log('Resultado de la compra:', resultado);
        
        // Mostrar mensaje de éxito
        mostrarAlerta('exito', '¡Compra realizada con éxito! Redirigiendo a la página principal...');
        
        // Redireccionar después de un breve retraso
        setTimeout(() => {
            window.location.href = `../cliente.html?nombreUsuario=${userId}#entradas`;
        }, 2000);
        
    } catch (error) {
        console.error('Error:', error);
        mostrarAlerta('error', 'Error al procesar la compra: ' + error.message);
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