// Manejar el cierre de sesión
const logoutButton = document.getElementById('logoutButton');

logoutButton.addEventListener('click', () => {
    firebase.auth().signOut()
        .then(() => {
            alert('Has cerrado sesión con éxito');
            window.location.href = 'logindocente.html'; // Redirigir al login
        })
        .catch((error) => {
            alert('Error al cerrar sesión: ' + error.message);
        });
});
