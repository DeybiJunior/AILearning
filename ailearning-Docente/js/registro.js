const firebaseConfig = {
    apiKey: "AIzaSyB4-pMStnoDR2vcY-HDYTM8QBfWKwQDX2U",
    authDomain: "ailearning-8e9ab.firebaseapp.com",
    projectId: "ailearning-8e9ab",
    storageBucket: "ailearning-8e9ab.appspot.com",
    messagingSenderId: "519801064675",
    appId: "1:519801064675:web:54c94242246a57ed6f09d6",
    measurementId: "G-H4VKHQQVKW"
};

// Inicializar Firebase con compatibilidad
firebase.initializeApp(firebaseConfig);
const auth = firebase.auth();
const db = firebase.firestore();

const registroForm = document.getElementById('registroForm');

registroForm.addEventListener('submit', (e) => {
    e.preventDefault();
    const nombre = document.getElementById('nombre').value;
    const apellido = document.getElementById('apellido').value;
    const colegio = document.getElementById('colegio').value;
    const codigoModular = document.getElementById('codigoIE').value; // Cambiado a 'codigoIE'
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const repeatPassword = document.getElementById('repeatPassword').value;

    // Validar contraseñas
    const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;

    if (password !== repeatPassword) {
        alert('Las contraseñas no coinciden.');
        return;
    }
    if (!passwordPattern.test(password)) {
        alert('La contraseña debe tener al menos 8 caracteres, una letra mayúscula, una letra minúscula y un número.');
        return;
    }

    // Registrar en Firebase Authentication
    auth.createUserWithEmailAndPassword(email, password)
        .then((userCredential) => {
            const user = userCredential.user;
            db.collection('users_docentes').doc(user.uid).set({
                nombre: nombre,
                apellido: apellido,
                colegio: colegio,
                codigoModular: codigoModular,
                email: email
            })
            .then(() => {
                alert('Docente registrado correctamente');
                window.location.href = 'logindocente.html';
            })
            .catch(error => {
                console.error('Error al guardar los datos: ', error);
            });
        })
        .catch(error => {
            console.error('Error en el registro: ', error.message);
        });
});
