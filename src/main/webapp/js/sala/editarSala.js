document.addEventListener('DOMContentLoaded', async () => {
    const urlParams = new URLSearchParams(window.location.search);
    const salaId = urlParams.get('salaId');
    const filasAsientos = document.getElementById('filasAsientos');
    let asientos = [];

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
            const response = await fetch(`/rest/resource/actualizarAsientos/${salaId}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(asientos)
            });

            if (response.ok) {
                alert('Cambios guardados correctamente.');
                const urlParams = new URLSearchParams(window.location.search);
                const nombreUsuario = urlParams.get('nombreUsuario'); // Obtén el nombre de usuario de la URL
                window.location.href = `../administrador.html?nombreUsuario=${nombreUsuario}`; // Redirige con el nombre de usuario
            } else {
                console.error('Error al guardar los cambios:', await response.text());
            }
        } catch (error) {
            console.error('Error al conectar con el servidor:', error);
        }
    };

    document.getElementById('btnGuardarCambios').addEventListener('click', guardarCambios);

    // Cargar los asientos al cargar la página
    cargarAsientos();
});