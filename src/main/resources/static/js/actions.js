window.addEventListener('DOMContentLoaded', function () {

    if (document.getElementById('page-new-user')) {
        let btnOpen = document.getElementById('btn-envoi');
        let btnClose = document.getElementById('btn-annulation');
        let popup = document.getElementById('popup');
        let domaine = document.getElementById('domaine');
        let domaineConfirme = document.getElementById('domaine-a-confirmer');
        let form = document.getElementById('form-confirmation');

        let enterPressed = false;

        function show() {
            /* Lorsque la fonction est appelée, elle ouvre la popup contenant le formulaire de bug */
            domaineConfirme.value = domaine.value;
            popup.style.display = "flex";
            enterPressed = true;
        }

        function hidden() {
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
                        show();
                    }
                }
            })
            btnOpen.addEventListener("click", show);
        } else {
            document.addEventListener("keydown", function (event) {
                /* Vérifie si la touche pressée est Entrée */
                if (event.key === "Enter") {
                    event.preventDefault();
                }
            });
        }
        if (btnClose != null) {
            btnClose.addEventListener("click", hidden);
        }

    }

    /* Nav Burger menu */

    if (document.getElementById('nav-toggler') != null) {
        document.getElementById('nav-toggler').addEventListener('click', function () {
            document.getElementById('nav-menu').classList.toggle('show');
        });
    }

    /* Sous menu documentation */
    if (document.getElementById('navDocumentation-toggler') != null) {
        const menuToggle = document.getElementById('navDocumentation-toggler');
        const menu = document.getElementById('menu');

        menuToggle.addEventListener('click', function (/*event*/) {
            /*event.preventDefault();*/
            menu.classList.toggle('show');

        });
        menu.querySelectorAll('a').forEach(function (link){
            link.addEventListener('click', function (){
                menu.classList.remove('show');
                /*menu.style.top = '0';*/
            });
        });
    }

    /* Sous menu bug */
    if (document.getElementById('navDashboard-toggler') != null && document.getElementById('content-nbr-report') != null) {
        const menuToggle = document.getElementById('navDashboard-toggler');
        const menu = document.getElementById('menu');
        const reportBox = document.getElementById('content-nbr-report');

            menuToggle.addEventListener('click', function (/*event*/) {
            /*event.preventDefault();*/
            menu.classList.toggle('show');
            reportBox.classList.toggle("viewLargeMenu");

        });
        menu.querySelectorAll('a').forEach(function (link){
            link.addEventListener('click', function (){
                menu.classList.remove('show');
                /*menu.style.top = '0';*/
            });
        });
    }

    /*/!* fix nav to the top *!/

    const fixedDiv = document.getElementById("fixedDiv");
    const offset = fixedDiv.offsetTop;

    window.addEventListener("scroll", function() {
        if (window.scrollY >= offset) {
            fixedDiv.classList.add("fixed");
        } else {
            fixedDiv.classList.remove("fixed");
        }
    });*/

    const cmd = document.getElementById("cmd");
    const ctrl = document.getElementById("ctrl");
    const encart = document.getElementById("trick-find");
    const divDetection = document.getElementById("div-detection-os");
    const divNoDetection = document.getElementById("no-detection-os");

    /* Récupère la plateforme (os) de l'utilisateur */
    function get_platform() {

        let os = null;
        // 2022 way of detecting. Note : this userAgentData feature is available only in secure contexts (HTTPS)
        if (typeof navigator.userAgentData !== 'undefined' && navigator.userAgentData != null) {
            os = navigator.userAgentData.platform;
            return os;
        }
        // Deprecated but still works for most of the browser
        if (typeof navigator.platform !== 'undefined') {
            if (typeof navigator.userAgent !== 'undefined' && /android/.test(navigator.userAgent.toLowerCase())) {
                // android device’s navigator.platform is often set as 'linux', so let’s use userAgent for them
                os = 'android';
                return os;
            }
            os = navigator.platform;
            return os;
        }
        os = 'unknown';
        return os;
    }

    let os = get_platform();

    if(os === "macOS"){
        ctrl.style.display="none";
    } else if(os === "Win32"){
        cmd.style.display="none";
    } else {
        encart.style.display="none";
    }

});

