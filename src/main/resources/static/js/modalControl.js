const key = "Vw6QSo7nPHkhXk7pD2z69HJ3A"; /*Ajoutez votre clé public ici, entre les ""*/

let btnopen = document.querySelector('#bugspointer_open_popup');
let btnopenlogo = document.querySelector('#bugspointer_logo');
let btnclose = document.querySelector('#bugspointer_close_popup');

const publicKey=document.getElementById("key")
publicKey.value = key;

var urlPointed = window.location.href;

/* Free Modal */

function afficher(){
    document.querySelector('#bugspointer_popup').style.display = "flex";
}

function masquer(){
    document.querySelector('#bugspointer_popup').style.display = "none";
}

btnopen.addEventListener("click", afficher);
btnopenlogo.addEventListener("click", afficher);
btnclose.addEventListener("click", masquer);

document.getElementById("urlPointed").value=urlPointed;

