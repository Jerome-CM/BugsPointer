window.addEventListener('DOMContentLoaded', function () {
    if (document.getElementById('page-new-user')) {
        let btnOpen = document.getElementById('btn-envoi');
        let btnClose = document.getElementById('btn-annulation');
        let popup = document.getElementById('popup');
        let domaine = document.getElementById('domaine');
        let domaineConfirme = document.getElementById('domaine-a-confirmer');
        let form = document.getElementById('form-confirmation');

        let enterPressed = false;

        function afficher() {
            /* Lorsque la fonction est appelée, elle ouvre la popup contenant le formulaire de bug */
            domaineConfirme.value = domaine.value;
            popup.style.display = "flex";
            enterPressed = true;
        }

        function masquer() {
            /* Lorsque la fonction est appelée, elle ferme la popup contenant le formulaire de bug */
            popup.style.display = "none";
            enterPressed = false;
        }

        if (domaine.value === "") {
            document.addEventListener("keydown", function (event) {
                /* Vérifie si la touche pressée est Entrée */
                if (event.key === "Enter") {
                    if (enterPressed) {
                        form.submit();
                    } else {
                        enterPressed = true;
                        event.preventDefault();
                        afficher();
                    }
                }
            })
            btnOpen.addEventListener("click", afficher);
        } else {
            document.addEventListener("keydown", function (event) {
                /* Vérifie si la touche pressée est Entrée */
                if (event.key === "Enter") {
                    event.preventDefault();
                }
            });
        }
        btnClose.addEventListener("click", masquer);

    }
    document.getElementById('nav-toggler').addEventListener('click', function (){
        document.getElementById('nav-menu').classList.toggle('show');
    });
});

