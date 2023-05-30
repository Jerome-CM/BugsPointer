window.addEventListener('DOMContentLoaded', function () {
    if (document.getElementById('page-new-user')) {
        let btnOpen = document.getElementById('btn-envoi');
        let btnClose = document.getElementById('btn-annulation');
        let popup = document.getElementById('popup');
        let domaine = document.getElementById('domaine');
        let domaineConfirme = document.getElementById('domaine-a-confirmer');


        function afficher() {
            /* Lorsque la fonction est appelée, elle ouvre la popup contenant le formulaire de bug */
            domaineConfirme.value = domaine.value;
            popup.style.display = "flex";
        }

        function masquer() {
            /* Lorsque la fonction est appelée, elle ferme la popup contenant le formulaire de bug */
            popup.style.display = "none";
        }

        btnOpen.addEventListener("click", afficher);
        btnClose.addEventListener("click", masquer);

    }
});

function showAlert() {
    alert("The button was clicked!");
}