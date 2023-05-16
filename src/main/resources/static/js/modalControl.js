const key = "Vw6QSo7nPHkhXk7pD2z69HJ3A"; /*Ajoutez votre clé public ici, entre les ""*/

let btnopen = document.querySelector('#bugspointer_open_popup');
let btnopenlogo = document.querySelector('#bugspointer_logo');
let btnclose = document.querySelector('#bugspointer_close_popup');
let btnenvoi = document.querySelector('#bugspointer-popup-btn-submit');
let form = document.querySelector('.bugspointer-popup-container');
let messageElement = document.getElementById("bugspointer-message-success");
let section = document.getElementById("bugspointer-affichage");

const publicKey=document.getElementById("key");
publicKey.value = key;

var urlPointed = window.location.href;

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

btnopen.addEventListener("click", afficher);
btnopenlogo.addEventListener("click", afficher);
btnclose.addEventListener("click", masquer);
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

document.getElementById("urlPointed").value=urlPointed;

