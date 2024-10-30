document.addEventListener('DOMContentLoaded', function () {
    // Configuración de Firebase
    const firebaseConfig = {
        apiKey: "AIzaSyB4-pMStnoDR2vcY-HDYTM8QBfWKwQDX2U",
        authDomain: "ailearning-8e9ab.firebaseapp.com",
        projectId: "ailearning-8e9ab",
        storageBucket: "ailearning-8e9ab.appspot.com",
        messagingSenderId: "519801064675",
        appId: "1:519801064675:web:54c94242246a57ed6f09d6",
        measurementId: "G-H4VKHQQVKW"
    };

    // Inicializar Firebase
    firebase.initializeApp(firebaseConfig);
    const auth = firebase.auth();

    // Manejar la recuperación de contraseña
    const resetForm = document.getElementById('resetForm');

    resetForm.addEventListener('submit', (e) => {
        e.preventDefault();
        const email = document.getElementById('email').value;

        if (email) {
            auth.sendPasswordResetEmail(email)
                .then(() => {
                    alert('Se ha enviado un enlace para restablecer la contraseña a ' + email);
                    window.location.href = 'logindocente.html'; // Redirigir a login
                })
                .catch(error => {
                    alert('Error al enviar el correo de restablecimiento: ' + error.message);
                });
        } else {
            alert('Por favor, ingresa tu correo electrónico.');
        }
    });
});
