function redirectionCliente(id){
    location.href = "../html/cliente.html" + "?id=" + id;
}

function redirectionAdministrador(id){
    location.href = "../html/administrador.html" + "?id=" + id;
}


let isLoggedIn = false;
let currentUser = null;

// Show/hide containers based on login state
function updateUI() {
    document.getElementById('loginContainer').style.display = isLoggedIn ? 'none' : 'block';
    document.getElementById('registerContainer').style.display = 'none';
    document.getElementById('loggedInContainer').style.display = isLoggedIn ? 'block' : 'none';
}

// Login Form Submission
document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;
    
    try {    
        const response = await fetch('/rest/resource/login', {
            method: "POST",
                headers: {
                    "Accept": "application/json",
                    "Content-Type": "application/json"
                },
            body: JSON.stringify({
                nombreUsuario: username,
                contrasenya: password
            })
        });

        const messageDiv = document.getElementById('loginMessage');
        messageDiv.style.display = 'block';

       

        if (response.ok) {
            currentUser = await response.json();
            console.log("Usuario recibido desde el backend:", currentUser);
            console.log("Tipo de usuario recibido:", currentUser.tipoUsuario);
        
            isLoggedIn = true;
            messageDiv.className = 'message success';
            messageDiv.textContent = 'Login successful!';
            
            // Redireccionar según rol
            if (currentUser.tipoUsuario === 'CLIENTE') {
                redirectionCliente(currentUser.dni);
            } else if (currentUser.tipoUsuario === 'ADMINISTRADOR') {
                redirectionAdministrador(currentUser.dni);
            }
            setTimeout(() => {
                updateUI();
                messageDiv.style.display = 'none';
            }, 500);
        } else {
            messageDiv.className = 'message error';
            messageDiv.textContent = await response.text();
        }
    } catch (error) {
        showMessage('loginMessage', 'Error connecting to server', false);
    }
});

// Show Register Form
document.getElementById('showRegisterBtn').addEventListener('click', () => {
    document.getElementById('loginContainer').style.display = 'none';
    document.getElementById('registerContainer').style.display = 'block';
});

// Back to Login
document.getElementById('backToLoginBtn').addEventListener('click', () => {
    document.getElementById('registerContainer').style.display = 'none';
    document.getElementById('loginContainer').style.display = 'block';
});

// Register Form Submission
document.getElementById('registerForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const userData = {
        dni: document.getElementById('dni').value,
        nombre: document.getElementById('nombre').value,
        apellidos: document.getElementById('apellidos').value,
        email: document.getElementById('email').value,
        nombreUsuario: document.getElementById('username').value,
        contrasenya: document.getElementById('password').value,
        direccion: document.getElementById('direccion').value,
        telefono: document.getElementById('telefono').value
    };

    try {
        const response = await fetch('/rest/resource/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(userData)
        });

        showMessage('registerMessage', 
            response.ok ? 'Registration successful! Please login.' : await response.text(),
            response.ok);
        
        if (response.ok) {
            setTimeout(() => {
                document.getElementById('registerContainer').style.display = 'none';
                document.getElementById('loginContainer').style.display = 'block';
                document.getElementById('registerMessage').style.display = 'none';
            }, 1000);
        }
    } catch (error) {
        showMessage('registerMessage', 'Error connecting to server', false);
    }
});

// Logout Button
document.getElementById('logoutBtn').addEventListener('click', async () => {
    try {
        const response = await fetch('/rest/resource/logout', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(currentUser)
        });

        if (response.ok) {
            isLoggedIn = false;
            currentUser = null;
            showMessage('logoutMessage', 'Logout successful!', true);
            setTimeout(() => {
                updateUI();
                document.getElementById('logoutMessage').style.display = 'none';
            }, 1000);
        } else {
            showMessage('logoutMessage', await response.text(), false);
        }
    } catch (error) {
        showMessage('logoutMessage', 'Error connecting to server', false);
    }
});

// Helper function to show messages
function showMessage(elementId, message, isSuccess) {
    const messageDiv = document.getElementById(elementId);
    messageDiv.style.display = 'block';
    messageDiv.className = `message ${isSuccess ? 'success' : 'error'}`;
    messageDiv.textContent = message;
}

// Initial UI setup
updateUI();
// fin script
