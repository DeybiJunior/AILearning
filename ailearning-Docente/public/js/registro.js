import firebaseConfig from './firebaseConfig.js';  // Asegúrate de que la ruta sea correcta


// Initialize Firebase
firebase.initializeApp(firebaseConfig);
const auth = firebase.auth();
const db = firebase.firestore();

const registroForm = document.getElementById('registroForm');
const alertContainer = document.getElementById('alert-container');

registroForm.addEventListener('submit', (e) => {
    e.preventDefault();
    const nombre = document.getElementById('nombre').value;
    const apellido = document.getElementById('apellido').value;
    const colegio = document.getElementById('colegio').value;
    const codigoModular = document.getElementById('codigoIE').value; // Changed to 'codigoIE'
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const repeatPassword = document.getElementById('repeatPassword').value;

    // Clear error messages
    document.getElementById('nombreError').textContent = '';
    document.getElementById('apellidoError').textContent = '';
    document.getElementById('colegioError').textContent = '';
    document.getElementById('codigoIEError').textContent = '';
    document.getElementById('emailError').textContent = '';
    document.getElementById('passwordError').textContent = '';

    // Name validation
    if (!nombre) {
        document.getElementById('nombreError').textContent = 'El nombre es obligatorio';
        return false;
    } else if (nombre.length < 3) {
        document.getElementById('nombreError').textContent = 'El nombre es demasiado corto';
        return false;
    } else if (nombre.length > 50) {
        document.getElementById('nombreError').textContent = 'El nombre es demasiado largo';
        return false;
    } else if (!/^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$/.test(nombre)) {
        document.getElementById('nombreError').textContent = 'El nombre contiene caracteres inválidos';
        return false;
    }

    // Last name validation
    if (!apellido) {
        document.getElementById('apellidoError').textContent = 'El apellido es obligatorio';
        return false;
    } else if (apellido.length < 3) {
        document.getElementById('apellidoError').textContent = 'El apellido es demasiado corto';
        return false;
    } else if (apellido.length > 50) {
        document.getElementById('apellidoError').textContent = 'El apellido es demasiado largo';
        return false;
    } else if (!/^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$/.test(apellido)) {
        document.getElementById('apellidoError').textContent = 'El apellido contiene caracteres inválidos';
        return false;
    }

    // Colegio validation (5-40 characters)
    if (colegio.length < 5) {
        document.getElementById('colegioError').textContent = 'El nombre de colegio es demasiado corto';
        return false;
    } else if (colegio.length > 40) {
        document.getElementById('colegioError').textContent = 'El nombre de colegio es demasiado largo';
        return false;
    }

    // Código Modular validation (4-10 characters)
    if (codigoModular.length < 5 || codigoModular.length > 10 || !/^\d+$/.test(codigoModular)) {
        document.getElementById('codigoIEError').textContent = 'El código modular no es valido';
        return false;
    } 

    // Email validation
    if (!email) {
        document.getElementById('emailError').textContent = 'El correo electrónico es obligatorio';
        return false;
    } else if (!/^[a-zA-Z][a-zA-Z0-9._%+-]*@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,6}$/.test(email)) {
        document.getElementById('emailError').textContent = 'El correo electrónico es inválido';
        return false;
    }    

    // Password validation (8 characters, at least 1 uppercase and 1 lowercase letter)

    if (!password) {
        document.getElementById('passwordError').textContent = 'La contraseña es obligatoria';
        return false;
    } else if (password.length < 8) {
        document.getElementById('passwordError').textContent = 'La contraseña debe tener al menos 8 caracteres';
        return false;
    } else if (!/[a-z]/.test(password)) {  // Corregido para usar la expresión regular correctamente
        document.getElementById('passwordError').textContent = 'La contraseña debe contener al menos una letra minúscula';
        return false;
    } else if (!/[A-Z]/.test(password)) {  // Corregido para usar la expresión regular correctamente
        document.getElementById('passwordError').textContent = 'La contraseña debe contener al menos una letra mayúscula';
        return false;
    }

    // Password match validation
    if (password !== repeatPassword) {
        showAlert('Las contraseñas no coinciden', 'error');
        return false;
    }

    // Register user in Firebase Authentication
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
            sessionStorage.setItem('logoutMessage', 'Docente registrado correctamente');
            sessionStorage.setItem('messageType', 'success');
            window.location.href = 'logindocente.html';
        })
        .catch(error => {
            showAlert('Error al guardar los datos: ' + error.message, 'error');
        });
    })
    .catch(error => {
        showAlert('Error en el registro: ' + error.message, 'error');
    });
});




function showAlert(message, type) {
    alertContainer.textContent = message;
    alertContainer.classList.remove('success', 'error');
    if (type === 'success') {
        alertContainer.classList.add('success');
    } else {
        alertContainer.classList.add('error');
    }
    alertContainer.style.display = 'block';
    setTimeout(() => {
        alertContainer.style.display = 'none';
    }, 5000);
}