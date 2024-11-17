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
    durationChartCanvas.style.display = 'none';
    pieChartCanvas.style.display = 'none';
    document.getElementById("btnLessonsChart").style.display = 'none';
    document.getElementById("btnDurationChart").style.display = 'none';
    document.getElementById("btnPieChart").style.display = 'none';

    // Oculta el selector de fecha
    document.getElementById("week-picker").style.display = 'none';
    document.querySelector("label[for='week-picker']").style.display = 'none';
    document.querySelector("label[for='chart-selector']").style.display = 'none';
    document.getElementById("filter-button").style.display = 'none';
    document.getElementById("chart-selector").style.display = 'none';

}



// Función para mostrar el gráfico correspondiente
function showChart(chartType) {
    // Ocultar todos los gráficos
    document.getElementById("chart-selector").style.display = 'block';


    // Mostrar la vista de gráficos (y ocultar la tabla si está visible)
    document.getElementById("lessonsTable").style.display = 'none';

    // Muestra el selector de fecha
    document.getElementById("week-picker").style.display = 'block';
    document.querySelector("label[for='week-picker']").style.display = 'block';
    document.querySelector("label[for='chart-selector']").style.display = 'block';
    document.getElementById("filter-button").style.display = 'block';


    document.getElementById("chart-selector").addEventListener("change", function() {
        // Obtiene el valor seleccionado
        const selectedChart = this.value;
    
        // Oculta todos los gráficos
        document.getElementById("lessonsChart").style.display = 'none';
        document.getElementById("durationChart").style.display = 'none';
        document.getElementById("pieChart").style.display = 'none';
    
        // Muestra el gráfico correspondiente
        if (selectedChart === "lessonsChart") {
            document.getElementById("lessonsChart").style.display = 'block';
        } else if (selectedChart === "durationChart") {
            document.getElementById("durationChart").style.display = 'block';
        } else if (selectedChart === "pieChart") {
            document.getElementById("pieChart").style.display = 'block';
        }
    });
    
    // Mostrar lessonsChart por defecto al cargar la página
    document.getElementById("lessonsChart").style.display = 'block';
    

}



// Agregar eventos a los botones
tableViewButton.addEventListener('click', showTable);
chartViewButton.addEventListener('click', showChart);



const lessons = [];

