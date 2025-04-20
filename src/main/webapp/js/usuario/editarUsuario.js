window.onload = function () {
    cargarDatosUsuario();
    redirectionPrincipalCliente();
}

const principalCliente = document.getElementById("principalCliente");
const botonEditUsuario = document.getElementById('botonEditUsuario');
const botonEliminar = document.getElementById('botonDeleteUsuario');
const urlParams = new URLSearchParams(window.location.search);
const userId = urlParams.get('nombreUsuario');

botonEditUsuario.addEventListener('click', async (e) => {
    e.preventDefault();

    const currentPassword = document.getElementById('currentPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    // Validar campos
    if (!currentPassword || !newPassword || !confirmPassword) {
        showMessage('error', 'Todos los campos son obligatorios');
        return;
    }

    // Validar que las contraseñas coincidan
    if (newPassword !== confirmPassword) {
        showMessage('error', 'Las contraseñas no coinciden');
        return;
    }

    // Validar requisitos de seguridad de contraseña
    if (newPassword.length < 8) {
        showMessage('error', 'La contraseña debe tener al menos 8 caracteres');
        return;
    }

    // Recopila los datos del usuario (incluyendo la nueva contraseña)
    const userData = {
        dni: document.getElementById('dni').value,
        nombre: document.getElementById('nombre').value,
        apellidos: document.getElementById('apellidos').value,
        email: document.getElementById('email').value,
        nombreUsuario: document.getElementById('nombreUsuario').value,
        contrasenya: newPassword, // Usar la nueva contraseña
        direccion: document.getElementById('direccion').value,
        telefono: document.getElementById('telefono').value,
        tipoUsuario: "CLIENTE" // Asumimos que es cliente para este caso
    };

    try {
        const response = await fetch('/rest/resource/updateUser', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(userData)
        });

        if (response.ok) {
            showMessage('success', 'Contraseña actualizada con éxito');
            // Limpiar los campos de contraseña
            document.getElementById('currentPassword').value = '';
            document.getElementById('newPassword').value = '';
            document.getElementById('confirmPassword').value = '';
        } else {
            const errorMessage = await response.text();
            showMessage('error', `Error al actualizar la contraseña: ${errorMessage}`);
        }
    } catch (error) {
        console.error('Error al conectar con el servidor:', error);
        showMessage('error', 'Error al conectar con el servidor. Por favor, inténtelo más tarde.');
    }
});

botonEliminar.addEventListener('click', async function () {
    if (confirm('¿Está seguro de que desea eliminar su cuenta? Esta acción no se puede deshacer.')) {
        try {
            const status = await deleteUsuario();
            if (status === 200) {
                showMessage('success', "Usuario eliminado con éxito.");
                setTimeout(() => {
                    window.location.href = "../../index.html";
                }, 1500);
            } else {
                showMessage('error', "Error eliminando el usuario, inténtelo de nuevo.");
            }
        } catch (error) {
            showMessage('error', "Error al eliminar el usuario: " + error);
        }
    }
});

async function deleteUsuario() {
    const peticion = await fetch("/rest/resource/deleteUser/" + userId, {
        method: "DELETE",
        headers: {
            "Accept": "application/json",
            "Content-Type": "application/json"
        }
    });
    return peticion.status;
}

async function cargarDatosUsuario() {
    try {
        const peticion = await fetch("/rest/resource/getUsuarioId/" + userId, {
            method: "GET",
            headers: {
                "Accept": "application/json",
                "Content-Type": "application/json"
            }
        });

        if (!peticion.ok) {
            throw new Error("Error al obtener los datos del usuario: " + peticion.status);
        }

        const user = await peticion.json();

        // Rellena los campos ocultos con los datos del usuario
        document.getElementById("nombre").value = user.nombre;
        document.getElementById("apellidos").value = user.apellidos;
        document.getElementById("nombreUsuario").value = user.nombreUsuario;
        document.getElementById("email").value = user.email;
        document.getElementById("direccion").value = user.direccion;
        document.getElementById("telefono").value = user.telefono;
        document.getElementById("dni").value = user.dni;

    } catch (error) {
        console.error("Error al cargar los datos del usuario:", error);
        showMessage('error', "No se pudieron cargar los datos del usuario. Por favor, inténtelo más tarde.");
    }
}

function redirectionPrincipalCliente() {
    principalCliente.href = "../../html/cliente.html?nombreUsuario=" + userId;
}

// Función para mostrar mensajes al usuario
function showMessage(type, message) {
    // Verificar si ya existe un mensaje y eliminarlo
    const existingMessage = document.querySelector('.message');
    if (existingMessage) {
        existingMessage.remove();
    }
    
    // Crear nuevo mensaje
    const messageDiv = document.createElement('div');
    messageDiv.className = `message ${type}`;
    messageDiv.textContent = message;
    
    // Insertar después del formulario
    const form = document.getElementById('passwordForm');
    form.insertAdjacentElement('afterend', messageDiv);
    
    // Eliminar el mensaje después de 3 segundos
    setTimeout(() => {
        messageDiv.remove();
    }, 3000);
}

// Añadir estilos para mensajes
const style = document.createElement('style');
style.textContent = `
    .message {
        margin-top: 1rem;
        padding: 0.75rem;
        border-radius: 4px;
        text-align: center;
        animation: fadeIn 0.3s;
    }
    
    .success {
        background-color: #d4edda;
        color: #155724;
        border: 1px solid #c3e6cb;
    }
    
    .error {
        background-color: #f8d7da;
        color: #721c24;
        border: 1px solid #f5c6cb;
    }
    
    @keyframes fadeIn {
        from { opacity: 0; transform: translateY(-10px); }
        to { opacity: 1; transform: translateY(0); }
    }
`;
document.head.appendChild(style);