#include <ESP8266HTTPClient.h>
#include <ArduinoJson.h>
#include <FirebaseArduino.h>
#include "ESP8266WiFi.h"
#include <NTPClient.h>
#include <WiFiUdp.h>
#include <Wire.h>

//============================== Firebase ==============================
#define FIREBASE_HOST "arduinoworkshop-bfda3.firebaseio.com" 
#define FIREBASE_AUTH "Kpu0wgc4WT18iV2l4MV3MMYNAFUlcXlCSWViyLUn" 
//============================== Firebase ==============================

//================================ Wifi ================================
WiFiServer  server(80);
//char        ssid[]      = "Oslo";            // your network SSID name
//char        pass[]      = "0526586120";      // your network password
char        ssid[]      = "HOTBOX 4-BC50";            // your network SSID name
char        pass[]      = "0542005293";      // your network password
//================================ Wifi ================================

//============================== Location ==============================
WiFiClientSecure  client;
DynamicJsonBuffer jsonBuffer;
char              bssid[6];
int               n;
const char*       Host        =     "www.googleapis.com";
String            thisPage    =     "/geolocation/v1/geolocate?key=";
String            key         =     "AIzaSyAgDkTp-T9Y_aOzOyxQC0OdEfbKwTo84So";
int               status      =     WL_IDLE_STATUS;
String            jsonString  =     "{\n";
double            latitude    =     0.0;
double            longitude   =     0.0;
int               more_text   =     1;    // set to 1 for more debug output
//============================== Location ==============================

//================================ Date ================================
WiFiUDP   ntpUDP;
NTPClient timeClient(ntpUDP);
String    formattedDate;
String    dayStamp;
String    timeStamp;
//================================ Date ================================

//================================ Pulse ===============================
int           sensorPin       =     A0;                 // A0 is the input pin for the heart rate sensor
boolean       isPulseAnomaly  =     false;
float         mes;
float         maxValue        =     0;
float         minValue        =     2000;
float         firstRead;
float         secondRead;
int           startTime       =     millis();
int           pulseCount      =     1;
int           pulse;
//================================ Pulse ===============================

//=========================== Fall Detection ===========================
// MMA8452Q I2C address is 0x1C(28)
#define Addr 0x1C
unsigned long currentTime     =   0; 
unsigned long timeAtFall      =   0;
float         currentAcc      =   0;
float         gForce          =   0;
int           fallCount       =   0;
float         maxFallValue    =   0;
const float   firstThreshold  =   100.0;
const float   shockThreshold  =   70.0;
const float   secondThreshold =   300.0;
const float   thiredThreshold =   110.0;
const int     delayTime       =   50;
bool          isFallDetected  =   false;
static float  xyz_g[3];
//=========================== Fall Detection ===========================

void setup()   
{
  // Connect to WIFI service:
  Serial.begin(115200);
  WiFi.mode(WIFI_STA);
  WiFi.disconnect();
  delay(100);
  //Serial.print("Connecting to ");
  //Serial.println(ssid);
  //WiFi.begin(ssid, pass);
  /*while (WiFi.status() != WL_CONNECTED) 
  {
    delay(500);
    Serial.print(".");
  }*/

  // Connect to Firebase:
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH); 

  // Connect to Time client:
  timeClient.begin();
  timeClient.setTimeOffset(3600);

  // Initialize Fall sensor:
  Wire.begin();
  // Start I2C Transmission
  Wire.beginTransmission(Addr);
  // Select control register
  Wire.write(0x2A);
  // StandBy mode
  Wire.write((byte)0x00);
  // Stop I2C Transmission
  Wire.endTransmission();
 
  // Start I2C Transmission
  Wire.beginTransmission(Addr);
  // Select control register
  Wire.write(0x2A);
  // Active mode
  Wire.write(0x01);
  // Stop I2C Transmission
  Wire.endTransmission();
 
  // Start I2C Transmission
  Wire.beginTransmission(Addr);
  // Select control register
  Wire.write(0x0E);
  // Set range to +/- 2g
  Wire.write((byte)0x00);
  // Stop I2C Transmission
  Wire.endTransmission();
  delay(300);
}

