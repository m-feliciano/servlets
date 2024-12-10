// IIFE: Immediately Invoked Function Expression
(function () {
    document.addEventListener('DOMContentLoaded', function () {
        const backButtons = document.querySelector('#backButton');
        if (!backButtons) return;

        const updateBackButtonState = () => {
            if (window.history.length <= 1) {
                backButtons.setAttribute('disabled', 'disabled');
            } else {
                backButtons.removeAttribute('disabled');
            }
        }

        updateBackButtonState();
    })
})();