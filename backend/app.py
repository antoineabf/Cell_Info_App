from flask import Flask, request, jsonify, abort
from flask_sqlalchemy import SQLAlchemy
from flask_cors import CORS
from flask_marshmallow import Marshmallow
from datetime import datetime

app = Flask(__name__)

app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql+pymysql://root:mysql123@localhost:3306/project451'
CORS(app)
db = SQLAlchemy(app)
ma = Marshmallow(app)

from .model.celldata import CellData, celldata_schema


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
            user_ip = request.remote_addr
        )
        db.session.add(cell_data)
        db.session.commit()
        return jsonify({'message': celldata_schema.dump(cell_data)}), 201

    except:
        return jsonify({'error': 'Something went wrong'}), 500


@app.route('/statistics', methods=['POST'])
def get_statistics():
    data = request.json
    START_DATE = datetime.strptime(data['start_date'], '%d %b %Y %I:%M %p')
    END_DATE = datetime.strptime(data['end_date'], '%d %b %Y %I:%M %p')
    try:
        client_ip = request.remote_addr
    except:
        abort(400)

    statistics = CellData.query.filter_by(user_ip=client_ip).filter(
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

    signal_power_device = [stat.signalPower for stat in statistics if stat.user_ip == client_ip]
    signal_power_avg_device = round((sum(signal_power_device) / len(signal_power_device)), 2) if len(
        signal_power_device) != 0 else 0

    return jsonify({
        "operators": operators,
        "network_types": network_types,
        "signal_powers": signal_powers,
        "signal_power_avg_device": signal_power_avg_device,
        "sinr_snr": sinr_snr
    }), 200
