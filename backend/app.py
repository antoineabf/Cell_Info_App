from flask import Flask, request, jsonify
from flask_sqlalchemy import SQLAlchemy
from datetime import datetime

app = Flask(__name__)

app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql+pymysql://root:mysql123@localhost:3306/project451'
db = SQLAlchemy(app)


class CellData(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    operator = db.Column(db.String(50), nullable=False)
    signalPower = db.Column(db.Float, nullable=False)
    sinr_snr = db.Column(db.Float, nullable=False)
    networkType = db.Column(db.String(20), nullable=False)
    frequency_band = db.Column(db.String(20), nullable=True)
    cell_id = db.Column(db.String(50), nullable=False)
    timestamp = db.Column(db.DateTime, nullable=False)

    def __init__(self, operator, signalPower, sinr_snr, networkType, frequency_band, cell_id, timestamp):
        self.operator = operator
        self.signalPower = signalPower
        self.sinr_snr = sinr_snr
        self.networkType = networkType
        self.frequency_band = frequency_band
        self.cell_id = cell_id
        self.timestamp = timestamp


@app.route('/hello', methods=['GET'])
def hello_world():
    return "Hello World!"

@app.route('/cellData', methods=['POST'])
def add_cell_data():
    try:
        data = request.json
        cellData = CellData(
            operator=data['operator'],
            signalPower=data['signalPower'],
            sinr_snr=data['sinr_snr'],
            networkType=data['networkType'],
            frequency_band=data['frequency_band'],
            cell_id=data['cell_id'],
            timestamp=datetime.strptime(data['timestamp'], '%d %b %Y %I:%M %p')
        )
        db.session.add(cellData)
        db.session.commit()
        return jsonify({'message': 'Cell data received and saved successfully'}), 201

    except:
        return jsonify({'error': 'Something went wrong'}), 500