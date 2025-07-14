///////////////////////////////////////////////////////
// Adopted from https://stackoverflow.com/a/62976646 //
///////////////////////////////////////////////////////

const zoomBackdropId = "zoom-backdrop";
const zoomedImageClass = "zoomed-image";
const zoomableImageClass = "zoomable-image";
let isImageResizing = false;

document
    .querySelector(".zoomable-image")
    .addEventListener("click", event => toggleElementZoom(event.target));

function toggleElementZoom(element, isResizeEvent) {
    if (!isResizeEvent && element.classList.contains(zoomedImageClass)) {
        element.classList.remove(zoomedImageClass);
        element.style.transform = "";
        document
            .querySelector(`#${zoomBackdropId}`)
            .classList.remove(zoomedImageClass);
        removeZoomOutListeners();
        removeResizeListener();
    } else {
        let imageCoordinates;
        if (isResizeEvent) {
            imageCoordinates = element._originalImageCordinates;
        } else {
            imageCoordinates = getBoundingClientRect(element);
            element._originalImageCordinates = imageCoordinates;
        }

        const deviceRatio = window.innerHeight / window.innerWidth;
        const imageRatio = imageCoordinates.height / imageCoordinates.width;

        // Scales the image according to the device and image size
        const imageScale = deviceRatio > imageRatio ?
            // Values are divided by 1.01 to prevent vertical scrollbar when fully zoomed in;
            // see the styles file for another solution which hides the scrollbar altogether.
            (window.innerWidth / imageCoordinates.width) / 1.01 :
            (window.innerHeight / imageCoordinates.height) / 1.01;

        const imageX = imageCoordinates.left + imageCoordinates.width / 2;
        const imageY = imageCoordinates.top + imageCoordinates.height / 2;
        const bodyX = window.innerWidth / 2;
        const bodyY = window.innerHeight / 2;

        const xOffset = (bodyX - imageX) / imageScale;
        const yOffset = (bodyY - imageY) / imageScale;

        element.style.transform = `scale(${imageScale}) translate(${xOffset}px, ${yOffset}px)`;
        element.classList.add(zoomedImageClass);
        document
            .querySelector(`#${zoomBackdropId}`)
            .classList.add(zoomedImageClass);
        registersZoomOutListeners();
        registerResizeListener();
    }
}

function registersZoomOutListeners() {
    // Zooms out on scroll
    document.addEventListener("scroll", scrollZoomOut);
    // Zooms out on pressing Escape
    document.addEventListener("keyup", escapeClickZoomOut);
    // Zooms out on clicking on the backdrop
    document
        .querySelector(`#${zoomBackdropId}`)
        .addEventListener("click", backDropClickZoomOut);
}

function removeZoomOutListeners() {
    document.removeEventListener("scroll", scrollZoomOut);
    document.removeEventListener("keyup", escapeClickZoomOut);
    document
        .querySelector(`#${zoomBackdropId}`)
        .removeEventListener("click", backDropClickZoomOut);
}

function registerResizeListener() {
    window.addEventListener("resize", onWindowResize);
}

function removeResizeListener() {
    window.removeEventListener("resize", onWindowResize);
}

function scrollZoomOut() {
    const element = document.querySelector(`.${zoomableImageClass}.${zoomedImageClass}`);
    if (element && !isImageResizing) toggleElementZoom(element);
}

function backDropClickZoomOut() {
    const element = document.querySelector(`.${zoomableImageClass}.${zoomedImageClass}`);
    if (element) toggleElementZoom(element);
}

function escapeClickZoomOut(event) {
    const element = document.querySelector(`.${zoomableImageClass}.${zoomedImageClass}`);
    if (element && event.key === "Escape") toggleElementZoom(element);
}

function onWindowResize() {
    isImageResizing = true;
    const element = document.querySelector(`.${zoomableImageClass}.${zoomedImageClass}`);
    if (element) {
        const func = function () {
            toggleElementZoom(element, true);
            isImageResizing = false;
        };
        debounce(func, 100)();
    }
}

function getBoundingClientRect(element) {
    const rect = element.getBoundingClientRect();
    return {
        top: rect.top,
        right: rect.right,
        bottom: rect.bottom,
        left: rect.left,
        width: rect.width,
        height: rect.height,
        x: rect.x,
        y: rect.y
    };
}

function debounce(func, delay) {
    let debounceTimer;
    return function () {
        const context = this;
        const args = arguments;
        clearTimeout(debounceTimer);
        debounceTimer = setTimeout(() => func.apply(context, args), delay);
    };
}
