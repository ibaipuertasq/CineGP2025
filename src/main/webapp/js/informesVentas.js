// Obtener el parámetro 'nombreUsuario' de la URL
const urlParams = new URLSearchParams(window.location.search);
const userId = urlParams.get('nombreUsuario');

// Configuración para formatear moneda
const formatoMoneda = new Intl.NumberFormat('es-ES', {
    style: 'currency',
    currency: 'EUR'
});

// Variables para almacenar datos
let datosPeliculas = [];
let datosTipoAsiento = [];
let datosPorDia = [];
let datosResumen = {};

// Referencias a los gráficos
let chartPeliculas = null;
let chartTipoAsiento = null;
let chartPorDia = null;

// Colores para los gráficos
const colores = [
    '#4CAF50', '#2196F3', '#F44336', '#FF9800', '#9C27B0',
    '#3F51B5', '#E91E63', '#009688', '#FFC107', '#795548',
    '#607D8B', '#8BC34A', '#FFEB3B', '#00BCD4', '#9E9E9E'
];

// Función para actualizar el enlace de volver a administrador
function actualizarEnlaceVolver() {
    const enlaceVolver = document.getElementById('volverAdmin');
    if (userId) {
        enlaceVolver.href = `../administrador.html?nombreUsuario=${userId}`;
    }
    
    // Actualizar nombre de usuario en la navbar
    const userName = document.getElementById('userName');
    if (userName && userId) {
        userName.textContent = userId;
    }
    
    // Configurar botón de editar usuario
    const btnEditarUsuario = document.getElementById('editarUsuario');
    if (btnEditarUsuario) {
        btnEditarUsuario.addEventListener('click', (e) => {
            e.preventDefault();
            if (userId) {
                window.location.href = `../usuario/editarUsuario.html?nombreUsuario=${userId}`;
            } else {
                console.error("No se encontró el parámetro 'nombreUsuario' en la URL.");
            }
        });
    }
}

// Función para cargar el resumen general de ventas
async function cargarResumenVentas() {
    try {
        const response = await fetch("http://localhost:8080/rest/resource/resumenVentas");
        if (!response.ok) {
            throw new Error(`Error al cargar el resumen de ventas: ${response.status}`);
        }
        
        datosResumen = await response.json();
        
        // Actualizar la interfaz con los datos
        document.getElementById('totalEntradas').textContent = datosResumen.totalEntradas || 0;
        document.getElementById('totalIngresos').textContent = formatoMoneda.format(datosResumen.totalIngresos || 0);
        document.getElementById('promedioPrecio').textContent = formatoMoneda.format(datosResumen.promedioPrecio || 0);
        document.getElementById('clientesUnicos').textContent = datosResumen.totalUsuariosUnicos || 0;
        
        return true;
    } catch (error) {
        console.error("Error al cargar el resumen de ventas:", error);
        // Mostrar mensaje de error
        document.getElementById('totalEntradas').textContent = "Error";
        document.getElementById('totalIngresos').textContent = "Error";
        document.getElementById('promedioPrecio').textContent = "Error";
        document.getElementById('clientesUnicos').textContent = "Error";
        
        return false;
    }
}

// Función para cargar ventas por película
async function cargarVentasPorPelicula() {
    try {
        const response = await fetch("http://localhost:8080/rest/resource/ventasPorPelicula");
        if (!response.ok) {
            throw new Error(`Error al cargar ventas por película: ${response.status}`);
        }
        
        datosPeliculas = await response.json();
        
        // Actualizar la tabla de películas
        actualizarTablaPeliculas();
        
        return true;
    } catch (error) {
        console.error("Error al cargar ventas por película:", error);
        document.getElementById('tablaPeliculasBody').innerHTML = `
            <tr>
                <td colspan="4">Error al cargar datos: ${error.message}</td>
            </tr>
        `;
        
        return false;
    }
}

// Función para cargar ventas por tipo de asiento
async function cargarVentasPorTipoAsiento() {
    try {
        const response = await fetch("http://localhost:8080/rest/resource/ventasPorTipoAsiento");
        if (!response.ok) {
            throw new Error(`Error al cargar ventas por tipo de asiento: ${response.status}`);
        }
        
        datosTipoAsiento = await response.json();
        
        // Actualizar la tabla de tipos de asiento
        actualizarTablaTipoAsiento();
        
        return true;
    } catch (error) {
        console.error("Error al cargar ventas por tipo de asiento:", error);
        document.getElementById('tablaTipoAsientoBody').innerHTML = `
            <tr>
                <td colspan="4">Error al cargar datos: ${error.message}</td>
            </tr>
        `;
        
        return false;
    }
}

