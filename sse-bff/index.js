import express from "express"
import EventSource from "eventsource"

const app = express()
const port = 3000

app.get('/', (req, res) => {
    res.send("PoC SSE Bff")
})

app.get('/events', (req, res) => {
    // Set headers for SSE
    res.writeHead(200, {
        'Content-Type': 'text/event-stream',
        'Cache-Control': 'no-cache',
        'Connection': 'keep-alive',
    });

    // Create a new EventSource instance to listen to the Spring Boot SSE endpoint
    const es = new EventSource('http://localhost:8080/sse');


    es.onopen = function () {
        console.log('EventSource connected');
        es.addEventListener('INIT', function (e) {
            res.write(`data: ${e.data}\n\n`);
        })
    };

    es.onerror = function (error) {
        console.error('EventSource error:', error);
        res.write(`data: ${error.type}\n\n`);
        es.close()
    };

    es.addEventListener('ping', function (e) {
        console.log(e.data)
        res.write(`data: ${e.data}\n\n`);
    })

    // Handle client disconnect
    req.on('close', (e) => {
        console.log('Client disconnected');
        res.write(`data: ${e.data}\n\n`);
        es.close(); // Close the EventSource connection when the client disconnects
    });
});


app.listen(port, () => {
    console.log(`Example app listening on port ${port}`)
})