from flask import Flask, request, jsonify, abort
from flask_sqlalchemy import SQLAlchemy
from flask_cors import CORS
from flask_marshmallow import Marshmallow
from datetime import datetime
from flask_socketio import SocketIO, emit

app = Flask(__name__)

app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql+pymysql://root:Abed12345@localhost:3306/project451'
CORS(app)
db = SQLAlchemy(app)
ma = Marshmallow(app)
socketio = SocketIO(app)

from backend.model.celldata import CellData, celldata_schema


@app.route('/cellData', methods=['POST'])
def add_cell_data():
    try:
        data = request.json
        cell_data = CellData(
            operator=data['operator'],
            signalPower=data['signalPower'],
            sinr_snr=data['sinr_snr'],
            networkType=data['networkType'],
            frequency_band=data['frequency_band'],
            cell_id=data['cell_id'],
            timestamp=datetime.strptime(data['timestamp'], '%d %b %Y %I:%M %p'),
            user_ip = data['user_ip'], #user_ip = request.remote_addr  # Get the IP address of the client
            user_mac = data['user_mac']
        )
        db.session.add(cell_data)
        db.session.commit()
        return jsonify({'message': celldata_schema.dump(cell_data)}), 201

    except Exception as e:
        print(e)
        return jsonify({'error': 'Something went wrong'}), 500


@app.route('/statistics', methods=['POST'])
def get_statistics():
    data = request.json
    START_DATE = datetime.strptime(data['start_date'], '%d %b %Y %I:%M %p')
    END_DATE = datetime.strptime(data['end_date'], '%d %b %Y %I:%M %p')
    client_mac = data['user_mac']

    statistics = CellData.query.filter_by(user_mac=client_mac).filter(
        CellData.timestamp.between(START_DATE, END_DATE)).all()

    operators = {}
    network_types = {}

    for stat in statistics:
        if stat.operator not in operators:
            operators[stat.operator] = 1
        else:
            operators[stat.operator] += 1

        if stat.networkType not in network_types:
            network_types[stat.networkType] = 1
        else:
            network_types[stat.networkType] += 1

    for op in operators:
        operators[op] = round(operators[op] / len(statistics) * 100, 2) if len(statistics) != 0 else 0

    for net in network_types:
        network_types[net] = round(network_types[net] / len(statistics) * 100, 2) if len(statistics) != 0 else 0

    signal_powers = {}
    sinr_snr = {}

    for net in network_types:
        signal_powers[net] = 0
        sinr_snr[net] = 0
        count = 0
        for stat in statistics:
            if stat.networkType == net:
                signal_powers[net] += stat.signalPower
                sinr_snr[net] += stat.sinr_snr
                count += 1
        signal_powers[net] = round(signal_powers[net] / count, 2) if count != 0 else 0
        sinr_snr[net] = round(sinr_snr[net] / count, 2) if count != 0 else 0

    signal_power_device = [stat.signalPower for stat in statistics if stat.user_mac == client_mac]
    signal_power_avg_device = round((sum(signal_power_device) / len(signal_power_device)), 2) if len(
        signal_power_device) != 0 else 0

    return jsonify({
        "operators": operators,
        "network_types": network_types,
        "signal_powers": signal_powers,
        "signal_power_avg_device": signal_power_avg_device,
        "sinr_snr": sinr_snr
    }), 200

from flask import render_template

@app.route('/')
def index():
    return render_template('index.html')

# Maintain a dictionary to store currently connected devices with their IP addresses
connected_devices = {}

# Maintain a dictionary to store previously connected devices with their IP addresses
previous_devices = {}

# Maintain a dictionary to store per-device statistics
device_statistics = {}

import time
# Event handler for when a client connects
@socketio.on('connect')
def handle_connect():
    user_sid = request.sid
    user_ip_request = request.remote_addr # Get the IP address of the client
    if user_sid not in connected_devices:
        emit('connection_ack', {'message': 'Connected to server'})
        #cell_data = CellData.query.filter_by(user_ip=user_ip_request).all()
        cell_data = db.session.query(CellData).filter_by(user_ip=user_ip_request).first() 
        if cell_data is not None:
            connected_devices[user_sid] = cell_data
        #print(connected_devices)

# Event handler for when a client disconnects
@socketio.on('disconnect')
def handle_disconnect():
    user_sid = request.sid
    user_ip_request = request.remote_addr # Get the IP address of the client
    if user_sid in connected_devices:
        #cell_data = CellData.query.filter_by(user_ip=user_ip_request).all()
        cell_data = db.session.query(CellData).filter_by(user_ip=user_ip_request).first() 
        previous_devices[user_sid] = cell_data
        del connected_devices[user_sid]
    emit('disconnection_ack', {'message': 'Disconnected from server'})

@app.route('/centralized-statistics', methods=['GET'])
def centralized_statistics():
    connected_devices_json = {}
    previous_devices_json = {}

    # Convert connected_devices dictionary to JSON serializable format
    for user_sid, cell_data in connected_devices.items():
        connected_devices_json[user_sid] = {
            'id': cell_data.id,
            'operator': cell_data.operator,
            'signalPower': cell_data.signalPower,
            'sinr_snr': cell_data.sinr_snr,
            'networkType': cell_data.networkType,
            'frequency_band': cell_data.frequency_band,
            'cell_id': cell_data.cell_id,
            'timestamp': cell_data.timestamp.strftime('%d %b %Y %I:%M %p'),
            'user_ip': cell_data.user_ip,
            'user_mac': cell_data.user_mac
        }

    # Convert previous_devices dictionary to JSON serializable format
    for user_sid, cell_data in previous_devices.items():
        previous_devices_json[user_sid] = {
            'id': cell_data.id,
            'operator': cell_data.operator,
            'signalPower': cell_data.signalPower,
            'sinr_snr': cell_data.sinr_snr,
            'networkType': cell_data.networkType,
            'frequency_band': cell_data.frequency_band,
            'cell_id': cell_data.cell_id,
            'timestamp': cell_data.timestamp.strftime('%d %b %Y %I:%M %p'),
            'user_ip': cell_data.user_ip,
            'user_mac': cell_data.user_mac
        }

    return jsonify({
        "connected_devices": len(connected_devices_json),
        "previous_devices": previous_devices_json,
        "current_devices": connected_devices_json,
        "device_statistics": device_statistics
    })
