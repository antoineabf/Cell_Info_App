from ..app import db, ma


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


class CellDataSchema(ma.Schema):
    class Meta:
        fields = ("operator", "signalPower", "sinr_snr", "networkType", "frequency_band", "cell_id", "timestamp")
        model = CellData


celldata_schema = CellDataSchema()
