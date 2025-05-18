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
        
        // Filtrar películas sin ventas para mostrar solo datos relevantes
        datosPeliculas = datosPeliculas.filter(pelicula => pelicula.totalEntradas > 0);
        
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
        
        // Filtrar tipos sin ventas para mostrar solo datos relevantes
        datosTipoAsiento = datosTipoAsiento.filter(tipo => tipo.totalEntradas > 0);
        
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
        
        // Filtrar días sin ventas para mostrar solo datos relevantes
        datosPorDia = datosPorDia.filter(dia => dia.totalEntradas > 0);
        
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
    // Eliminar y reemplazar los elementos canvas para evitar problemas de persistencia
    reemplazarCanvas('chartPeliculas', 'chart-peliculas-container');
    reemplazarCanvas('chartTipoAsiento', 'chart-tipoasiento-container');
    reemplazarCanvas('chartPorDia', 'chart-pordia-container');
    
    // Crear gráfico de películas
    crearGraficoPeliculas();
    
    // Crear gráfico de tipos de asiento
    crearGraficoTiposAsiento();
    
    // Crear gráfico de ventas por día
    crearGraficoPorDia();
}

// Función para reemplazar un canvas viejo por uno nuevo
function reemplazarCanvas(canvasId, containerId) {
    const container = document.getElementById(containerId);
    if (!container) return;
    
    // Eliminar el canvas anterior
    const canvasViejo = document.getElementById(canvasId);
    if (canvasViejo) {
        canvasViejo.remove();
    }
    
    // Crear un nuevo canvas
    const canvasNuevo = document.createElement('canvas');
    canvasNuevo.id = canvasId;
    container.appendChild(canvasNuevo);
}

// Función para crear el gráfico de películas
function crearGraficoPeliculas() {
    const canvas = document.getElementById('chartPeliculas');
    if (!canvas || datosPeliculas.length === 0) return;
    
    const ctx = canvas.getContext('2d');
    
    // Ordenar películas por ingresos de mayor a menor
    const peliculasOrdenadas = [...datosPeliculas]
        .sort((a, b) => b.totalIngresos - a.totalIngresos)
        .slice(0, 7); // Mostrar solo las 7 películas con más ingresos
    
    // Abreviar títulos largos para mejor visualización
    const labels = peliculasOrdenadas.map(pelicula => {
        const titulo = pelicula.titulo || 'Sin título';
        return titulo.length > 20 ? titulo.substring(0, 17) + '...' : titulo;
    });
    
    const data = peliculasOrdenadas.map(pelicula => pelicula.totalIngresos || 0);
    const backgroundColors = colores.slice(0, peliculasOrdenadas.length);
    
    // Calcular el valor máximo para el eje Y
    const maxIngreso = Math.max(...data) * 1.2; // 20% más alto para dar espacio
    
    // Crear gráfico
    if (chartPeliculas) {
        chartPeliculas.destroy();
    }
    
    chartPeliculas = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Ingresos',
                data: data,
                backgroundColor: backgroundColors,
                borderColor: 'rgba(0, 0, 0, 0.1)',
                borderWidth: 1,
                barPercentage: 0.7,
                categoryPercentage: 0.8
            }]
        },
        options: {
            animation: {
                duration: 0 // Desactivar animaciones para evitar problemas al hacer hover
            },
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    max: maxIngreso,
                    ticks: {
                        callback: function(value) {
                            return formatoMoneda.format(value);
                        }
                    }
                },
                x: {
                    grid: {
                        display: false
                    },
                    ticks: {
                        font: {
                            size: 11
                        },
                        autoSkip: false,
                        maxRotation: 45,
                        minRotation: 45
                    }
                }
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        title: function(context) {
                            // Mostrar el título completo en el tooltip
                            return peliculasOrdenadas[context[0].dataIndex].titulo;
                        },
                        label: function(context) {
                            return 'Ingresos: ' + formatoMoneda.format(context.raw);
                        },
                        afterLabel: function(context) {
                            return 'Entradas: ' + peliculasOrdenadas[context.dataIndex].totalEntradas;
                        }
                    }
                },
                legend: {
                    display: false
                }
            }
        }
    });
}

