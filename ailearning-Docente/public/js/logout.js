const logoutButton = document.getElementById('logoutButton');
const logoutModal = document.getElementById('logoutModal');
const confirmLogout = document.getElementById('confirmLogout');
const cancelLogout = document.getElementById('cancelLogout');

// Mostrar el modal al hacer clic en "Logout"
logoutButton.addEventListener('click', () => {
    logoutModal.style.display = 'flex'; // Mostrar el modal
});

// Cerrar sesión si se confirma
// Cerrar sesión si se confirma
confirmLogout.addEventListener('click', () => {
    firebase.auth().signOut()
        .then(() => {
            sessionStorage.setItem('logoutMessage', 'Has cerrado sesión con éxito'); // Guardar mensaje
            sessionStorage.setItem('messageType', 'success'); // Guardar tipo de mensaje
            window.location.href = 'logindocente.html'; // Redirigir al login
        })
        .catch((error) => {
            alert('Error al cerrar sesión: ' + error.message);
        });
});


// Ocultar el modal si se cancela
cancelLogout.addEventListener('click', () => {
    logoutModal.style.display = 'none'; // Ocultar el modal
});

// Cerrar el modal si se hace clic fuera del contenido
window.addEventListener('click', (e) => {
    if (e.target === logoutModal) {
        logoutModal.style.display = 'none';
    }
});