// Mostrar las lecciones del alumno y renderizar el gráfico
db.collection(`users/${userId}/lecciones`).get().then(querySnapshot => {
    if (querySnapshot.empty) {
        lessonsTableBody.innerHTML = `<tr><td colspan="7">No hay lecciones disponibles.</td></tr>`;
    } else {
        querySnapshot.forEach(doc => {
            const lessonData = doc.data();

            const totalSeconds = Math.floor(lessonData.duration / 1000);
            const minutes = Math.floor(totalSeconds / 60);
            const seconds = totalSeconds % 60;

            const startTime = new Date(lessonData.startTime).toLocaleDateString('en-GB', {
                year: 'numeric', month: '2-digit', day: '2-digit'
            }) + ' en ' + new Date(lessonData.startTime).toLocaleTimeString('en-GB', {
                hour: '2-digit', minute: '2-digit', second: '2-digit',
                hour12: false
            });
            const completionDate = new Date(lessonData.completionDate).toLocaleString('en-GB', {
                year: 'numeric', month: '2-digit', day: '2-digit'
            }) + ' en ' + new Date(lessonData.completionDate).toLocaleTimeString('en-GB', {
                hour: '2-digit', minute: '2-digit', second: '2-digit',
                hour12: false
            });

            lessons.push(lessonData);

            const lessonRow = document.createElement('tr');
            const uniqueId = lessonData.idLeccion;

            // Validar si el JSON tiene el formato adecuado
            let contenidoHTML = '';

            if (lessonData.tipo === 'Pronunciación Perfecta') {
                try {
                    const frasesArray = JSON.parse(lessonData.json);
                    if (Array.isArray(frasesArray)) {
                        contenidoHTML += `<h4>Frases:</h4>`;
                        contenidoHTML += `<ul style="list-style-type: circle; padding-left: 20px;">`;
                        frasesArray.forEach(fraseObj => {
                            if (fraseObj.ID && fraseObj.frase) {
                                contenidoHTML += `<li>${fraseObj.ID}. ${fraseObj.frase}</li>`;
                            }
                        });
                        contenidoHTML += `</ul>`;
                    }
                } catch (error) {
                    console.error('Formato de JSON inválido para Pronunciación Perfecta:', error);
                    contenidoHTML = '<p style="color:red;">JSON inválido</p>';
                }
            } else if (lessonData.tipo === 'Desafío de Comprensión' || lessonData.tipo === 'Escucha Activa') {
                try {
                    const desafioArray = JSON.parse(lessonData.json);
                    if (Array.isArray(desafioArray)) {
                        desafioArray.forEach(desafioObj => {
                            contenidoHTML += `<h4>Lectura:</h4><p>${desafioObj.reading};</p></br><h4>Preguntas:</h4>`;
                            desafioObj.quiz.forEach((quizItem, index) => {
                                contenidoHTML += `<p>${index + 1}. ${quizItem.question}</p>`;
                                // Usamos un estilo para que todos los ítems sean círculos
                                contenidoHTML += `<ul style="list-style-type: circle; padding-left: 20px;">`;
                                quizItem.options.forEach(option => {
                                    contenidoHTML += `<li>${option}</li>`;
                                });
                                contenidoHTML += `</ul>`;
                                contenidoHTML += `<p><strong>Respuesta Correcta:</strong> ${quizItem.correct_answer}</p>`;
                                contenidoHTML += `</br>`;
                            });
                        });
                    }
                } catch (error) {
                    console.error('Formato de JSON inválido para Desafío de Comprensión o Escucha Activa:', error);
                    contenidoHTML = '<p style="color:red;">JSON inválido</p>';
                }
            } else if (lessonData.tipo === 'Frases en Acción') {
                // Nueva sección para el tipo "Frases en Acción"
                try {
                    const frasesEnAccionArray = JSON.parse(lessonData.json);
                    if (Array.isArray(frasesEnAccionArray)) {
                        frasesEnAccionArray.forEach(fraseAccionObj => {
                            contenidoHTML += `<h4>Preguntas:</h4>`;
                            fraseAccionObj.quiz.forEach((quizItem, index) => {
                                contenidoHTML += `<p>${index + 1}. ${quizItem.frase}</p>`;
                                // Usamos un estilo para que todos los ítems sean círculos
                                contenidoHTML += `<ul style="list-style-type: circle; padding-left: 20px;">`;
                                quizItem.options.forEach((option, optIndex) => {
                                    contenidoHTML += `<li>${String.fromCharCode(65 + optIndex)}. ${option}</li>`;
                                });
                                contenidoHTML += `</ul>`;
                                contenidoHTML += `<p><strong>Respuesta Correcta:</strong> ${quizItem.correct_answer}</p>`;
                                contenidoHTML += `</br>`;
                            });
                        });
                    }
                } catch (error) {
                    console.error('Formato de JSON inválido para Frases en Acción:', error);
                    contenidoHTML = '<p style="color:red;">JSON inválido</p>';
                }
            }else if (lessonData.tipo === 'Desafío de Cartas') {
                // Nueva sección para el tipo "Desafío de Cartas"
                try {
                    const desafioCartasArray = JSON.parse(lessonData.json);
                    if (Array.isArray(desafioCartasArray)) {
                        desafioCartasArray.forEach(cartaObj => {
                            contenidoHTML += `<h4>Preguntas:</h4>`;
                            cartaObj.quiz.forEach((quizItem, index) => {
                                contenidoHTML += `<p>${index + 1}. ${quizItem.question}</p>`;
                                // Usamos un estilo para que todos los ítems sean círculos
                                contenidoHTML += `<ul style="list-style-type: circle; padding-left: 20px;">`;
                                quizItem.options.forEach((option, optIndex) => {
                                    contenidoHTML += `<li>${String.fromCharCode(65 + optIndex)}. ${option}</li>`;
                                });
                                contenidoHTML += `</ul>`;
                                contenidoHTML += `<p><strong>Respuesta Correcta:</strong> ${quizItem.correct_answer}</p>`;
                                contenidoHTML += `</br>`;
                            });
                        });
                    }
                } catch (error) {
                    console.error('Formato de JSON inválido para Desafío de Cartas:', error);
                    contenidoHTML = '<p style="color:red;">JSON inválido</p>';
                }
            }else if (lessonData.tipo === 'Adivina la Palabra') {
                // Nueva sección para el tipo "Adivina la Palabra"
                try {
                    const adivinaArray = JSON.parse(lessonData.json);
                    if (Array.isArray(adivinaArray)) {
                        contenidoHTML += `<h4>Palabras a Adivinar:</h4>`;
                        adivinaArray.forEach((adivinaObj, index) => {
                            contenidoHTML += `<p>${index + 1}.</strong> ${adivinaObj.clue}</p>`;
                            contenidoHTML += `<ul style="list-style-type: circle; padding-left: 20px;">`;
                            contenidoHTML += `<li><strong>Respuesta Correcta:</strong> ${adivinaObj.oneword}</li>`;
                            contenidoHTML += `</br>`;
                            contenidoHTML += `</ul>`;
                        });
                    }
                } catch (error) {
                    console.error('Formato de JSON inválido para Adivina la Palabra:', error);
                    contenidoHTML = '<p style="color:red;">JSON inválido</p>';
                }
            }

                        


            lessonRow.innerHTML = `
                <td>${lessonData.idLeccion}</td>
                <td>${lessonData.tipo}</td>
                <td>${lessonData.dificultad}</td>
                <td>${lessonData.tema}</td>
                <td>${lessonData.estado ? 'Completado' : 'En Proceso'}</td>
                <td>${lessonData.puntaje}</td>
                <td>${startTime}</td>
                <td>${completionDate}</td>
                <td>${minutes}:${seconds.toString().padStart(2, '0')} min</td>
                <td>
                    <button id="btnAbrirModal-${uniqueId}" class="ver-leccion">
                        <i class="far fa-eye"></i>
                    </button>
                    <dialog id="miPopUp-${uniqueId}" class="popup-dialog">
                        <h2>ID: ${lessonData.idLeccion}</h2>

                        <div class="container-ver-leccion">
                            <div class="columna-izquierda">
                                <table class="tabla-datos">
                                    <tr><td><strong>Tipo:</strong></td><td>${lessonData.tipo}</td></tr>
                                    <tr><td><strong>Dificultad:</strong></td><td>${lessonData.dificultad}</td></tr>
                                    <tr><td><strong>Tema:</strong></td><td>${lessonData.tema}</td></tr>
                                    <tr><td><strong>Estado:</strong></td><td>${lessonData.estado ? 'Completado' : 'En Proceso'}</td></tr>
                                </table>
                            </div>
                            <div class="columna-derecha">
                                <table class="tabla-datos">
                                    <tr><td><strong>Inicio:</strong></td><td>${startTime}</td></tr>
                                    <tr><td><strong>Fin:</strong></td><td>${completionDate}</td></tr>
                                    <tr><td><strong>Duración:</strong></td><td>${minutes}:${seconds.toString().padStart(2, '0')} min</td></tr>
                                    <tr><td><strong>Puntaje:</strong></td><td>${lessonData.puntaje}</td></tr>
                                </table>
                            </div>
                        </div>

                        <h3>Contenido</h3>
                        <ul>${contenidoHTML || '<li>No hay contenido disponible</li>'}</ul>

                        <button id="btnCerrarModal-${uniqueId}">Cerrar</button>
                    </dialog>
                </td>
            `;
            lessonsTableBody.appendChild(lessonRow);

            // Añadir event listeners dinámicamente
            const btnAbrirModal = document.getElementById(`btnAbrirModal-${uniqueId}`);
            const btnCerrarModal = document.getElementById(`btnCerrarModal-${uniqueId}`);
            const miPopUp = document.getElementById(`miPopUp-${uniqueId}`);

            if (btnAbrirModal && btnCerrarModal && miPopUp) {
                btnAbrirModal.addEventListener('click', () => {
                    miPopUp.showModal();
                });
                btnCerrarModal.addEventListener('click', () => {
                    miPopUp.close();
                });
            }
        });
    }
    renderLessonsChart(lessons);
    renderDurationChart(lessons);
    renderPieChart(lessons);
}).catch(error => {
    console.error("Error al obtener las lecciones:", error);
});