// Función para crear el gráfico de tipos de asiento
function crearGraficoTiposAsiento() {
    const canvas = document.getElementById('chartTipoAsiento');
    if (!canvas || datosTipoAsiento.length === 0) return;
    
    const ctx = canvas.getContext('2d');
    
    const labels = datosTipoAsiento.map(tipo => tipo.tipo || 'Desconocido');
    const data = datosTipoAsiento.map(tipo => tipo.totalIngresos || 0);
    const backgroundColors = colores.slice(0, datosTipoAsiento.length);
    
    // Crear gráfico
    if (chartTipoAsiento) {
        chartTipoAsiento.destroy();
    }
    
    chartTipoAsiento = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: labels,
            datasets: [{
                data: data,
                backgroundColor: backgroundColors,
                borderColor: 'rgba(255, 255, 255, 0.5)',
                borderWidth: 2,
                hoverOffset: 4
            }]
        },
        options: {
            animation: {
                duration: 500 // Reducir la duración de las animaciones
            },
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            const tipo = datosTipoAsiento[context.dataIndex];
                            const value = context.raw;
                            const total = context.dataset.data.reduce((a, b) => a + b, 0);
                            const percentage = Math.round((value / total) * 100);
                            return [
                                `${tipo.tipo}: ${formatoMoneda.format(value)} (${percentage}%)`,
                                `Entradas: ${tipo.totalEntradas}`
                            ];
                        }
                    }
                },
                legend: {
                    position: 'right',
                    labels: {
                        padding: 20,
                        font: {
                            size: 12
                        },
                        generateLabels: function(chart) {
                            const labels = Chart.defaults.plugins.legend.labels.generateLabels(chart);
                            labels.forEach((label, i) => {
                                const tipo = datosTipoAsiento[i];
                                const ingresos = formatoMoneda.format(tipo.totalIngresos);
                                label.text = `${label.text}: ${ingresos}`;
                            });
                            return labels;
                        }
                    }
                }
            }
        }
    });
}

// Función para crear el gráfico de ventas por día
function crearGraficoPorDia() {
    const canvas = document.getElementById('chartPorDia');
    if (!canvas || datosPorDia.length === 0) return;
    
    const ctx = canvas.getContext('2d');
    
    const labels = datosPorDia.map(dia => dia.fecha || 'Fecha desconocida');
    const dataIngresos = datosPorDia.map(dia => dia.totalIngresos || 0);
    const dataEntradas = datosPorDia.map(dia => dia.totalEntradas || 0);
    
    // Calcular valores máximos para ejes Y
    const maxIngreso = Math.max(...dataIngresos) * 1.2;
    const maxEntradas = Math.max(...dataEntradas) * 1.2;
    
    // Crear gráfico
    if (chartPorDia) {
        chartPorDia.destroy();
    }
    
    chartPorDia = new Chart(ctx, {
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
                    fill: true,
                    yAxisID: 'y',
                    tension: 0.3,
                    pointRadius: 4,
                    pointHoverRadius: 6
                },
                {
                    label: 'Entradas',
                    data: dataEntradas,
                    backgroundColor: 'rgba(33, 150, 243, 0.2)',
                    borderColor: 'rgba(33, 150, 243, 1)',
                    borderWidth: 2,
                    fill: true,
                    yAxisID: 'y1',
                    tension: 0.3,
                    pointRadius: 4,
                    pointHoverRadius: 6
                }
            ]
        },
        options: {
            animation: {
                duration: 500 // Reducir la duración de las animaciones
            },
            responsive: true,
            maintainAspectRatio: false,
            interaction: {
                mode: 'index',
                intersect: false
            },
            scales: {
                y: {
                    type: 'linear',
                    display: true,
                    position: 'left',
                    beginAtZero: true,
                    max: maxIngreso,
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
                    max: maxEntradas,
                    title: {
                        display: true,
                        text: 'Entradas'
                    },
                    grid: {
                        drawOnChartArea: false
                    }
                },
                x: {
                    grid: {
                        color: 'rgba(0, 0, 0, 0.1)'
                    },
                    ticks: {
                        maxRotation: 45,
                        minRotation: 45
                    }
                }
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            const label = context.dataset.label || '';
                            const value = context.raw;
                            if (label === 'Ingresos') {
                                return label + ': ' + formatoMoneda.format(value);
                            }
                            return label + ': ' + value;
                        }
                    }
                },
                legend: {
                    position: 'top',
                    align: 'center',
                    labels: {
                        boxWidth: 15,
                        padding: 15
                    }
                }
            }
        }
    });
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
    
    // Modificar el HTML para incluir contenedores para los canvas
    prepararContenedoresGraficos();
    
    // Inicializar gráficos solo si tenemos datos
    if (peliculasOk || tiposOk || diasOk) {
        // Usar un pequeño retraso para asegurar que el DOM está actualizado
        setTimeout(() => {
            inicializarGraficos();
        }, 100);
    }
}

// Función para preparar los contenedores de gráficos
function prepararContenedoresGraficos() {
    // Preparar contenedor para gráfico de películas
    prepararContenedor('chartPeliculas', 'chart-peliculas-container');
    
    // Preparar contenedor para gráfico de tipos de asiento
    prepararContenedor('chartTipoAsiento', 'chart-tipoasiento-container');
    
    // Preparar contenedor para gráfico por día
    prepararContenedor('chartPorDia', 'chart-pordia-container');
}

// Función auxiliar para preparar un contenedor de gráfico
function prepararContenedor(canvasId, containerId) {
    const canvas = document.getElementById(canvasId);
    if (!canvas) return;
    
    const parent = canvas.parentElement;
    
    // Si ya existe el contenedor, no hacer nada
    if (parent.id === containerId) return;
    
    // Crear contenedor
    const container = document.createElement('div');
    container.id = containerId;
    container.style.width = '100%';
    container.style.height = '100%';
    
    // Reemplazar el canvas con el contenedor
    parent.replaceChild(container, canvas);
    
    // Añadir el canvas al contenedor
    container.appendChild(canvas);
}

// Iniciar cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', inicializar);