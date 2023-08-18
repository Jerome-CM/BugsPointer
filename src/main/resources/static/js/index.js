window.addEventListener('DOMContentLoaded', function () {

    function displayBlockToScroll(id, pixToScroll) {
        // Affiche la guideLine sur l'index
        const domElement = document.getElementById(id);

        const pixelsToScroll = pixToScroll; // Le nombre de pixels à défiler avant d'afficher l'image

        domElement.style.display = "none";

        setInterval(function() {
                                              // .index == body
            if (document.querySelector('.index').scrollTop > pixelsToScroll) {
                domElement.style.display = "block";
            }

        },100);
    }

    displayBlockToScroll("guidelineMotionDiv", 300);

    // Affiche le slider de l'index
    const images = document.querySelectorAll('.img-slider-index');
    const listItems = document.querySelectorAll('.li-what-signal');

    let currentIndex = 0;

    function showImage(index) {
        images.forEach((img, i) => {
            img.style.display = i === index ? 'block' : 'none';
            img.style.opacity = i === index ? '1' : '0';
        });

        listItems.forEach((li, i) => {
            if (i === index) {
                li.classList.add('active-li');
            } else {
                li.classList.remove('active-li');
            }
        });
    }

    function nextImage() {
        currentIndex = (currentIndex + 1) % images.length;
        showImage(currentIndex);
    }

    showImage(currentIndex);
    setInterval(nextImage, 3000);


    displayBlockToScroll("numbers-bar", 2300);
    const bugCounter = document.getElementById('counterDisplay');
    const startValue = 0;
    const targetValue = 277;
    bugCounter.textContent = startValue;
    let currentValue = targetValue - 50;

    setInterval(function() {

        if (document.getElementById("numbers-bar").style.display === "block") {
            function updateCounter() {
                if (currentValue < targetValue) {
                    /*setTimeout(function() {*/
                        currentValue++;
                        bugCounter.textContent = currentValue;
                        /*updateCounter();*/ // Appel récursif pour la prochaine itération
                    /*}, 2000);*/
                } else {
                    bugCounter.textContent = currentValue;
                }
            }
            updateCounter();
        }
    },100);
});