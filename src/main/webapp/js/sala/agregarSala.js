document.addEventListener('DOMContentLoaded', () => {
    const formAgregarSala = document.getElementById('formAgregarSala');

    formAgregarSala.addEventListener('submit', async (event) => {
        event.preventDefault(); // Evita que el formulario se envíe de forma predeterminada

        const numeroSala = document.getElementById('numeroSala').value;
        const capacidadSala = document.getElementById('capacidadSala').value;
        const disponibleSala = document.getElementById('disponibleSala').value === 'true';

        const nuevaSala = {
            numero: parseInt(numeroSala, 10),
            capacidad: parseInt(capacidadSala, 10),
            disponible: disponibleSala
        };

        try {
            const response = await fetch('/rest/resource/agregarSala', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(nuevaSala)
            });

            if (response.ok) {
                alert('Sala agregada correctamente.');
                const userId = new URLSearchParams(window.location.search).get('nombreUsuario');
                window.location.href = `../administrador.html?nombreUsuario=${userId}`;
            } else {
                const errorText = await response.text();
                console.error('Error al agregar la sala:', errorText);
                alert('Error al agregar la sala: ' + errorText);
            }
        } catch (error) {
            console.error('Error al conectar con el servidor:', error);
            alert('Error al conectar con el servidor.');
        }
    });
});