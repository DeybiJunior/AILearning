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

// Obtener el ID del usuario desde la URL
const params = new URLSearchParams(window.location.search);
const userId = params.get('userId');

// Mostrar los datos del alumno
const studentInfoDiv = document.getElementById('studentInfo');
db.collection("users").doc(userId).get().then(doc => {
    if (doc.exists) {
        const userData = doc.data();
        studentInfoDiv.innerHTML = `
            <p><strong>Nombre:</strong> ${userData.nombres}</p>
            <p><strong>Apellido:</strong> ${userData.apellidos}</p>
            <p><strong>Edad:</strong> ${userData.edad}</p>
        `;
    } else {
        studentInfoDiv.innerHTML = `<p>Usuario no encontrado.</p>`;
    }
}).catch(error => {
    console.error("Error al obtener el usuario:", error);
});

// Mostrar las lecciones del alumno
const lessonsTableBody = document.querySelector("#lessonsTable tbody");
const lessonsTable = document.getElementById('lessonsTable');
const lessonsChartCanvas = document.getElementById('lessonsChart');
const tableViewButton = document.getElementById('tableViewButton');
const chartViewButton = document.getElementById('chartViewButton');

// Mostrar la tabla al cargar
lessonsTable.style.display = 'table';
lessonsChartCanvas.style.display = 'none';

// Función para mostrar la tabla
function showTable() {
    lessonsTable.style.display = 'table';
    lessonsChartCanvas.style.display = 'none';
}

// Función para mostrar el gráfico
function showChart() {
    lessonsTable.style.display = 'none';
    lessonsChartCanvas.style.display = 'block';
}

// Agregar eventos a los botones
tableViewButton.addEventListener('click', showTable);
chartViewButton.addEventListener('click', showChart);

// Mostrar las lecciones del alumno y renderizar el gráfico
db.collection(`users/${userId}/lecciones`).get().then(querySnapshot => {
    const lessons = [];
    if (querySnapshot.empty) {
        lessonsTableBody.innerHTML = `<tr><td colspan="7">No hay lecciones disponibles.</td></tr>`;
    } else {
        querySnapshot.forEach(doc => {
            const lessonData = doc.data();
            lessons.push(lessonData); // Agregar lección a la lista
            const lessonRow = document.createElement('tr');
            lessonRow.innerHTML = `
                <td>${lessonData.idLeccion}</td>
                <td>${lessonData.tipo}</td>
                <td>${lessonData.dificultad}</td>
                <td>${lessonData.tema}</td>
                <td>${lessonData.estado? 'Completado' : 'En Proceso'}</td>
                <td>${lessonData.puntaje}</td>
                <td>${lessonData.json}</td>
            `;
            lessonsTableBody.appendChild(lessonRow);
        });
    }
    // Renderizar el gráfico después de cargar las lecciones
    renderLessonsChart(lessons);
}).catch(error => {
    console.error("Error al obtener las lecciones:", error);
});

// Función para renderizar el gráfico
function renderLessonsChart(lessons) {
    const labels = lessons.map(lesson => lesson.tema); // Extrae los temas de las lecciones
    const data = lessons.map(lesson => lesson.puntaje); // Cambiado a puntaje

    console.log(lessons); // Para verificar qué datos se están pasando

    const ctx = lessonsChartCanvas.getContext('2d');

    if (labels.length === 0 || data.length === 0) {
        console.error("No hay datos para renderizar el gráfico");
        return; // Evitar renderizar si no hay datos
    }

    const lessonsChart = new Chart(ctx, {
        type: 'bar', // Tipo de gráfico (puedes cambiar a 'line' para un gráfico de líneas)
        data: {
            labels: labels,
            datasets: [{
                label: 'Puntaje Final',
                data: data,
                backgroundColor: 'rgba(75, 192, 192, 0.2)',
                borderColor: 'rgba(75, 192, 192, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });
}