// Función para mostrar la alerta
function showAlert(message) {
    const alertContainer = document.getElementById('alert-container');
    alertContainer.textContent = message;
    alertContainer.style.display = 'block';
}

// Función para ocultar la alerta
function hideAlert() {
    const alertContainer = document.getElementById('alert-container');
    alertContainer.style.display = 'none';
}

// Función para obtener el rango de fechas de la semana de una fecha seleccionada
function getWeekRange(date) {
    const startOfWeek = new Date(date);
    const endOfWeek = new Date(date);

    // Ajustar al inicio de la semana (lunes)
    startOfWeek.setDate(startOfWeek.getDate() - startOfWeek.getDay() + 1);
    startOfWeek.setHours(0, 0, 0, 0);

    // Ajustar al final de la semana (domingo)
    endOfWeek.setDate(endOfWeek.getDate() - endOfWeek.getDay() + 7);
    endOfWeek.setHours(23, 59, 59, 999);

    return { start: startOfWeek, end: endOfWeek };
}

// Función para filtrar lecciones por semana
function filtrarsemanda() {
    const selectedDate = document.getElementById('week-picker').value;
    if (!selectedDate) {
        alert("Por favor, selecciona una fecha.");
        return;
    }

    const selectedDateObject = new Date(selectedDate);
    const { start, end } = getWeekRange(selectedDateObject);

    // Filtrar las lecciones por la semana seleccionada
    const filteredLessons = lessons.filter(lesson => {
        const lessonDate = new Date(lesson.startTime);
        return lessonDate >= start && lessonDate <= end;
    });

    // Mostrar/ocultar alerta dependiendo del resultado del filtro
    if (filteredLessons.length === 0) {
        showAlert("No hay datos disponibles para la semana seleccionada.");
    } else {
        hideAlert();
        // Renderizar los gráficos con las lecciones filtradas
        renderLessonsChart(filteredLessons);
        renderDurationChart(filteredLessons);
        renderPieChart(filteredLessons);
        showChart();
    }
}

