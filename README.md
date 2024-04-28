# Mobile Network Data Collection Application

## Project Overview
This project taps into the potential of smartphones to automate network measurements across all generations of mobile networks. Using the smartphone's APIs, this application allows numerous mobile users to collect and report measurements to data centers. The app supports multiple network standards including GSM/GPRS/EDGE (2G/2.5G), UMTS (3G), and LTE (4G), and utilizes the Android API to obtain various cell-related information.

## Android Application Functionality

### Data Collection
- Gathers cell info from the actively connected base station.

### Supported Networks
- GSM/GPRS/EDGE, UMTS, and LTE.

### Data Sent to Server
- Operator
- Signal Power
- SINR/SNR when applicable
- Network Type
- Frequency Band (if available)
- Cell ID
- Timestamp

## Server Design
The server is designed to receive network cell data from various phones regularly (e.g., every 10 seconds). It manages multiple connections simultaneously, stores received data in a database, and provides statistics based on data queries from the Android application.

## Data Statistics and Reporting
The server and app will provide insights such as:
- Average connectivity time per operator and network type.
- Average Signal Power per network type and per device.
- Average SINR or SNR per network type.
- Real-time cell info display on the Android app.
- Customizable statistical data reporting between two specific dates selected by the user.

## Server Interface Features
- Displays the number of connected mobile devices.
- Shows IP and MAC addresses of connected devices.
- Optionally provides detailed statistics for connected mobile devices.

## Setup and Installation Instructions

### Clone the Repository
- Get the project code by cloning the repository.

### Configure the Android Application
- Open with Android Studio.
- Set up your development environment and device configurations.
- Run the application to start data collection.

### Prepare and Run the Server and Application
- Extract the zipped folder
- For the database open MySQL and create on localhost:3306 a database <mysql_db_name>
- Create a venv for the backend folder
- Install requirements using pip -r install requirements.txt
- Add db_config.py file to specify MySQL database name and username/password by adding the following variable:
app.config['SQLALCHEMY_DATABASE_URI'] = 'mysql+pymysql://<mysql_username>:<mysql_password>@<mysql_host>:<mysql_port>/<mysql_db_name>'
- now open a python flask shell using: python -m flask shell
- Type the following:
- 1) from backend.app import db
- 2) db.create_all()
- 3) exit()
- run the flask server by using the following command: flask run -h 0.0.0.0
-copy the link where the server is running and save it for later
- Open the frontend folder in android studio
- place the copied link in the retrofit.kt file in the API_URL variable and in the MainActivity.kt in the SOCKET_URL variable
- Build your project and run
- Begin processing and storing data.

## Usage Instructions
- **Mobile App Usage**: Start the application on an Android device to begin the data collection and transmission to the server.
- **Server Monitoring**: Access the server interface to monitor data inflow, manage the database, and generate reports.
## Dependencies
Add the following lines to your `build.gradle` file under `dependencies` to include necessary libraries and frameworks for the Android application:

```gradle
dependencies {
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.11.0") // This line is duplicated and can be removed
    implementation("io.socket:socket.io-client:2.0.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}


## Contribution and Development
Contributors can fork the project, make modifications, and propose changes through pull requests. Contributions are encouraged to improve the application and server functionalities.

## Licensing
This project is released under a standard open-source license, facilitating free use, modification, and distribution under the defined terms.