// Función para cargar ventas por día
async function cargarVentasPorDia() {
    try {
        const response = await fetch("http://localhost:8080/rest/resource/ventasPorDia");
        if (!response.ok) {
            throw new Error(`Error al cargar ventas por día: ${response.status}`);
        }
        
        datosPorDia = await response.json();
        
        // Actualizar la tabla de ventas por día
        actualizarTablaPorDia();
        
        return true;
    } catch (error) {
        console.error("Error al cargar ventas por día:", error);
        document.getElementById('tablaPorDiaBody').innerHTML = `
            <tr>
                <td colspan="3">Error al cargar datos: ${error.message}</td>
            </tr>
        `;
        
        return false;
    }
}

// Función para actualizar la tabla de películas
function actualizarTablaPeliculas() {
    const tbody = document.getElementById('tablaPeliculasBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    // Ordenar películas por ingresos de mayor a menor
    const peliculasOrdenadas = [...datosPeliculas].sort((a, b) => b.totalIngresos - a.totalIngresos);
    
    if (peliculasOrdenadas.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="4">No hay datos disponibles</td>
            </tr>
        `;
        return;
    }
    
    peliculasOrdenadas.forEach(pelicula => {
        const fila = document.createElement('tr');
        fila.innerHTML = `
            <td>${pelicula.titulo || 'Sin título'}</td>
            <td>Sala ${pelicula.numeroSala || '-'}</td>
            <td>${pelicula.totalEntradas || 0}</td>
            <td>${formatoMoneda.format(pelicula.totalIngresos || 0)}</td>
        `;
        tbody.appendChild(fila);
    });
}

// Función para actualizar la tabla de tipos de asiento
function actualizarTablaTipoAsiento() {
    const tbody = document.getElementById('tablaTipoAsientoBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    // Ordenar tipos por ingresos de mayor a menor
    const tiposOrdenados = [...datosTipoAsiento].sort((a, b) => b.totalIngresos - a.totalIngresos);
    
    if (tiposOrdenados.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="4">No hay datos disponibles</td>
            </tr>
        `;
        return;
    }
    
    tiposOrdenados.forEach(tipo => {
        const promedio = tipo.totalEntradas > 0 ? tipo.totalIngresos / tipo.totalEntradas : 0;
        
        const fila = document.createElement('tr');
        fila.innerHTML = `
            <td>${tipo.tipo || 'Desconocido'}</td>
            <td>${tipo.totalEntradas || 0}</td>
            <td>${formatoMoneda.format(tipo.totalIngresos || 0)}</td>
            <td>${formatoMoneda.format(promedio)}</td>
        `;
        tbody.appendChild(fila);
    });
}

// Función para actualizar la tabla de ventas por día
function actualizarTablaPorDia() {
    const tbody = document.getElementById('tablaPorDiaBody');
    if (!tbody) return;
    
    tbody.innerHTML = '';
    
    if (datosPorDia.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="3">No hay datos disponibles</td>
            </tr>
        `;
        return;
    }
    
    // Los datos ya vienen ordenados por fecha
    datosPorDia.forEach(dia => {
        const fila = document.createElement('tr');
        fila.innerHTML = `
            <td>${dia.fecha || 'Fecha desconocida'}</td>
            <td>${dia.totalEntradas || 0}</td>
            <td>${formatoMoneda.format(dia.totalIngresos || 0)}</td>
        `;
        tbody.appendChild(fila);
    });
}

// Función para inicializar los gráficos
function inicializarGraficos() {
    // Destruir gráficos existentes para evitar duplicados
    if (chartPeliculas) {
        chartPeliculas.destroy();
        chartPeliculas = null;
    }
    
    if (chartTipoAsiento) {
        chartTipoAsiento.destroy();
        chartTipoAsiento = null;
    }
    
    if (chartPorDia) {
        chartPorDia.destroy();
        chartPorDia = null;
    }
    
    // Inicializar gráfico de películas
    const ctxPeliculas = document.getElementById('chartPeliculas');
    if (ctxPeliculas) {
        const peliculasOrdenadas = [...datosPeliculas]
            .sort((a, b) => b.totalIngresos - a.totalIngresos)
            .slice(0, 7); // Mostrar solo las 7 películas con más ingresos
        
        const labels = peliculasOrdenadas.map(pelicula => pelicula.titulo || 'Sin título');
        const data = peliculasOrdenadas.map(pelicula => pelicula.totalIngresos || 0);
        const backgroundColors = colores.slice(0, peliculasOrdenadas.length);
        
        // Encontrar el valor máximo para establecer el límite del eje Y
        const maxIngreso = Math.max(...data) * 1.1; // 10% más que el valor máximo
        
        chartPeliculas = new Chart(ctxPeliculas, {
            type: 'bar',
            data: {
                labels: labels,
                datasets: [{
                    label: 'Ingresos (€)',
                    data: data,
                    backgroundColor: backgroundColors,
                    borderColor: backgroundColors.map(color => color),
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true,
                        max: maxIngreso, // Establecer límite máximo fijo
                        ticks: {
                            callback: function(value) {
                                return formatoMoneda.format(value);
                            }
                        }
                    },
                    x: {
                        ticks: {
                            maxRotation: 45,
                            minRotation: 45
                        }
                    }
                },
                plugins: {
                    legend: {
                        display: false
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return formatoMoneda.format(context.raw);
                            }
                        }
                    }
                }
            }
        });
    }
    
    // Inicializar gráfico de tipos de asiento
    const ctxTipoAsiento = document.getElementById('chartTipoAsiento');
    if (ctxTipoAsiento && datosTipoAsiento.length > 0) {
        const labels = datosTipoAsiento.map(tipo => tipo.tipo || 'Desconocido');
        const data = datosTipoAsiento.map(tipo => tipo.totalIngresos || 0);
        const backgroundColors = colores.slice(0, datosTipoAsiento.length);
        
        chartTipoAsiento = new Chart(ctxTipoAsiento, {
            type: 'pie',
            data: {
                labels: labels,
                datasets: [{
                    data: data,
                    backgroundColor: backgroundColors,
                    borderColor: backgroundColors.map(color => color),
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                const value = context.raw;
                                const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                const percentage = Math.round((value / total) * 100);
                                return `${formatoMoneda.format(value)} (${percentage}%)`;
                            }
                        }
                    }
                }
            }
        });
    }
    
    // Inicializar gráfico de ventas por día
    const ctxPorDia = document.getElementById('chartPorDia');
    if (ctxPorDia && datosPorDia.length > 0) {
        const labels = datosPorDia.map(dia => dia.fecha || 'Fecha desconocida');
        const dataIngresos = datosPorDia.map(dia => dia.totalIngresos || 0);
        const dataEntradas = datosPorDia.map(dia => dia.totalEntradas || 0);
        
        // Encontrar valores máximos para establecer límites de ejes
        const maxIngreso = Math.max(...dataIngresos) * 1.1;
        const maxEntradas = Math.max(...dataEntradas) * 1.1;
        
        chartPorDia = new Chart(ctxPorDia, {
            type: 'line',
            data: {
                labels: labels,
                datasets: [
                    {
                        label: 'Ingresos',
                        data: dataIngresos,
                        backgroundColor: 'rgba(76, 175, 80, 0.2)',
                        borderColor: 'rgba(76, 175, 80, 1)',
                        borderWidth: 2,
                        yAxisID: 'y',
                        tension: 0.4
                    },
                    {
                        label: 'Entradas',
                        data: dataEntradas,
                        backgroundColor: 'rgba(33, 150, 243, 0.2)',
                        borderColor: 'rgba(33, 150, 243, 1)',
                        borderWidth: 2,
                        yAxisID: 'y1',
                        tension: 0.4
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        type: 'linear',
                        display: true,
                        position: 'left',
                        beginAtZero: true,
                        max: maxIngreso, // Establecer límite máximo fijo
                        title: {
                            display: true,
                            text: 'Ingresos (€)'
                        },
                        ticks: {
                            callback: function(value) {
                                return formatoMoneda.format(value);
                            }
                        }
                    },
                    y1: {
                        type: 'linear',
                        display: true,
                        position: 'right',
                        beginAtZero: true,
                        max: maxEntradas, // Establecer límite máximo fijo
                        title: {
                            display: true,
                            text: 'Entradas'
                        },
                        grid: {
                            drawOnChartArea: false
                        }
                    },
                    x: {
                        ticks: {
                            maxRotation: 45,
                            minRotation: 45
                        }
                    }
                }
            }
        });
    }
}

// Manejar los botones de cambio de tabla
function configurarBotonesFiltro() {
    const botones = document.querySelectorAll('.btn-filtro');
    const tablas = document.querySelectorAll('.tabla-box');
    
    botones.forEach(boton => {
        boton.addEventListener('click', () => {
            // Quitar clase active de todos los botones
            botones.forEach(b => b.classList.remove('active'));
            
            // Añadir clase active al botón clickeado
            boton.classList.add('active');
            
            // Ocultar todas las tablas
            tablas.forEach(tabla => tabla.style.display = 'none');
            
            // Mostrar la tabla correspondiente
            const tablaId = boton.dataset.tabla;
            document.getElementById(tablaId).style.display = 'block';
        });
    });
}

// Función principal de inicialización
async function inicializar() {
    // Actualizar enlaces y configurar botones
    actualizarEnlaceVolver();
    configurarBotonesFiltro();
    
    // Cargar todos los datos primero
    const [resumenOk, peliculasOk, tiposOk, diasOk] = await Promise.all([
        cargarResumenVentas(),
        cargarVentasPorPelicula(),
        cargarVentasPorTipoAsiento(),
        cargarVentasPorDia()
    ]);
    
    // Inicializar gráficos solo si tenemos datos
    if (peliculasOk || tiposOk || diasOk) {
        // Usar requestAnimationFrame para asegurar que el DOM está listo
        window.requestAnimationFrame(() => {
            inicializarGraficos();
        });
    }
}

// Iniciar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', inicializar);