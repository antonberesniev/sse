const url = "sse-ping-server/stream-sse";
const eventSource = new EventSource(url);
const eventsList = document.querySelector('ul#events');

function addPing(text) {
    addEvent("PING: " + text);
}

function addMessage(text) {
    addEvent("MESSAGE: " + text);
}

function addEvent(text) {
    const newElement = document.createElement('li');
    newElement.textContent = text;
    eventsList.appendChild(newElement);
}

eventSource.onopen = () => {
    console.log('Connected to channel');
}

eventSource.addEventListener("message", (event) => {
    console.log('Message:' + event.data);
    addMessage(event.data);
});

eventSource.addEventListener("ping", (event) => {
    console.log('Ping:' + event.data);
    addPing(event.data);
});

eventSource.onerror = (event) => {
    console.log('Error: ' + event.data);
}