// Obtiene el parámetro 'nombreUsuario' de la URL
const urlParams = new URLSearchParams(window.location.search);
const userId = urlParams.get('nombreUsuario');

// Función para redirigir a la página de administrador con el nombre de usuario
function redirectionEditar(nombreUsuario) {
    location.href = "../html/usuario/editarUsuario.html" + "?nombreUsuario=" + nombreUsuario;
}

// Evento para manejar el clic en el botón
document.getElementById('editarUsuario').addEventListener('click', (e) => {
    e.preventDefault(); // Previene el comportamiento predeterminado
    if (userId) {
        redirectionEditar(userId); // Pasa el nombre de usuario obtenido de la URL
    } else {
        console.error("No se encontró el parámetro 'nombreUsuario' en la URL.");
    }
});