// Añadir evento al botón para activar el filtrado
document.getElementById('filter-button').addEventListener('click', filtrarsemanda);








// Función para renderizar el gráfico
function renderLessonsChart(lessons) {
    const labels = lessons.map(lesson => lesson.tema);
    const data = lessons.map(lesson => lesson.puntaje);
    const fehca = lessons.filter(lesson => lesson.startTime);

    console.log(lessons);

    const ctx = lessonsChartCanvas.getContext('2d');

    // Verificar si ya existe un gráfico y destruirlo
    if (window.lessonsChartInstance) {
        window.lessonsChartInstance.destroy();
    }

    if (labels.length === 0 || data.length === 0) {
        console.error("No hay datos para renderizar el gráfico");
        return;
    }

    window.lessonsChartInstance = new Chart(ctx, {
        type: 'bar',
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

function formatDuration(milliseconds) {
    const totalSeconds = Math.floor(milliseconds / 1000);
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;
    return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
}

function renderDurationChart(lessons) {
    const labels = lessons.map(lesson => lesson.tema);
    const data = lessons.map(lesson => lesson.duration); // Mantener en milisegundos

    const ctx = durationChartCanvas.getContext('2d');

    // Verificar si ya existe un gráfico y destruirlo
    if (window.durationChartInstance) {
        window.durationChartInstance.destroy();
    }

    if (labels.length === 0 || data.length === 0) {
        console.error("No hay datos para renderizar el gráfico de duración");
        return;
    }

    window.durationChartInstance = new Chart(ctx, {
        type: 'line', // Gráfico de líneas
        data: {
            labels: labels,
            datasets: [{
                label: 'Duración de la Lección',
                data: data, // Mantener los milisegundos como están
                backgroundColor: 'rgba(153, 102, 255, 0.2)',
                borderColor: 'rgba(153, 102, 255, 1)',
                borderWidth: 2,
                tension: 0.4, // Suavizar las líneas
                fill: true // Rellenar debajo de la línea
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return formatDuration(value); // Formatear en MM:SS
                        }
                    },
                    title: {
                        display: true,
                        text: 'Duración en Minutos'
                    }
                },
                x: {
                    title: {
                        display: true,
                        text: 'Tema'
                    }
                }
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            return `Duración: ${formatDuration(context.raw)}`;
                        }
                    }
                }
            }
        }
    });
}




// Función para renderizar el gráfico de pastel
function renderPieChart(lessons) {
    const completedCount = lessons.filter(lesson => lesson.estado === true).length;
    const inProgressCount = lessons.length - completedCount;
    const fehca = lessons.filter(lesson => lesson.startTime);

    const ctx = pieChartCanvas.getContext('2d');

    // Verificar si ya existe un gráfico y destruirlo
    if (window.pieChartInstance) {
        window.pieChartInstance.destroy();
    }

    if (completedCount === 0 && inProgressCount === 0) {
        console.error("No hay datos para renderizar el gráfico de pastel");
        return;
    }

    window.pieChartInstance = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: ['Completadas', 'En Proceso'],
            datasets: [{
                data: [completedCount, inProgressCount],
                backgroundColor: [
                    'rgba(75, 192, 192, 0.6)', 
                    'rgba(153, 102, 255, 0.6)'
                ],
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'top',
                    labels: {
                        font: {
                            size: 14
                        }
                    }
                },
                title: {
                    display: true,
                    text: 'Estado de las Lecciones',
                    font: {
                        size: 18
                    }
                }
            },
            aspectRatio: 1,
        }
    });

    pieChartCanvas.style.width = '150px';
    pieChartCanvas.style.height = '150px';
}
