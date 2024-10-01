document.addEventListener('DOMContentLoaded', () => {
    // Verificar si es la primera vez que el usuario visita la web
    const isFirstVisit = localStorage.getItem('isFirstVisit');

    if (!isFirstVisit) {
        // Si es la primera visita, redirigir a login.html
        window.location.href = 'login.html';
        // Guardar que el usuario ha visitado la página
        localStorage.setItem('isFirstVisit', 'true');
    }
});