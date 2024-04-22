from flask import Flask, request, jsonify, abort
from flask_sqlalchemy import SQLAlchemy
from sqlalchemy.exc import IntegrityError, SQLAlchemyError
from flask_cors import CORS
from flask_marshmallow import Marshmallow
import datetime
from flask_socketio import SocketIO, emit
from sqlalchemy import func
from flask import render_template
import jwt
from flask_bcrypt import Bcrypt
from .db_config import DB_CONFIG

app = Flask(__name__)

app.config['SQLALCHEMY_DATABASE_URI'] = DB_CONFIG
CORS(app)
db = SQLAlchemy(app)
ma = Marshmallow(app)
socketio = SocketIO(app)
bcrypt = Bcrypt(app)


from .model.user import User, user_schema
from .model.celldata import celldata_schema, CellData

SECRET_KEY = "b'|\xe7\xbfU3`\xc4\xec\xa7\xa9zf:}\xb5\xc7\xb9\x139^3@Dv'"


def create_token(user_id):
    payload = {
        'exp': datetime.datetime.utcnow() + datetime.timedelta(days=4),
        'iat': datetime.datetime.utcnow(),
        'sub': user_id
    }
    return jwt.encode(
        payload,
        SECRET_KEY,
        algorithm='HS256'
    )


def extract_auth_token(authenticated_request):
    auth_header = authenticated_request.headers.get('Authorization')
    if auth_header:
        return auth_header.split(" ")[1]
    else:
        return None


def decode_token(token):
    payload = jwt.decode(token, SECRET_KEY, 'HS256')
    return payload['sub']


@app.route('/authentication', methods=['POST'])
def authentication():
    try:
        if "user_name" not in request.json or "password" not in request.json:
            abort(400)
        username = request.json["user_name"]
        password = request.json["password"]
        user = User.query.filter_by(user_name=username).first()
        if not user:
            abort(403)
        if not bcrypt.check_password_hash(user.hashed_password, password):
            abort(403)
        token = create_token(user.id)
        return jsonify({"token": token}), 200
    except Exception as e:
        return jsonify({"message": str(e)}), 500


@app.route('/user', methods=['POST'])
def create_user():
    try:
        user_name = request.json["user_name"]
        password = request.json["password"]
        user = User(user_name, password)
        db.session.add(user)
        db.session.commit()
        return jsonify(user_schema.dump(user)), 201

    except IntegrityError:
        db.session.rollback() 
        return jsonify({"message": "Username taken"}), 409  
    except KeyError:
        return jsonify({"message": "Missing required fields"}), 400  
    except Exception as e:
        return jsonify({"message": str(e)}), 500



def retrieve_statistics(statistics):
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

    signal_power_device = [stat.signalPower for stat in statistics if stat.signalPower != None]
    signal_power_avg_device = round((sum(signal_power_device) / len(signal_power_device)), 2) if len(
        signal_power_device) != 0 else 0

    return jsonify({
        "operators": operators,
        "network_types": network_types,
        "signal_powers": signal_powers,
        "signal_power_avg_device": signal_power_avg_device,
        "sinr_snr": sinr_snr
    })

@app.route('/cellData', methods=['POST'])
def add_cell_data():
    token = extract_auth_token(request)
    if not token:
        abort(403)
    else:
        try:
            user_id = decode_token(token)
        except:
            abort(403)
    try:
        data = request.json
        cell_data = CellData(
            operator=data['operator'] if 'operator' in request.json else None,
            signalPower=data['signalPower'] if 'signalPower' in request.json else None,
            sinr_snr=data['sinr_snr'] if 'sinr_snr' in request.json else None,
            networkType=data['networkType'] if 'networkType' in request.json else None,
            frequency_band=data['frequency_band'] if 'frequency_band' in request.json else None,
            cell_id=data['cell_id'] if 'cell_id' in request.json else None,
            timestamp=datetime.datetime.strptime(data['timestamp'], '%d %b %Y %I:%M:%S %p') if 'timestamp' in request.json else None,
            user_ip=data['user_ip'] if 'user_ip' in request.json else None,
            user_mac=data['user_mac'] if 'user_mac' in request.json else None,
            user_id= user_id
        )

        if cell_data.signalPower == None and cell_data.sinr_snr == None and cell_data.networkType == None and cell_data.frequency_band == None and cell_data.cell_id == None:
            return jsonify({'message': "Empty entry"}), 404

        db.session.add(cell_data)
        db.session.commit()
        return jsonify({'message': celldata_schema.dump(cell_data)}), 201

    except Exception as e:
        print(e)
        return jsonify({'error': 'Something went wrong'}), 500


