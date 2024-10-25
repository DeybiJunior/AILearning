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
const db = firebase.firestore();
const auth = firebase.auth();

// Verificar el estado de autenticación del usuario
auth.onAuthStateChanged(user => {
    if (!user) {
        // Si no hay usuario, redirigir a la página de inicio de sesión
        window.location.href = 'logindocente.html';
    } else {
        // Si hay un usuario autenticado, cargar los datos del dashboard
        cargarUsuarios();
        obtenerNombreDocente(user.uid);
    }
});

// Función para cargar los datos del dashboard
function cargarUsuarios() {
    const userTableBody = document.querySelector("#userTable tbody");

    db.collection("users").get().then(querySnapshot => {
        querySnapshot.forEach(doc => {
            const userData = doc.data();
            const userRow = document.createElement('tr');

            // Crear una fila para cada usuario
            userRow.innerHTML = `
                <td>${userData.nombres}</td>
                <td>${userData.apellidos}</td>
                <td>${userData.edad}</td>
                <td>${userData.nivel}</td>

                <td id="completed-${doc.id}">0</td>
            `;
            userTableBody.appendChild(userRow);

            // Contar las lecciones completadas (estadoFinal: true)
            db.collection(`users/${doc.id}/lecciones`).where("estadoFinal", "==", true)
                .get()
                .then(lessonSnapshot => {
                    const completedLessons = lessonSnapshot.size;
                    document.getElementById(`completed-${doc.id}`).textContent = completedLessons;
                });

            // Añadir evento de click para redirigir a detalles del alumno
            userRow.addEventListener('click', () => {
                window.location.href = `alumno.html?userId=${doc.id}`;
            });
        });
    });
}

// Función para obtener el nombre del docente
function obtenerNombreDocente(uid) {
    db.collection("users_docentes").doc(uid).get()
        .then(doc => {
            if (doc.exists) {
                const docenteData = doc.data();
                // Mostrar el nombre del docente en el dashboard
                document.getElementById('teacherName').innerText = `${docenteData.nombre} ${docenteData.apellido}`;
            } else {
                console.error("No se encontraron datos del docente");
            }
        })
        .catch(error => {
            console.error("Error al obtener los datos del docente: ", error);
        });
}

