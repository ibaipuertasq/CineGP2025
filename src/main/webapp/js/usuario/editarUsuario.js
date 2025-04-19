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

    // Recopila los datos del formulario
    const userData = {
        dni: document.getElementById('dni').value,
        nombre: document.getElementById('nombre').value,
        apellidos: document.getElementById('apellidos').value,
        email: document.getElementById('email').value,
        nombreUsuario: document.getElementById('nombreUsuario').value,
        contrasenya: document.getElementById('pass').value,
        direccion: document.getElementById('direccion').value,
        telefono: document.getElementById('telefono').value
    };

    try {
        const response = await fetch('/rest/resource/updateUser', {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(userData)
        });

    
        console.log("Estado de la respuesta:", response.status);
        console.log("Texto de la respuesta:", await response.text());
    
        if (response.ok) {
            alert('Usuario actualizado con éxito.');
        } else {
            const errorMessage = await response.text();
            alert(`Error al actualizar el usuario: ${errorMessage}`);
        }
    } catch (error) {
        console.error('Error al conectar con el servidor:', error);
        alert('Error al conectar con el servidor. Por favor, inténtelo más tarde.');
    }
});

botonEliminar.addEventListener('click', async function () {
    try {
        const status = await deleteUsuario();

        if (status === 200) {
            //redirection();
            alert("Usuario eliminado con éxito.");
        } else {
            alert("Error eliminando el usuario, intentelo de nuevo.");
        }
    } catch (error) {
        alert("Error al eliminar el usuario ", error);
    }
});

let deleteUsuario = async () => {
    const peticion = await fetch("http://localhost:8080/rest/resource/deleteUser/" + userId,
    {
        method: "DELETE",
        headers: {
            "Accept": "application/json",
            "Content-Type": "application/json"
        }
    });

    return peticion.status;
}




let newUsuario = async () => {
    let campos = {};
    campos.nombre = document.getElementById("nombre").value;
    campos.apellidos = document.getElementById("apellidos").value;
    campos.nombreUsuario = document.getElementById("nombreUsuario").value;
    campos.contrasenya = document.getElementById("pass").value;
    campos.email = document.getElementById("email").value;
    campos.direccion = document.getElementById("direccion").value;
    campos.telefono = document.getElementById("telefono").value;
    campos.dni = document.getElementById("dni").value;

    const peticion = await fetch("http://localhost:8080/rest/resource/register",
    {
        method: "POST",
        headers: {
            "Accept": "application/json",
            "Content-Type": "application/json"
        },
        body: JSON.stringify(campos)
    });
    return peticion.status;
}

let cargarDatosUsuario = async () => {
    try {
        const peticion = await fetch("http://localhost:8080/rest/resource/getUsuarioId/" + userId, {
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

        // Rellena los campos del formulario con los datos del usuario
        document.getElementById("nombre").value = user.nombre;
        document.getElementById("apellidos").value = user.apellidos;
        document.getElementById("nombreUsuario").value = user.nombreUsuario;
        document.getElementById("pass").value = user.contrasenya;
        document.getElementById("email").value = user.email;
        document.getElementById("direccion").value = user.direccion;
        document.getElementById("telefono").value = user.telefono;
        document.getElementById("dni").value = user.dni;

    } catch (error) {
        console.error("Error al cargar los datos del usuario:", error);
        alert("No se pudieron cargar los datos del usuario. Por favor, inténtelo más tarde.");
    }
};

function redirectionPrincipalCliente(){
    
    principalCliente.href = "../../html/cliente.html?nombreUsuario=" + userId;
}