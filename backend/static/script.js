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

            // Update for device statistics
            let stats = data.device_statistics;
            let networkTypesHtml = '';
            for (const [key, value] of Object.entries(stats.network_types)) {
                networkTypesHtml += `<li>${key}: ${value}%</li>`;
            }
            if (!networkTypesHtml) networkTypesHtml = '<li>Not Available</li>';

            let operatorsHtml = '';
            for (const [key, value] of Object.entries(stats.operators)) {
                operatorsHtml += `<li>${key}: ${value}%</li>`;
            }
            if (!operatorsHtml) operatorsHtml = '<li>Not Available</li>';

            let signalPowersHtml = '';
            for (const [key, value] of Object.entries(stats.signal_powers)) {
                signalPowersHtml += `<li>${key}: ${value} dBm</li>`;
            }
            if (!signalPowersHtml) signalPowersHtml = '<li>Not Available</li>';

            let sinrSnrHtml = '';
            for (const [key, value] of Object.entries(stats.sinr_snr)) {
                sinrSnrHtml += `<li>${key}: ${value} dB</li>`;
            }
            if (!sinrSnrHtml) sinrSnrHtml = '<li>Not Available</li>';

            document.getElementById('devicestatistics').innerHTML = `
                <h5>Connectivity Time per Operator:</h5>
                <ul>${operatorsHtml}</ul>
                <h5>Connectivity Time per Network Type:</h5>
                <ul>${networkTypesHtml}</ul>
                <h5>Signal Power per Network Type:</h5>
                <ul>${signalPowersHtml}</ul>
                <h5>Average Signal Power of Devices:</h5>
                <ul>${stats.signal_power_avg_device ? stats.signal_power_avg_device + ' dBm' : 'Not Available'}</ul>
                <h5>SNR per Network Type:</h5>
                <ul>${sinrSnrHtml}</ul>
            `;
        })
        .catch(error => console.error('Error fetching centralized statistics:', error));
}

window.onload = fetchCentralizedStatistics;
setInterval(fetchCentralizedStatistics, 1000);
