let btnopen = document.querySelector('#bugspointer_open_popup');
let btnopenlogo = document.querySelector('#bugspointer_logo');
let btnclose = document.querySelector('#bugspointer_close_popup');
var urlPointed = window.location.href;

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