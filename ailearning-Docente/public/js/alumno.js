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

const lessonsCompleted = document.getElementById('lessonsCompleted');
const totalLessonsPromise = db.collection(`users/${userId}/lecciones`).get(); // Obtener todas las lecciones

const completedLessonsPromise = db.collection(`users/${userId}/lecciones`)
  .where("estado", "==", true) // Filtrar lecciones completadas
  .get(); 

Promise.all([totalLessonsPromise, completedLessonsPromise]) // Esperar ambas promesas
  .then(([totalLessonsSnapshot, completedLessonsSnapshot]) => {
    const totalCount = totalLessonsSnapshot.size; // Total de lecciones
    const completedCount = completedLessonsSnapshot.size; // Lecciones completadas
    
    // Mostrar el número de lecciones en el formato deseado
    lessonsCompleted.innerHTML = `<p><strong>Lecciones Completadas:</strong> ${completedCount}/${totalCount}</p>`;
  })
  .catch(error => {
    console.error("Error al obtener las lecciones:", error);
  });


// Mostrar las lecciones del alumno
const lessonsTableBody = document.querySelector("#lessonsTable tbody");
const lessonsTable = document.getElementById('lessonsTable');
const lessonsChartCanvas = document.getElementById('lessonsChart');
const durationChartCanvas = document.getElementById('durationChart'); // Canvas para el gráfico de duración
const pieChartCanvas= document.getElementById('pieChart');
const tableViewButton = document.getElementById('tableViewButton');
const chartViewButton = document.getElementById('chartViewButton');

// Mostrar la tabla al cargar
lessonsTable.style.display = 'table';
lessonsChartCanvas.style.display = 'none';
durationChartCanvas.style.display = 'none'; // Asegúrate de ocultar el gráfico de duración al inicio
pieChartCanvas.style.display = 'none';

// Función para mostrar la tabla
function showTable() {
    lessonsTable.style.display = 'table';
    lessonsChartCanvas.style.display = 'none';
    durationChartCanvas.style.display = 'none'; // Oculta el gráfico de duración
    pieChartCanvas.style.display = 'none';
}

// Función para mostrar ambos gráficos
function showChart() {
    lessonsTable.style.display = 'none';
    lessonsChartCanvas.style.display = 'block'; // Muestra el gráfico de lecciones
    durationChartCanvas.style.display = 'block'; // Muestra el gráfico de duración
    pieChartCanvas.style.display = 'block';
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

            const totalSeconds = Math.floor(lessonData.duration / 1000);//calculando segundos totales de la duracion
            const minutes = Math.floor(totalSeconds / 60);
            const seconds = totalSeconds % 60;

            
            // Convertir startTime y completionDate a formato de fecha y hora completo
            const startTime = new Date(lessonData.startTime).toLocaleString('en-GB', {
                year: 'numeric', month: '2-digit', day: '2-digit',
                hour: '2-digit', minute: '2-digit', second: '2-digit',
                hour12: false // Asegura formato de 24 horas
            });
            const completionDate = new Date(lessonData.completionDate).toLocaleString('en-GB', {
                year: 'numeric', month: '2-digit', day: '2-digit',
                hour: '2-digit', minute: '2-digit', second: '2-digit',
                hour12: false // Asegura formato de 24 horas
            });

            lessons.push(lessonData); // Agregar lección a la lista
            const lessonRow = document.createElement('tr');
            lessonRow.innerHTML = `
                <td>${lessonData.idLeccion}</td>
                <td>${lessonData.tipo}</td>
                <td>${lessonData.dificultad}</td>
                <td>${lessonData.tema}</td>
                <td>${lessonData.estado? 'Completado' : 'En Proceso'}</td>
                <td>${lessonData.puntaje}</td>
                <td>${startTime}</td>
                <td>${completionDate}</td>
                <td>${minutes}:${seconds.toString().padStart(2, '0')} minutos</td>
            `;
            lessonsTableBody.appendChild(lessonRow);
        });
    }
    // Renderizar el gráfico después de cargar las lecciones
    renderLessonsChart(lessons);
    renderDurationChart(lessons);  // Gráfico de duración
    renderPieChart(lessons);
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
// Función para renderizar el gráfico de duración
function renderDurationChart(lessons) {
    const labels = lessons.map(lesson => lesson.tema); // Extrae los temas de las lecciones
    const data = lessons.map(lesson => lesson.duration / 60000); // Convertir a minutos

    const ctx = durationChartCanvas.getContext('2d'); // Canvas para el gráfico de duración

    if (labels.length === 0 || data.length === 0) {
        console.error("No hay datos para renderizar el gráfico de duración");
        return; // Evitar renderizar si no hay datos
    }

    const durationChart = new Chart(ctx, {
        type: 'bar', // Tipo de gráfico (puedes cambiar a 'line' para un gráfico de líneas)
        data: {
            labels: labels,
            datasets: [{
                label: 'Duración de la Lección (minutos)',
                data: data,
                backgroundColor: 'rgba(153, 102, 255, 0.2)',
                borderColor: 'rgba(153, 102, 255, 1)',
                borderWidth: 1
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true,
                    title: {
                        display: true,
                        text: 'Duración (minutos)' // Título del eje Y
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: 'Tema' // Título del eje X
                    }
                }
            }
        }
    });
}

// Función para renderizar el gráfico de pastel
function renderPieChart(lessons) {
    // Contar lecciones completadas y en progreso
    const completedCount = lessons.filter(lesson => lesson.estado === true).length; // Contar lecciones completadas
    const inProgressCount = lessons.length - completedCount; // Contar lecciones en proceso

    const ctx = pieChartCanvas.getContext('2d'); // Canvas para el gráfico de pastel

    if (completedCount === 0 && inProgressCount === 0) {
        console.error("No hay datos para renderizar el gráfico de pastel");
        return; // Evitar renderizar si no hay datos
    }

    const pieChart = new Chart(ctx, {
        type: 'pie', // Tipo de gráfico
        data: {
            labels: ['Completadas', 'En Proceso'],
            datasets: [{
                data: [completedCount, inProgressCount],
                backgroundColor: [
                    'rgba(75, 192, 192, 0.6)', // Color para completadas
                    'rgba(153, 102, 255, 0.6)' // Color para en proceso
                ],
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: 'top',
                },
                title: {
                    display: true,
                    text: 'Estado de las Lecciones' // Título del gráfico
                }
            }
        }
    });
}
