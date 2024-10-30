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

window.onload = function() {
    const userId = auth.currentUser.uid; // Obtener el ID del usuario actual

    // Cargar los datos del docente desde Firestore
    db.collection('users_docentes').doc(userId).get()
        .then((doc) => {
            if (doc.exists) {
                const data = doc.data();
                document.getElementById('teacherNombre').innerText = data.nombre || 'Sin nombre';
                document.getElementById('teacherApellido').innerText = data.apellido || 'Sin apellido';
                document.getElementById('teacherEmail').innerText = data.email || 'Sin email';
            } else {
                console.log('No se encontraron datos del docente');
            }
        })
        .catch((error) => {
            console.error('Error al obtener los datos: ', error);
        });
};

function editField(field) {
    const currentValue = document.getElementById(`teacher${capitalizeFirstLetter(field)}`).innerText;
    const newValue = prompt(`Edita tu ${field}:`, currentValue);

    if (newValue !== null && newValue.trim() !== "") {
        // Actualizar el valor en la interfaz
        document.getElementById(`teacher${capitalizeFirstLetter(field)}`).innerText = newValue;

        // Actualizar el valor en Firebase
        const userId = firebase.auth().currentUser.uid; // Obtén el ID del usuario actual
        db.collection('users_docentes').doc(userId).update({
            [field]: newValue
        })
        .then(() => {
            alert(`${capitalizeFirstLetter(field)} actualizado correctamente.`);
        })
        .catch((error) => {
            console.error('Error al actualizar el dato: ', error);
            alert('Error al actualizar el dato.');
        });
    }
}

function capitalizeFirstLetter(string) {
    return string.charAt(0).toUpperCase() + string.slice(1);
}
