document.addEventListener('DOMContentLoaded', async () => {
    const urlParams = new URLSearchParams(window.location.search);
    const salaId = urlParams.get('salaId');
    const filasAsientos = document.getElementById('filasAsientos');
    let asientos = [];

    const cargarSala = async () => {
        try {
            const response = await fetch(`/rest/resource/getSala/${salaId}`, {
                method: 'GET',
                headers: { 'Content-Type': 'application/json' }
            });
    
            if (response.ok) {
                const sala = await response.json();
                document.getElementById('capacidadSala').value = sala.capacidad; // Mostrar la capacidad actual
            } else {
                console.error('Error al cargar la sala:', await response.text());
            }
        } catch (error) {
            console.error('Error al conectar con el servidor:', error);
        }
    };

    // Cargar los asientos de la sala
    const cargarAsientos = async () => {
        try {
            const response = await fetch(`/rest/resource/getAsientos/${salaId}`, {
                method: 'GET',
                headers: { 'Content-Type': 'application/json' }
            });

            if (response.ok) {
                asientos = await response.json();

                // Mostrar los asientos en la interfaz
                filasAsientos.innerHTML = '';
                asientos.forEach(asiento => {
                    const asientoElement = document.createElement('div');
                    asientoElement.classList.add('asiento');
                    asientoElement.textContent = asiento.numero;

                    if (asiento.tipo === 'VIP') {
                        asientoElement.classList.add('vip');
                    } else if (asiento.tipo === 'DISCAPACITADOS') {
                        asientoElement.classList.add('discapacitados');
                    }

                    if (asiento.ocupado) {
                        asientoElement.classList.add('ocupado');
                    }

                    asientoElement.addEventListener('click', () => {
                        asiento.ocupado = !asiento.ocupado;
                        asientoElement.classList.toggle('ocupado');
                    });

                    filasAsientos.appendChild(asientoElement);
                });
            } else {
                console.error('Error al cargar los asientos:', await response.text());
            }
        } catch (error) {
            console.error('Error al conectar con el servidor:', error);
        }
    };

    // Guardar los cambios en los asientos
    const guardarCambios = async () => {
        try {
            const nuevaCapacidad = parseInt(document.getElementById('capacidadSala').value, 10);
    
            const response = await fetch(`/rest/resource/actualizarSala/${salaId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ capacidad: nuevaCapacidad, asientos })
            });
    
            if (response.ok) {
                alert('Cambios guardados correctamente.');
                const nombreUsuario = new URLSearchParams(window.location.search).get('nombreUsuario');
                window.location.href = `../administrador.html?nombreUsuario=${nombreUsuario}`;
            } else {
                console.error('Error al guardar los cambios:', await response.text());
            }
        } catch (error) {
            console.error('Error al conectar con el servidor:', error);
        }
    };

    document.getElementById('btnGuardarCambios').addEventListener('click', guardarCambios);

    // Cargar los asientos al cargar la página
    cargarSala();
    cargarAsientos();
});