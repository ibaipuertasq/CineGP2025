const boton = document.getElementById('btnNewEntrada');

const urlParams = new URLSearchParams(window.location.search);
const eventId = urlParams.get('idPelicula');
const userId = urlParams.get('nombreUsuario');

const redireccion = document.getElementById('ventanaPrincipal');

redireccion.addEventListener('click', redirection);

function redirection() {
    location.href = "../../html/principalCliente.html?nombreUsuario=" + userId;
}

boton.addEventListener('click', async function () {
    try {
        const status = await newEntrada();

        if (status === 200) {
            redirection();
        } else {
            alert("Error comprando la entrada, intentelo de nuevo.");
        }
    } catch (error) {
        alert("Error al comprar la entrada ", error);
    }
});

let newEntrada = async () => {
    sector = document.getElementById("opciones").value;
    cantidad = document.getElementById("cantidad").value;

    const peticion = await fetch("http://localhost:8080/rest/resource/comprarEntrada/" + eventId + "/" + sector + "/" + cantidad,
    {

        method: "GET",
        headers: {
            "Accept": "application/json",
            "Content-Type": "application/json"
        }
    });

    return peticion.status;
}