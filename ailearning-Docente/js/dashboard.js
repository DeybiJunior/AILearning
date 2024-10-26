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
        cargarUsuarios(user.uid);
        obtenerNombreDocente(user.uid);
    }
});

// Función para cargar los datos del dashboard
// Función para cargar los datos del dashboard
// Función para cargar los datos del dashboard
function cargarUsuarios(docenteId) {
    const userTableBody = document.querySelector("#userTable tbody");
    const searchName = document.getElementById("searchName");
    const filterGrado = document.getElementById("filterGrado");
    const filterSeccion = document.getElementById("filterSeccion");

    // Filtrar usuarios por docente_id igual al del usuario autenticado
    db.collection("users").where("docente_id", "==", docenteId).get().then(querySnapshot => {
        const usuarios = []; // Almacenar usuarios para filtrar después
        
        querySnapshot.forEach(doc => {
            const userData = doc.data();
            usuarios.push({ id: doc.id, ...userData }); // Almacenar ID junto con datos
            
            const userRow = document.createElement('tr');
            userRow.innerHTML = `
                <td>${userData.nombres}</td>
                <td>${userData.apellidos}</td>
                <td>${userData.edad}</td>
                <td>${userData.nivel}</td>
                <td>${userData.grado}</td>
                <td>${userData.seccion}</td>
                <td id="completed-${doc.id}">0</td>
            `;
            userTableBody.appendChild(userRow);
            
            // Contar lecciones completadas (estadoFinal: true)
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

        // Manejar el botón de aplicar filtros
        document.getElementById("applyFilters").addEventListener("click", () => {
            // Filtrar y mostrar usuarios según el nombre, grado y sección
            const nombreBuscado = searchName.value.toLowerCase();
            const gradoSeleccionado = filterGrado.value;
            const seccionSeleccionada = filterSeccion.value;

            // Limpiar la tabla antes de aplicar los filtros
            userTableBody.innerHTML = '';

            usuarios.forEach(user => {
                const nombreCompleto = `${user.nombres} ${user.apellidos}`.toLowerCase();
                const nombreCoincide = nombreCompleto.includes(nombreBuscado);
                const gradoCoincide = gradoSeleccionado ? user.grado === gradoSeleccionado : true;
                const seccionCoincide = seccionSeleccionada ? user.seccion === seccionSeleccionada : true;

                // Mostrar el usuario solo si cumple con los criterios de búsqueda
                if (nombreCoincide && gradoCoincide && seccionCoincide) {
                    const userRow = document.createElement('tr');
                    userRow.innerHTML = `
                        <td>${user.nombres}</td>
                        <td>${user.apellidos}</td>
                        <td>${user.edad}</td>
                        <td>${user.nivel}</td>
                        <td>${user.grado}</td>
                        <td>${user.seccion}</td>
                        <td id="completed-${user.id}">0</td>
                    `;
                    userTableBody.appendChild(userRow);

                    // Contar lecciones completadas para el usuario filtrado
                    db.collection(`users/${user.id}/lecciones`).where("estadoFinal", "==", true)
                        .get()
                        .then(lessonSnapshot => {
                            const completedLessons = lessonSnapshot.size;
                            document.getElementById(`completed-${user.id}`).textContent = completedLessons;
                        });

                    // Añadir evento de click para redirigir a detalles del alumno
                    userRow.addEventListener('click', () => {
                        window.location.href = `alumno.html?userId=${user.id}`;
                    });
                }
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

