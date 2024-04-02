// JavaScript code
function fetchCentralizedStatistics() {
    fetch('/centralized-statistics')
        .then(response => response.json())
        .then(data => {
            document.getElementById('connecteddevices').textContent = data.connected_devices;

            document.getElementById('previousdevices').innerHTML = '<tr><th>IP Address</th><th>MAC Address</th></tr>';
            Object.entries(data.previous_devices).forEach(([key, value]) => {
                document.getElementById('previousdevices').innerHTML += `
                    <tr>
                        <td>${value.user_ip}</td>
                        <td>${value.user_mac}</td>
                    </tr>
                `;
            });

            document.getElementById('currentdevices').innerHTML = '<tr><th>IP Address</th><th>MAC Address</th></tr>';
            Object.entries(data.current_devices).forEach(([key, value]) => {
                document.getElementById('currentdevices').innerHTML += `
                    <tr>
                        <td>${value.user_ip}</td>
                        <td>${value.user_mac}</td>
                    </tr>
                `;
            });

            document.getElementById('devicestatistics').textContent = data.device_statistics;
        })
        .catch(error => console.error('Error fetching centralized statistics:', error));
}

window.onload = fetchCentralizedStatistics;
setInterval(fetchCentralizedStatistics, 1000);