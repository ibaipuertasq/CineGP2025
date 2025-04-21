window.onload = function() {
    listarPeliculas();
}


let listarPeliculas = async () => {
    const peticion = await fetch("http://localhost:8080/rest/resource/getPeliculas",
    {
        method: "GET",
        headers: {
            "Accept": "application/json",
            "Content-Type": "application/json"
        }
    });

    const peliculas = await peticion.json();

    let contenidoTabla = "";

    if(peliculas.length > 0) {
        for(let pelicula of peliculas) {
            let contenidoFila = 
            `<tr>
                <td>${pelicula.titulo}</td>
                <td>${pelicula.genero}</td>
                <td>${pelicula.duracion}</td>
                <td>${pelicula.sala}</td>
                <td>${pelicula.horario}</td>
                <td>
                    <i class="material-icons button edit">edit</i>
                    <i onClick="eliminarPelicula(${pelicula.id})"class="material-icons button delete">delete</i>
                </td>
            <tr>`
    
            contenidoTabla += contenidoFila;
        }
    } else {
        contenidoTabla += 
        `<tr>
            <td colspan="5">No hay peliculas registradas</td>
        </tr>`
    }

    document.querySelector("#tablaPeliculasBody").innerHTML = contenidoTabla;
}

let eliminarPelicula = async (id ) => {
    const peticion = await fetch("http://localhost:8080/rest/resource/eliminarPelicula/"+id,
    {
        method: "DELETE",
        headers: {
            "Acept": "application/json",
            "Content-Type": "application/json"
        }
    });

    listarPeliculas();
}