void loop() 
{
  /*if(detectFall())
  {
    handleClientCommunication();
    writeFallDataToFirebase();
  }

  delay(1000);*/
  
  if(detectPulseAnomaly()){
    //handleClientCommunication();
    //writePulseAnomalyToDatabase();
  }
}

void handleClientCommunication(){
  // WiFi.scanNetworks will return the number of networks found
    n = WiFi.scanNetworks();
    if (n == 0)
    {
      Serial.println("no networks found");
    }
  
    buildJasonAndMakeApiCallForGeolocation();
    readAndParseServerReplay();
    extractCurrentDate();
    client.stop();
}

bool detectPulseAnomaly() {
  while(millis() < startTime + 5000){
    mes = analogRead(sensorPin);
    if(mes > maxValue){
      maxValue = mes;
    }
    if(mes < minValue){
      minValue = mes;
    }
    delay(10);
  }

  Serial.print("max value");
  Serial.println(maxValue);
  maxValue = (maxValue + minValue)/2;
  Serial.print("min value");
  Serial.println(minValue);


  startTime = millis();
  pulseCount = 0;
  firstRead = analogRead(sensorPin);
  while(millis() < startTime + 10000){
    Serial.print("first read: ");
    Serial.println(firstRead);
    delay(160);
    secondRead = analogRead(sensorPin);
    Serial.print("second read: ");
    Serial.println(secondRead);
    if(secondRead > maxValue && firstRead < maxValue) {
      pulseCount++;
      Serial.println(pulseCount);
    }
    firstRead = secondRead;
  }

  pulse = pulseCount * 6;
  Serial.println(pulse);
  //writePulseToDatabse();

  if(pulse < 60 || pulse > 100) {
    isPulseAnomaly = true;
  }
  
  return isPulseAnomaly;
}

void writePulseToDatabse(){
  Firebase.setInt("100/current_pulse/", pulse);
  // handle error 
  if (Firebase.failed()) { 
      Serial.print("pushing /logs failed:"); 
      Serial.println(Firebase.error());   
      return; 
   } 
  delay(1000);
}

bool detectFall(){
  getMeasurements();
  currentAcc = sqrt(pow(xyz_g[0], 2) + pow(xyz_g[1], 2) + pow(xyz_g[2], 2));
  gForce = currentAcc / 9.82;
  Serial.println("1");
  Serial.println(gForce);

  // gForce is below 100, suspecting a fall
  if (gForce < firstThreshold && fallCount == 0) {
    fallCount++;
    currentAcc = sqrt(pow(xyz_g[0], 2) + pow(xyz_g[1], 2) + pow(xyz_g[2], 2));
    gForce = currentAcc / 9.82;
    Serial.println("2");
    Serial.println(gForce);
    
    // the next value of gForce is below 70, keep testing if it's a pattern of a fall
    if (gForce <= shockThreshold) 
    {
      getMeasurements();
      currentAcc = sqrt(pow(xyz_g[0], 2) + pow(xyz_g[1], 2) + pow(xyz_g[2], 2));
      gForce = currentAcc / 9.82;
      Serial.println("3");
      Serial.println(gForce);
      maxFallValue = gForce;
      timeAtFall = millis();
      currentTime = millis();

      while (currentTime - timeAtFall < 1000 ) 
      {
        getMeasurements();
        currentAcc = sqrt(pow(xyz_g[0], 2) + pow(xyz_g[1], 2) + pow(xyz_g[2], 2));
        gForce = currentAcc / 9.82;
        Serial.println("4");
        Serial.println(gForce);
        
        if (maxFallValue < gForce) 
        {
          maxFallValue = gForce;
        }
        currentTime = millis();
        delay(delayTime);
      }
      
      if (maxFallValue < secondThreshold && maxFallValue < thiredThreshold) 
      {
        Serial.println("fall detected!");
        isFallDetected = true;
      }
    }
    // Detected continuous movment and not a fall
    else
    {
      isFallDetected = false;
      fallCount = 0;
    }
  }
  else
  {
    isFallDetected = false;
  }
  
  fallCount = 0;

  return isFallDetected;
}

