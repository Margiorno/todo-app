const startSlider      = document.getElementById('startRange');
const endSlider        = document.getElementById('endRange');
const startTimeInput   = document.getElementById('startTimeInput');
const endTimeInput     = document.getElementById('endTimeInput');
const startMinusBtn    = document.getElementById('startMinusBtn');
const startPlusBtn     = document.getElementById('startPlusBtn');
const endMinusBtn      = document.getElementById('endMinusBtn');
const endPlusBtn       = document.getElementById('endPlusBtn');

function formatTime(minutes) {
    const h = Math.floor(minutes / 60).toString().padStart(2, '0');
    const m = (minutes % 60).toString().padStart(2, '0');
    return `${h}:${m}`;
}

function timeToMinutes(time) {
    const [h, m] = time.split(':').map(Number);
    return h * 60 + m;
}

function updateFromSliders() {
    let s = +startSlider.value;
    let e = +endSlider.value;
    if (s > e) [s, e] = [e, s];
    startSlider.value = s;
    endSlider.value   = e;
    startTimeInput.value = formatTime(s);
    endTimeInput.value   = formatTime(e);
}

function updateFromInputs() {
    let s = timeToMinutes(startTimeInput.value);
    let e = timeToMinutes(endTimeInput.value);
    if (s > e) [s, e] = [e, s];
    startSlider.value = s;
    endSlider.value   = e;
}

function adjustTime(inputElem, delta) {
    let mins = timeToMinutes(inputElem.value) + delta;
    mins = Math.max(0, Math.min(1439, mins));
    inputElem.value = formatTime(mins);
    updateFromInputs();
}

startSlider.addEventListener('input', updateFromSliders);
endSlider.addEventListener('input', updateFromSliders);
startTimeInput.addEventListener('input', updateFromInputs);
endTimeInput.addEventListener('input', updateFromInputs);

startMinusBtn.addEventListener('click', () => adjustTime(startTimeInput, -1));
startPlusBtn.addEventListener('click',  () => adjustTime(startTimeInput,  1));
endMinusBtn.addEventListener('click',   () => adjustTime(endTimeInput,   -1));
endPlusBtn.addEventListener('click',    () => adjustTime(endTimeInput,    1));

updateFromSliders();