const key = "Vw6QSo7nPHkhXk7pD2z69HJ3A"; /*Ajoutez votre clé public ici, entre les ""*/


/* Variables document */
let btnopen = document.querySelector('#bugspointer_open_popup');
let btnopenlogo = document.querySelector('#bugspointer_logo');
let btnclose = document.querySelector('#bugspointer_close_popup');
let btnpointer = document.querySelector('#bugspointer-popup-btn-pointer');
let form = document.querySelector('.bugspointer-popup-container');
let messageElement = document.getElementById("bugspointer-message-success");
let section = document.getElementById("bugspointer-affichage");
let urlPointed = window.location.href;
let os = document.getElementById('bugspointer-popup-os');
let browser = document.getElementById('bugspointer-popup-browser');
let screen = document.getElementById('bugspointer-popup-screen');
let pointerSelected = document.getElementById('bugspointer-popup-pointer-bug');
const balisePointer = document.querySelectorAll('*:not(body):not(html)');
const backgroundPointer = "rgba(128,197,234,0.5)";
const publicKey = document.getElementById("key");

publicKey.value = key;

const platform = get_platform();
os.value = platform;
const browserWithVersion = get_browser();
browser.value = browserWithVersion;

/*dimension();*/

document.getElementById("urlPointed").value = urlPointed;

/* Free Modal */

function afficher(){
    document.querySelector('#bugspointer_popup').style.display = "flex";
}

function masquer(){
    document.querySelector('#bugspointer_popup').style.display = "none";
}

function message(){
    messageElement.innerHTML = "Merci pour votre envoi";
    section.style.display = "none";
}

function dimension(){
    const screenWidth = window.innerWidth;
    const screenHeight = window.innerHeight;
    screen.value = screenWidth + ' x ' + screenHeight;
}

/* User Agent Navigator */

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

function get_browser() {
    if (typeof navigator.userAgentData !== 'undefined' && navigator.userAgentData != null) {
        const firstBrand = navigator.userAgentData.brands[0];
        return firstBrand.brand + ' v' + firstBrand.version;
    }

    if (typeof navigator.userAgent !== 'undefined') {
        const userAgent = navigator.userAgent.toLowerCase();
        const browsers = {
            edge: /edge\/([\d.]+)/.exec(userAgent),
            chrome: /chrome\/([\d.]+)/.exec(userAgent),
            safari: /version\/([\d.]+).*safari/.exec(userAgent),
            firefox: /firefox\/([\d.]+)/.exec(userAgent),
            ie: /msie ([\d.]+)/.exec(userAgent) || /trident\/.*rv:([\d.]+)/.exec(userAgent)
        };

        for (const browser in browsers) {
            if (browsers[browser]) {
                return browser.charAt(0).toUpperCase() + browser.slice(1) + ' v' + browsers[browser][1];
            }
        }
    }

    return 'undefined';
}

/* Point a bug */

let pointerActive = false;
let iteration = 0;

function pointer(){
    /*pointerActive = true;

    masquer();

    for(let i = 0; i < balisePointer.length; i++){
        let balise = balisePointer[i];
        let originalBackgroundColor = getComputedStyle(balise).backgroundColor;
        balise.addEventListener('mouseover', handleMouseOver);
        balise.addEventListener('mouseout', handleMouseOut);
        if (document.querySelector('#bugspointer_popup').style.display === "flex") {
                    break;
                }

        function handleMouseOver(){
            if(pointerActive) {
                modifBackground(balise, backgroundPointer);
                balise.addEventListener('click', () => {
                    pointerSelected.value = handleClick();
                });
            }
        }

        function handleMouseOut() {
            if (pointerActive) {
                modifBackground(balise, originalBackgroundColor);
            }
        }

        function handleClick() {
            pointerActive = false;
            modifBackground(balise, originalBackgroundColor);

            pointerSelected.value = balise.textContent;
            afficher();
            iteration++;
            console.log(iteration);
            return balise.textContent;
        }



    }

    if (!pointerActive){
        return;
    }*/

    masquer();

    let styleInitial;
    let pointerActive;

    document.addEventListener('mouseover', handleMouseOver);
    document.addEventListener('mouseout', handleMouseOut);

    function handleMouseOver(event) {
        let balise = event.target.closest("*:not(body):not(html)");
        if (balise != null) {
            styleInitial = getComputedStyle(balise).border;
            balise.style.border = '3px solid #FF0000';
            balise.addEventListener('click', handleClick);
        }
        /*console.log("Balise survolée : ", balise)*/


    }
    function handleMouseOut(event) {
        let balise = event.target.closest("*:not(body):not(html)");
        if (balise != null) {
            balise.style.border = styleInitial;
        }
    }
    function handleClick(event){
        /*event.preventDefault();*/
        event.stopPropagation();
        let balise = event.target.closest("*:not(body):not(html)");
        if (balise != null) {
            pointerSelected.value = balise.textContent;
            console.log(balise);
            document.removeEventListener('mouseover', handleMouseOver);
            document.removeEventListener('mouseout', handleMouseOut);
            balise.removeEventListener('click', handleClick);
            /*btnpointer.removeEventListener("click", pointer);*/
            balise.style.border = styleInitial;
            afficher();
        }

    }
    iteration++;
    console.log(iteration);

}



function modifBackground(balise, backgroundColor){
    balise.style.backgroundColor = backgroundColor;
}

/* Events Listener */

btnopen.addEventListener("click", afficher);
btnopenlogo.addEventListener("click", afficher);
btnclose.addEventListener("mousedown", masquer);
btnpointer.addEventListener("click", pointer);
form.addEventListener("submit", function (e) {
    e.preventDefault();
    let formData = new FormData(this);

    fetch(this.action, {
        method: this.method,
        body: formData
    }).then(function (response){
        if (response.ok) {
            message();
        }
    }).catch(function (error) {
        console.error("Une erreur s'est produite lors de l'envoi du formulaire", error)
    })
});
btnopen.addEventListener("click", dimension);
btnopenlogo.addEventListener("click", dimension);