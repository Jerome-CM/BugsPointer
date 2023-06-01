const key = "Vw6QSo7nPHkhXk7pD2z69HJ3A"; /*Ajoutez votre clé publique ici, entre les ""*/
window.addEventListener("DOMContentLoaded", function(){

/* Variables document */
let btnopen = document.querySelector('#bugspointer_open_popup');
let btnopenlogo = document.querySelector('#bugspointer_logo');
let btnclose = document.querySelector('#bugspointer_close_popup');
let btnpointer = document.querySelector('#bugspointer-popup-btn-pointer');
let form = document.querySelector('.bugspointer-popup-container');
let messageElement = document.getElementById("bugspointer-message-success");
let section = document.getElementById("bugspointer-affichage");
let url = document.getElementById("bugspointer-urlPointed");
let urlPointed = window.location.href;
let os = document.getElementById('bugspointer-popup-os');
let browser = document.getElementById('bugspointer-popup-browser');
let screen = document.getElementById('bugspointer-popup-screen');
let pointerSelected = document.getElementById('bugspointer-popup-pointer-bug');
let submit = document.getElementById('bugspointer-popup-btn-submit');//boutton initiallement désactivé
let textPointed = document.getElementById('bugspointer-popup-pointer-bug-text');
const publicKey = document.getElementById("key");

/* Autres variables */
const textValid = "Bug pointé correctement";
const platform = get_platform();
const browserWithVersion = get_browser();

/* Valeurs document modifées */
/* url */
url.value = urlPointed;

/* Free Modal */

function afficher(){
    /* Lorsque la fonction est appelée, elle ouvre la popup contenant le formulaire de bug */
    document.querySelector('#bugspointer_popup').style.display = "flex";
}

function masquer(){
    /* Lorsque la fonction est appelée, elle ferme la popup contenant le formulaire de bug */
    document.querySelector('#bugspointer_popup').style.display = "none";
}

function message(){
    /* Affiche le message lors du clic sur le bouton envoi (même si un problème lors de l'enregistrement est survenu)
    * et masque le formulaire, il ne reste que le bouton close */
    messageElement.innerHTML = "Merci pour votre envoi";
    section.style.display = "none";
}

/* User Agent Navigator */

function dimension(){
    /* Lors du 1er clic sur la modal */
    /* Récupère la largeur de la fenêtre */
    const screenWidth = window.innerWidth;
    /* Récupère la hauteur de la fenêtre */
    const screenHeight = window.innerHeight;
    /* Enregistrement de la valeur dans la ligne du formulaire correspondant */
    screen.value = screenWidth + ' x ' + screenHeight;
}

/* Récupère la plateforme (os) de l'utilisateur */
function get_platform() {

    // 2022 way of detecting. Note : this userAgentData feature is available only in secure contexts (HTTPS)
    if (typeof navigator.userAgentData !== 'undefined' && navigator.userAgentData != null) {
        return navigator.userAgentData.platform;
    }
    // Deprecated but still works for most of the browser
    if (typeof navigator.platform !== 'undefined') {
        if (typeof navigator.userAgent !== 'undefined' && /android/.test(navigator.userAgent.toLowerCase())) {
            // android device’s navigator.platform is often set as 'linux', so let’s use userAgent for them
            return 'android';
        }
        return navigator.platform;
    }
    return 'unknown';
}

/* Récupère le navigateur de l'utilisateur */
function get_browser() {
     // Nouvelle méthode
    if (typeof navigator.userAgentData !== 'undefined' && navigator.userAgentData != null) {
        /* brands récupère un tableau de 3 données, c'est la première qui nous intéresse ainsi que la version*/
        const firstBrand = navigator.userAgentData.brands[0];
        return firstBrand.brand + ' v' + firstBrand.version;
    }

    // Ancienne méthode pour la plupart des navigateurs
    if (typeof navigator.userAgent !== 'undefined') {
        const userAgent = navigator.userAgent.toLowerCase();
        // Chaque navigateur à son système de notation comme ci-joint
        const browsers = {
            edge: /edge\/([\d.]+)/.exec(userAgent),
            chrome: /chrome\/([\d.]+)/.exec(userAgent),
            safari: /version\/([\d.]+).*safari/.exec(userAgent),
            firefox: /firefox\/([\d.]+)/.exec(userAgent),
            ie: /msie ([\d.]+)/.exec(userAgent) || /trident\/.*rv:([\d.]+)/.exec(userAgent)
        };

        // On récupère le browser correspondant à l'userAgent
        for (const browser in browsers) {
            if (browsers[browser]) {
                return browser.charAt(0).toUpperCase() + browser.slice(1) + ' v' + browsers[browser][1];
            }
        }
    }
    // si le browser n'a pas pu être récupéré on retourne indéfini
    return 'undefined';
}

/* Point a bug */

function pointer(){
    /* Masque la popup pour aller sélectionner l'endroit où est le bug*/
    masquer();

    /* TODO: Est censé arrêter la fonction pointer si la popup n'a pas été masquée */
    if (document.querySelector('#bugspointer_popup').style.display !== "none") {
        console.log("display flex");
        return;
    } else {
        /* Variable lié au style initial */
        let styleInitial;

        /* Activation des écoutes d'évènement liée au passage de la souris */
        document.addEventListener('mouseover', handleMouseOver);
        document.addEventListener('mouseout', handleMouseOut);

        /* Lorsque la souris passe sur une balise */
        function handleMouseOver(event) {
            /* Permet de survoler toutes les balises sauf les balises body et html */
            let balise = event.target.closest("*:not(body):not(html)");
            if (balise != null) {
                /* Récupère le style de bordure de la balise survolée */
                styleInitial = getComputedStyle(balise).border;
                /* Applique une bordure rouge autour de la balise survolée */
                balise.style.border = '3px solid #FF0000';
                /* Activation de l'évènement de clic */
                balise.addEventListener('click', handleClick);
            }


        }
        /* Lorsque la souris ne survole plus une balise */
        function handleMouseOut(event) {
            /* Sur toutes les balises sauf les balises body et html */
            let balise = event.target.closest("*:not(body):not(html)");
            if (balise != null) {
                /* Réapplilque la bordure initiale */
                balise.style.border = styleInitial;
            }
        }
        /* Lors d'un clic sur une balise */
        function handleClick(event) {
            /* Annule les comportements par défault (lien, bouton...) */
            event.preventDefault();

            //TODO : Peut rester coincer dans handleClick et ne s'arrête pas

            /* Si la popup est active alors le clic n'est pas actif TODO:(ça fonctionne mais le mouseover peut être actif quand même) */
            if (document.querySelector('#bugspointer_popup').style.display != "none") {
                return;
            } else {
                /* L'évènement ne se déclenche qu'une fois */
                event.stopPropagation();
                /* récupère l'élément sélectionné (balise, class, text, value etc...) */
                let balise = event.target.closest("*:not(body):not(html)");
                if (balise != null) {
                    /* On réapplique le style initial */
                    balise.style.border = styleInitial;
                    /* La variable parentAffichage permettra d'afficher le dernier parent de balise
                    * et s'il n'y en a pas alors affichera la balise. */
                    let parentAffichage = balise;

                    /* Pour trouver les parents, on réalise une boucle à 2 itérations */
                    for (let i = 0; i < 2; i++) {
                        /* parent prend la valeur de parentAffichage (initialement balise) */
                        let parent = parentAffichage;

                        /* si la balise <> de parent correspond à l'une de celle-ci alors on arrête la boucle (initialement on récupère balise). */
                        if (parent.tagName == "SECTION" || parent.tagName == "ARTICLE" || parent.tagName == "ASIDE"
                                || parent.tagName == "FOOTER" || parent.tagName == "HEADER" || parent.tagName == "NAV" || parent.tagName == "FROM") {
                            break;
                        }

                        /* parent(i+1) prend la valeur du parent de parentAffichage */
                        parent = parentAffichage.parentElement;
                        /* S'il n'y a pas de parent supérieur ou si le parent correspond à BODY on arrête la boucle et on récupère le parent précédent. */
                        if (parent === null || parent.tagName == "BODY"){
                            break;
                        } else {
                            /* Sinon parentAffichage récupère le parent */
                            parentAffichage = parent;
                        }
                    }

                    let baliseType = balise.tagName.toLowerCase(); // On récupère le type de la balise
                    let baliseLu = balise.outerHTML; // On renvoie les données de balise en format text
                    let baliseModif; // permettra de récupérer le text modifié
                    if (baliseLu.includes('class="')){
                        /* Si dans la balise il existe une class, alors on va ajouter à celle-ci la class "bugspointer-text-balise */
                        baliseModif = baliseLu.replace('class="', 'class="bugspointer-text-balise ');
                    } else {
                        /* S'il n'y a pas de class alors on ajoute la class après le type de la balise */
                        baliseModif = baliseLu.replace('<' + baliseType, '<' + baliseType + ' class="bugspointer-text-balise"');
                    }
                    /* On récupère le texte dernier parent trouvé dans la boucle (ou à défaut la balise). */
                    let parentLu = parentAffichage.outerHTML;
                    /* On remplace la balise récupérée par sa modification ci-dessus */
                    let parentModif = parentLu.replace(baliseLu, baliseModif);
                    /* On retire tous les éléments de style (car mouseover et mouseout ajoute des styles qui pourraient perturber les clients et ça allège le texte */
                    let parentWithoutStyle = parentModif.replace(/ style="[^"]*"/g, "");
                    console.log(parentWithoutStyle);
                    /* On envoie le parent ainsi modifié à la valeur du pointer */
                    pointerSelected.innerHTML = parentWithoutStyle;

                    /* On annule tous les écoutes sur évènement lié à pointer */
                    document.removeEventListener('mouseover', handleMouseOver);
                    document.removeEventListener('mouseout', handleMouseOut);
                    btnpointer.removeEventListener("click", pointer);
                    balise.removeEventListener('click', handleClick);
                    btnpointer.removeEventListener("click", pointer);

                    /* On change le texte du bouton pointer par textvalid ainsi que sa couleur */
                    btnpointer.innerHTML = textValid;
                    btnpointer.style.backgroundColor = "#00E676"
                    /* On rend invisible le texte d'indication, car il a été suivi */
                    textPointed.style.display = "none";
                    /* On active le bouton d'envoi du formulaire */
                    submit.disabled = false;

                    /* On réaffiche la popup pour continuer le formulaire et l'envoyer */
                    afficher();

                    /* On arrête la fonction TODO:(ne fonctionne pas)*/
                    return true;
                }
            }
            document.removeEventListener('click', handleClick);
            return true;
        }
    }
}

/* Events Listener */
    /* On vérifie que les boutons sont bien présents sur la page pour lancer les events d'affichage*/
    if (btnopen != null) {
        btnopen.addEventListener("click", afficher); // le clic sur le lien ouvre la popup
        /* Permet de déclencher la lecture des dimensions de l'écran au moment du clic pour l'ouverture de la popup */
        btnopen.addEventListener("click", dimension);
    }
    if (btnopenlogo != null) {
        btnopenlogo.addEventListener("click", afficher); // le clic sur le logo ouvre la popup
        /* Permet de déclencher la lecture des dimensions de l'écran au moment du clic pour l'ouverture de la popup */
        btnopenlogo.addEventListener("click", dimension);
    }
/* Le clic sur la croix ferme la popup et permet de rafraichir la page pour recharger un nouveau formulaire après envoi */
btnclose.addEventListener("mousedown", () => {
    masquer();
    if (messageElement.innerHTML != null){ // TODO: Supprimer le if pour n'appliquer que le reload ?
        location.reload();
    }
});
btnpointer.addEventListener("click", pointer);  // Le clic sur pointer un bug actif la fonction pointer
/* Envoi du formulaire */
form.addEventListener("submit", function (e) {
    // Met en pause le comportement par défault qu'est l'envoi
    e.preventDefault();
    /* Ajout des valeurs qui ne devront pas être changée */
    /* key */
    publicKey.value = key;
    /* user agent */
    os.value = platform;
    browser.value = browserWithVersion;
    // Récupération des données du formulaire
    let formData = new FormData(this);

    // Envoi les données du formulaire à l'adresse correspondante
    fetch(this.action, {
        method: this.method,
        body: formData
    }).then(function (response){
        if (response.ok) {
            message(); // Si l'envoi se fait alors le message apparait, sinon on reste sur le formulaire
        }
    }).catch(function (error) {
        console.error("Une erreur s'est produite lors de l'envoi du formulaire", error)
    })
});


});