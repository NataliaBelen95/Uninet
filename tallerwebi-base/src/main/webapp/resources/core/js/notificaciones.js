document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll('button.marcar-leida').forEach(button => {
        button.addEventListener('click', () => {
            const id = button.getAttribute('data-id');
            fetch('/marcar-leida/' + id, { method: 'POST' })
                .then(res => {
                    if (res.ok) {
                        const li = button.closest('li');
                        li.classList.remove('noleida');
                        li.classList.add('leida');
                    }
                });
        });
    });
});