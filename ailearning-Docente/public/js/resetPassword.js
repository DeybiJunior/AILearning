import firebaseConfig from './firebaseConfig.js';

document.addEventListener('DOMContentLoaded', function () {
    // Inicializar Firebase
    firebase.initializeApp(firebaseConfig);
    const auth = firebase.auth();

    // Manejar la recuperación de contraseña
    const resetForm = document.getElementById('resetForm');
    const alertContainer = document.getElementById('alert-container');

    resetForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const email = document.getElementById('email').value;

        if (email) {
            auth.sendPasswordResetEmail(email)
                .then(() => {
                    sessionStorage.setItem('logoutMessage', 'Se ha enviado un enlace para restablecer la contraseña a ' + email); // Guardar mensaje en sessionStorage
                    sessionStorage.setItem('messageType', 'success'); // Guardar tipo de mensaje
                    window.location.href = 'logindocente.html'; // Redirigir a login
                })
                .catch(error => {
                    showAlert('Error al enviar el correo de restablecimiento: ' + error.message, 'error');
                });
        } else {
            showAlert('Por favor, ingresa tu correo electrónico.', 'error');
        }
    });

    function showAlert(message, isSuccess = false) {
        const alertContainer = document.getElementById('alert-container');
        alertContainer.textContent = message;
        alertContainer.className = isSuccess ? 'success' : ''; // Aplica la clase "success" si es un mensaje positivo
        alertContainer.style.display = 'block';
    
        // Ocultar el mensaje después de 3 segundos
        setTimeout(() => {
            alertContainer.style.display = 'none';
        }, 5000);
    }
});
