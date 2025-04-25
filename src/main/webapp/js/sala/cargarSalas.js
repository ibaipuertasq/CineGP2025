document.addEventListener('DOMContentLoaded', async () => {
    const tablaSalasBody = document.querySelector('#tablaSalas tbody');

    // Función para cargar las salas
    const cargarSalas = async () => {
        try {
            const response = await fetch('/rest/resource/getSalas', {
                method: 'GET',
                headers: { 'Content-Type': 'application/json' }
            });

            if (response.ok) {
                const salas = await response.json();

                // Limpia la tabla
                tablaSalasBody.innerHTML = '';

                // Rellena la tabla con las salas obtenidas
                salas.forEach((sala, index) => {
                    const row = document.createElement('tr');
                    row.classList.add(sala.disponible ? 'sala-disponible' : 'sala-no-disponible');

                    row.innerHTML = `
                        <td>${index + 1}</td>
                        <td>${sala.numero}</td>
                        <td>${sala.capacidad}</td>
                        <td>${sala.disponible ? 'Disponible' : 'No Disponible'}</td>
                        <td>
                            <button class="btn-accion ${sala.disponible ? 'btn-no-disponible' : 'btn-disponible'}" 
                                data-id="${sala.id}" 
                                onclick="cambiarEstadoSala(${sala.id}, ${!sala.disponible})">
                                ${sala.disponible ? 'Marcar como No Disponible' : 'Marcar como Disponible'}
                            </button>
                            <button class="btn-accion btn-editar" data-id="${sala.id}" onclick="editarSala(${sala.id})">
                                Editar Sala
                            </button>
                        </td>
                    `;

                    tablaSalasBody.appendChild(row);
                });

                console.log('Salas cargadas correctamente.');
            } else {
                console.error('Error al obtener las salas:', await response.text());
            }
        } catch (error) {
            console.error('Error al conectar con el servidor:', error);
        }
    };

    // Función para cambiar el estado de una sala
    window.cambiarEstadoSala = async (salaId, nuevoEstado) => {
        try {
            const response = await fetch(`/rest/resource/cambiarEstadoSala/${salaId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ disponible: nuevoEstado })
            });

            if (response.ok) {
                console.log(`Estado de la sala ${salaId} cambiado a ${nuevoEstado ? 'Disponible' : 'No Disponible'}`);
                cargarSalas(); // Recargar la tabla
            } else {
                console.error('Error al cambiar el estado de la sala:', await response.text());
            }
        } catch (error) {
            console.error('Error al conectar con el servidor:', error);
        }
    };

    window.editarSala = (salaId) => {
        const userId = new URLSearchParams(window.location.search).get('nombreUsuario'); // Obtén el nombreUsuario de la URL actual
        window.location.href = `sala/editarSala.html?nombreUsuario=${userId}&salaId=${salaId}`;
    };

    document.getElementById('btnAgregarSala').addEventListener('click', () => {
        const userId = new URLSearchParams(window.location.search).get('nombreUsuario'); // Obtén el nombreUsuario de la URL actual
        window.location.href = `sala/agregarSala.html?nombreUsuario=${userId}`;
    });

    // Cargar las salas al cargar la página
    cargarSalas();
});