@app.route('/statistics', methods=['POST'])
def get_statistics():
    token = extract_auth_token(request)
    if not token:
        abort(403)
    else:
        try:
            user_id = decode_token(token)
        except:
            abort(403)
    data = request.json
    START_DATE = datetime.datetime.strptime(data['start_date'], '%Y-%m-%d %H:%M')
    END_DATE = datetime.datetime.strptime(data['end_date'], '%Y-%m-%d %H:%M')
    
    statistics = CellData.query.filter_by(user_id=user_id).filter(
        CellData.timestamp.between(START_DATE, END_DATE)).all()

    return retrieve_statistics(statistics),200
    
  

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
    latest_devices_query = db.session.query(CellData.user_ip,
                                            func.max(CellData.timestamp).label('max_timestamp')). \
        group_by(CellData.user_ip).subquery()

    # Join with CellData table to get the complete rows
    latest_devices = db.session.query(CellData). \
        join(latest_devices_query,
             db.and_(CellData.user_ip == latest_devices_query.c.user_ip,
                     CellData.timestamp == latest_devices_query.c.max_timestamp)).all()

    # Populate the previous_devices dictionary
    for device in latest_devices:
        if all(device.user_ip != device_info["user_ip"] for device_info in connected_devices.values()):
            previous_devices[device.user_ip] = {"user_ip": device.user_ip, "user_mac": device.user_mac}


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
        user_ip = user_data.get('user_ip')
        if user_ip:
            # Move the device from connected_devices to previous_devices
            previous_devices[user_ip] = connected_devices.pop(session_id)

            print("User IP:", previous_devices[user_ip]['user_ip'])
            print("User MAC:", previous_devices[user_ip]['user_mac'])

    emit('disconnection_ack', {'message': 'Disconnected from server'})


@socketio.on('user_data')
def handle_user_data(data):
    user_ip = data.get('user_ip')
    user_mac = data.get('user_mac')
    session_id = request.sid  # Get the session ID

    # Store the association between session ID, IP, and MAC address
    connected_devices[session_id] = {'user_ip': user_ip, 'user_mac': user_mac}

    # Query the database to get the latest entry for the current device
    latest_device = CellData.query.filter_by(user_ip=user_ip).order_by(CellData.timestamp.desc()).first()

    if latest_device:
        # Add the latest device to previous_devices
        previous_devices[user_ip] = {"user_ip": latest_device.user_ip, "user_mac": latest_device.user_mac}
    else:
        # If no entry found, use the provided IP
        previous_devices[user_ip] = {"user_ip": user_ip, "user_mac": user_mac}

    # If the device is in previous_devices, move it to connected_devices
    if user_ip in previous_devices:
        previous_devices.pop(user_ip)  # Delete the matching entry

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
    for user_ip, data in previous_devices.items():
        previous_devices_json[user_ip] = {
            'user_ip': data.get("user_ip"),
            'user_mac': data.get("user_mac")
        }
        # Collect all user IPs from connected_devices_json
    user_ips = set(device['user_ip'] for device in connected_devices_json.values())
    statistics = CellData.query.filter(CellData.user_ip.in_(user_ips)).all()
    device_statistics = retrieve_statistics(statistics).get_json()


    return jsonify({
        "connected_devices": len(connected_devices_json),
        "previous_devices": previous_devices_json,
        "current_devices": connected_devices_json,
        "device_statistics": device_statistics
    })
