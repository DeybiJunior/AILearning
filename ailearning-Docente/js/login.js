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
const db = firebase.firestore(); // Inicializa Firestore

// Manejar el inicio de sesión
const loginForm = document.getElementById('loginForm');

loginForm.addEventListener('submit', (e) => {
    e.preventDefault();
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    // Validar inicio de sesión en Firebase Authentication
    auth.signInWithEmailAndPassword(email, password)
        .then((userCredential) => {
            const user = userCredential.user;

            // Verificar si el usuario está en la colección users_docentes
            return db.collection('users_docentes').doc(user.uid).get(); // Cambia esto si usas un método diferente para acceder a los documentos
        })
        .then(doc => {
            if (doc.exists) {
                alert('Inicio de sesión exitoso');
                window.location.href = 'Dashboard.html'; // Redirigir a index.html
            } else {
                // El usuario no es un docente
                auth.signOut(); // Cierra la sesión
                alert('No tienes permiso para acceder.'); // Mensaje de error
            }
        })
        .catch(error => {
            alert('Error en el inicio de sesión: ' + error.message);
        });
});
