from flask import Flask, request, jsonify, abort
from flask_sqlalchemy import SQLAlchemy
from flask_cors import CORS
from flask_marshmallow import Marshmallow
from datetime import datetime
from flask_socketio import SocketIO, emit
from sqlalchemy import func

app = Flask(__name__)

app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql+pymysql://root:Toto2003#@localhost:3306/project451'
CORS(app)
db = SQLAlchemy(app)
ma = Marshmallow(app)
socketio = SocketIO(app)

from .model.celldata import celldata_schema, CellData


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
            user_ip=data['user_ip'],
            user_mac=data['user_mac']
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
    return render_template("index.html")


# Maintain a dictionary to store currently connected devices with their IP addresses
connected_devices = {}

# Maintain a dictionary to store previously connected devices with their IP addresses
previous_devices = {}

# Maintain a dictionary to store per-device statistics
device_statistics = {}


# In your centralized_statistics route or any other appropriate place
def fetch_previous_devices():
    # Query for the latest entry for each MAC address
    latest_devices_query = db.session.query(CellData.user_mac,
                                            func.max(CellData.timestamp).label('max_timestamp')). \
        group_by(CellData.user_mac).subquery()

    # Join with CellData table to get the complete rows
    latest_devices = db.session.query(CellData). \
        join(latest_devices_query,
             db.and_(CellData.user_mac == latest_devices_query.c.user_mac,
                     CellData.timestamp == latest_devices_query.c.max_timestamp)).all()

    # Populate the previous_devices dictionary
    for device in latest_devices:
        previous_devices[device.user_mac] = {"user_ip": device.user_ip, "user_mac": device.user_mac}


# Event handler for when a client connects
@socketio.on('connect')
def handle_connect():
    print('connected')


# Event handler for when a client disconnects
@socketio.on('disconnect')
def handle_disconnect():
    print('disconnected')
    session_id = request.sid  # Get the session ID
    user_data = connected_devices.get(session_id)

    if user_data:
        user_mac = user_data.get('user_mac')
        if user_mac:
            # Move the device from connected_devices to previous_devices
            previous_devices[user_mac] = connected_devices.pop(session_id)

            print("User IP:", previous_devices[user_mac]['user_ip'])
            print("User MAC:", user_mac)

    emit('disconnection_ack', {'message': 'Disconnected from server'})



@socketio.on('user_data')
def handle_user_data(data):
    user_ip = data.get('user_ip')
    user_mac = data.get('user_mac')
    session_id = request.sid  # Get the session ID

    # Store the association between session ID, IP, and MAC address
    connected_devices[session_id] = {'user_ip': user_ip, 'user_mac': user_mac}

    # Query the database to get the latest entry for the current device
    latest_device = CellData.query.filter_by(user_mac=user_mac).order_by(CellData.timestamp.desc()).first()

    if latest_device:
        # Add the latest device to previous_devices
        previous_devices[user_mac] = {"user_ip": latest_device.user_ip, "user_mac": latest_device.user_mac}
    else:
        # If no entry found, use the provided IP
        previous_devices[user_mac] = {"user_ip": user_ip, "user_mac": user_mac}

    # If the device is in previous_devices, move it to connected_devices
    if user_mac in previous_devices:
        del previous_devices[user_mac]  # Delete the matching entry

    connected_devices[session_id] = {"user_ip": user_ip, "user_mac": user_mac}



@app.route('/centralized-statistics', methods=['GET'])
def centralized_statistics():
    fetch_previous_devices()
    connected_devices_json = {}
    previous_devices_json = {}

    # Convert connected_devices dictionary to JSON serializable format
    for user_sid, data in connected_devices.items():
        connected_devices_json[user_sid] = {
            'user_ip': data.get('user_ip'),
            'user_mac': data.get('user_mac')
        }

    # Convert previous_devices dictionary to JSON serializable format
    for user_mac, data in previous_devices.items():
        previous_devices_json[user_mac] = {
            'user_ip': data.get("user_ip"),
            'user_mac': data.get("user_mac")
        }

    return jsonify({
        "connected_devices": len(connected_devices_json),
        "previous_devices": previous_devices_json,
        "current_devices": connected_devices_json,
        "device_statistics": device_statistics
    })
