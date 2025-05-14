// Función para cargar las estadísticas de ocupación
async function cargarEstadisticas() {
    try {
        const response = await fetch("http://localhost:8080/rest/resource/getSalas", {
            method: "GET",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/json"
            }
        });

        if (!response.ok) {
            throw new Error(`Error al cargar las estadísticas: ${response.status}`);
        }

        const salas = await response.json();
        mostrarEstadisticas(salas);
    } catch (error) {
        console.error("Error al cargar las estadísticas:", error);
        const tablaBody = document.querySelector("#tablaEstadisticas tbody");
        tablaBody.innerHTML = `
            <tr>
                <td colspan="4">Error al cargar las estadísticas: ${error.message}</td>
            </tr>
        `;
    }
}

// Función para mostrar las estadísticas en la tabla
function mostrarEstadisticas(salas) {
    const tablaBody = document.querySelector("#tablaEstadisticas tbody");
    tablaBody.innerHTML = ""; // Limpiar contenido previo

    salas.forEach(sala => {
        const asientosOcupados = sala.asientos.filter(asiento => asiento.ocupado).length;
        const porcentajeOcupacion = ((asientosOcupados / sala.capacidad) * 100).toFixed(2);

        const fila = `
            <tr>
                <td>${sala.numero}</td>
                <td>${sala.capacidad}</td>
                <td>${asientosOcupados}</td>
                <td>${porcentajeOcupacion}%</td>
            </tr>
        `;
        tablaBody.innerHTML += fila;
    });
}

// Función para redirigir al administrador
function configurarBotonVolver() {
    const btnVolver = document.getElementById("btnVolver");
    const userId = new URLSearchParams(window.location.search).get("nombreUsuario");

    if (userId) {
        btnVolver.addEventListener("click", () => {
            window.location.href = `../administrador.html?nombreUsuario=${userId}`;
        });
    } else {
        console.error("No se encontró el parámetro 'nombreUsuario' en la URL.");
        btnVolver.disabled = true;
        btnVolver.textContent = "Error: No se puede volver";
    }
}

// Cargar las estadísticas y configurar el botón al cargar la página
document.addEventListener("DOMContentLoaded", () => {
    cargarEstadisticas();
    configurarBotonVolver();
});