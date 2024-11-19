import firebaseConfig from './firebaseConfig.js';  // Asegúrate de que la ruta sea correcta

// Inicializar Firebase
firebase.initializeApp(firebaseConfig);
const auth = firebase.auth();
const db = firebase.firestore(); // Inicializa Firestore

// Manejar el inicio de sesión
const loginForm = document.getElementById('loginForm');

window.addEventListener('DOMContentLoaded', () => {
    const alertContainer = document.getElementById('alert-container');
    const message = sessionStorage.getItem('logoutMessage');
    const messageType = sessionStorage.getItem('messageType');

    if (message) {
        alertContainer.textContent = message;
        if (messageType === 'success') {
            alertContainer.classList.add('success');
        }
        alertContainer.style.display = 'block';

        // Borrar el mensaje después de mostrarlo
        sessionStorage.removeItem('logoutMessage');
        sessionStorage.removeItem('messageType');

        // Ocultar el mensaje después de 3 segundos
        setTimeout(() => {
            alertContainer.style.display = 'none';
        }, 3000);
    }
});


loginForm.addEventListener('submit', (e) => {
    e.preventDefault();

    // Mostrar el cargador
    document.getElementById('loader').style.display = 'flex';

    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    auth.signInWithEmailAndPassword(email, password)
        .then((userCredential) => {
            const user = userCredential.user;

            return db.collection('users_docentes').doc(user.uid).get();
        })
        .then(doc => {
            // Ocultar el cargador
            document.getElementById('loader').style.display = 'none';
        
            if (doc.exists) {
                sessionStorage.setItem('successMessage', 'Inicio de sesión exitoso'); // Guardar mensaje en sessionStorage
                window.location.href = 'Dashboard.html'; // Redirigir inmediatamente
            } else {
                auth.signOut();
                showAlert('No tienes permiso para acceder.');
            }
        })
        .catch(error => {
            // Ocultar el cargador en caso de error
            document.getElementById('loader').style.display = 'none';
            showAlert('Error en el inicio de sesión: ' + error.message);
        });
});


function showAlert(message, isSuccess = false) {
    const alertContainer = document.getElementById('alert-container');
    alertContainer.textContent = message;
    alertContainer.className = isSuccess ? 'success' : ''; // Aplica la clase "success" si es un mensaje positivo
    alertContainer.style.display = 'block';

    // Ocultar el mensaje después de 3 segundos
    setTimeout(() => {
        alertContainer.style.display = 'none';
    }, 3000);
}