void getMeasurements(){
  unsigned int data[7];
 
  // Request 7 bytes of data
  Wire.requestFrom(Addr, 7);
 
  // Read 7 bytes of data
  // staus, xAccl lsb, xAccl msb, yAccl lsb, yAccl msb, zAccl lsb, zAccl msb
  if(Wire.available() == 7) 
  {
    data[0] = Wire.read();
    data[1] = Wire.read();
    data[2] = Wire.read();
    data[3] = Wire.read();
    data[4] = Wire.read();
    data[5] = Wire.read();
    data[6] = Wire.read();
  }
 
  // Convert the data to 12-bits
  int xAccl = ((data[1] * 256) + data[2]) / 16;
  if (xAccl > 2047)
  {
    xAccl -= 4096;
  }
 
  int yAccl = ((data[3] * 256) + data[4]) / 16;
  if (yAccl > 2047)
  {
    yAccl -= 4096;
  }
 
  int zAccl = ((data[5] * 256) + data[6]) / 16;
  if (zAccl > 2047)
  {
    zAccl -= 4096;
  }

  xyz_g[0] = xAccl;
  xyz_g[1] = yAccl;
  xyz_g[2] = zAccl;
}

void buildJasonAndMakeApiCallForGeolocation()
{
  // build the jsonString...
  jsonString  = "{\n";
  jsonString += "\"homeMobileCountryCode\": 234,\n"; // this is a real UK MCC
  jsonString += "\"homeMobileNetworkCode\": 27,\n";  // and a real UK MNC
  jsonString += "\"radioType\": \"gsm\",\n";         // for gsm
  jsonString += "\"carrier\": \"Vodafone\",\n";      // associated with Vodafone
  jsonString += "\"wifiAccessPoints\": [\n";
    
  for (int j = 0; j < n; ++j)
  {
    jsonString += "{\n";
    jsonString += "\"macAddress\" : \"";
    jsonString += (WiFi.BSSIDstr(j));
    jsonString += "\",\n";
    jsonString += "\"signalStrength\": ";
    jsonString += WiFi.RSSI(j);
    jsonString += "\n";
      
    if (j < n - 1)
    {
      jsonString += "},\n";
    }
    else
    {
      jsonString += "}\n";
    }
  }
    
  jsonString += ("]\n");
  jsonString += ("}\n");
  
  //Connect to the client and make the api call
  client.setInsecure();
    
  if (client.connect(Host, 443)) 
  {
    client.println("POST " + thisPage + key + " HTTP/1.1");
    client.println("Host: " + (String)Host);
    client.println("Connection: close");
    client.println("Content-Type: application/json");
    client.println("User-Agent: Arduino/1.0");
    client.print("Content-Length: ");
    client.println(jsonString.length());
    client.println();
    client.print(jsonString);
    delay(500);
  }
}

void readAndParseServerReplay()
{
  //Read and parse all the lines of the reply from server
  while (client.available()) 
  {
    String line = client.readStringUntil('\r');
    JsonObject& root = jsonBuffer.parseObject(line);
    
    if (root.success()) 
    {
      latitude   = root["location"]["lat"];
      longitude  = root["location"]["lng"];
    }
  }
}

void extractCurrentDate()
{
  while(!timeClient.update()) 
  {
    timeClient.forceUpdate();
  }
    
  // The formattedDate comes with the following format:
  // 2018-05-28T16:00:13Z
  // We need to extract date and time
  formattedDate = timeClient.getFormattedDate();
  
  // Extract date
  int splitT = formattedDate.indexOf("T");
  dayStamp = formattedDate.substring(0, splitT);

  // Extract time
  timeStamp = formattedDate.substring(splitT+1, formattedDate.length()-4);
  delay(1000);
}

void writeFallDataToFirebase(){
  Firebase.setFloat("100/falls/" + dayStamp + " " + timeStamp + "/lat", latitude);
  Firebase.setFloat("100/falls/" + dayStamp + " " + timeStamp + "/long", longitude);
   
  // handle error 
  if (Firebase.failed()) { 
      Serial.print("pushing /logs failed:"); 
      Serial.println(Firebase.error());   
      return; 
   } 
  delay(1000); 
}

/*void writePulseAnomalyToDatabase(){
  Firebase.setFloat("100/pulse_history/" + dayStamp + " " + timeStamp + "/heart_rate", heartRate);
  
  // handle error 
  if (Firebase.failed()) { 
      Serial.print("pushing /logs failed:"); 
      Serial.println(Firebase.error());   
      return; 
   } 
  delay(1000); 
}*/
