const io = require('socket.io-client');
const axios = require('axios');

// Connect to the server
const socket = io.connect('http://127.0.0.1:5000'); // Assuming your server is running on 127.0.0.1:5000

// Event handler for connection acknowledgment
socket.on('connection_ack', (data) => {
    console.log('Connected:', data.message);

    // Make API call to /cellData endpoint
    axios.post('http://127.0.0.1:5000/cellData', {
        operator: 'Touch',
        signalPower: -85,
        sinr_snr: 20,
        networkType: '2G',
        frequency_band: '7(23MHz)',
        cell_id: '12345',
        timestamp: '01 Apr 2024 4:00 PM',
        user_ip: '127.0.0.1',
        user_mac: '00:1A:2B:3C:4D:5E'
    })
    .then(response => {
        console.log('Cell data added successfully:', response.data.message);
        // Emit data sent acknowledgment to the server
        socket.emit('data_sent_ack', { message: 'Cell data received and processed successfully' });
    })
    .catch(error => {
        console.error('Error adding cell data:', error);
    });
});

// Event handler for disconnection acknowledgment
socket.on('disconnection_ack', (data) => {
    console.log('Disconnected:', data.message);
});

// Simulate disconnection after 10 seconds
setTimeout(() => {
    socket.disconnect();
}, 20000);
