from flask import Flask, request, jsonify
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
            timestamp=datetime.strptime(data['timestamp'], '%d %b %Y %I:%M %p')
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
    cell_id_user = data['cell_id']

    statistics = CellData.query.filter_by(cell_id=cell_id_user).filter(
        CellData.timestamp.between(START_DATE, END_DATE)).all()

    operators = {}

    for stat in statistics:
        if stat.operator not in operators:
            operators[stat.operator] = 1
        else:
            operators[stat.operator] += 1

    for stat in operators:
        operators[stat] = round(operators[stat]/len(statistics) *100,2)

    # alfa_count = sum(1 for stat in statistics if stat.operator == "Alfa")
    # touch_count = sum(1 for stat in statistics if stat.operator == "Touch")
    # total_count_operator = alfa_count + touch_count
    # percentage_alfa = round((alfa_count / total_count_operator * 100), 2) if total_count_operator != 0 else 0
    # percentage_touch = round((touch_count / total_count_operator * 100), 2) if total_count_operator != 0 else 0

    count_4G = sum(1 for stat in statistics if stat.networkType == "4G")
    count_3G = sum(1 for stat in statistics if stat.networkType == "3G")
    count_2G = sum(1 for stat in statistics if stat.networkType == "2G")
    total_count_network = count_4G + count_3G + count_2G

    percentage_4G = round((count_4G / total_count_network * 100), 2) if total_count_network != 0 else 0
    percentage_3G = round((count_3G / total_count_network * 100), 2) if total_count_network != 0 else 0
    percentage_2G = round((count_2G / total_count_network * 100), 2) if total_count_network != 0 else 0

    signal_power_4G = [stat.signalPower for stat in statistics if stat.networkType == "4G"]
    signal_power_3G = [stat.signalPower for stat in statistics if stat.networkType == "3G"]
    signal_power_2G = [stat.signalPower for stat in statistics if stat.networkType == "2G"]

    signal_power_avg_4G = round((sum(signal_power_4G) / len(signal_power_4G)), 2) if len(signal_power_4G) != 0 else 0
    signal_power_avg_3G = round((sum(signal_power_3G) / len(signal_power_3G)), 2) if len(signal_power_3G) != 0 else 0
    signal_power_avg_2G = round((sum(signal_power_2G) / len(signal_power_2G)), 2) if len(signal_power_2G) != 0 else 0

    signal_power_device = [stat.signalPower for stat in statistics if stat.cell_id == cell_id_user]
    signal_power_avg_device = round((sum(signal_power_device) / len(signal_power_device)), 2) if len(
        signal_power_device) != 0 else 0

    sinr_snr_4G = [stat.sinr_snr for stat in statistics if stat.networkType == "4G"]
    sinr_snr_3G = [stat.sinr_snr for stat in statistics if stat.networkType == "3G"]
    sinr_snr_2G = [stat.sinr_snr for stat in statistics if stat.networkType == "2G"]

    sinr_snr_avg_4G = round((sum(sinr_snr_4G) / len(sinr_snr_4G)), 2) if len(sinr_snr_4G) != 0 else 0
    sinr_snr_avg_3G = round((sum(sinr_snr_3G) / len(sinr_snr_3G)), 2) if len(sinr_snr_3G) != 0 else 0
    sinr_snr_avg_2G = round((sum(sinr_snr_2G) / len(sinr_snr_2G)), 2) if len(sinr_snr_2G) != 0 else 0

    return jsonify({
        "operators": operators,
        "percentage_4G": percentage_4G,
        "percentage_3G": percentage_3G,
        "percentage_2G": percentage_2G,
        "signal_power_avg_4G": signal_power_avg_4G,
        "signal_power_avg_3G": signal_power_avg_3G,
        "signal_power_avg_2G": signal_power_avg_2G,
        "signal_power_avg_device": signal_power_avg_device,
        "sinr_snr_avg_4G": sinr_snr_avg_4G,
        "sinr_snr_avg_3G": sinr_snr_avg_3G,
        "sinr_snr_avg_2G": sinr_snr_avg_2G
    }), 200
