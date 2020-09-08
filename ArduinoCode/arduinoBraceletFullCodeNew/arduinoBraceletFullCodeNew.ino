#include <ESP8266HTTPClient.h>
#include <ArduinoJson.h>
#include <FirebaseArduino.h>
#include "ESP8266WiFi.h"
#include <NTPClient.h>
#include <WiFiUdp.h>
#include <Wire.h>
#include <MPU6050.h>

void ICACHE_RAM_ATTR detectFall();
MPU6050 mpu;

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
float         minValue        =     10000;
float         firstRead;
float         secondRead;
int           startTime       =     millis();
int           pulseCount      =     1;
int           pulse;
int           methodCounter   =     0;
int           fallCounter     =     0;
//================================ Pulse ===============================


//=========================== Fall Detection ===========================
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
  Serial.print("Connecting to ");
  Serial.println(ssid);
  WiFi.begin(ssid, pass);
  while (WiFi.status() != WL_CONNECTED) 
  {
    delay(500);
    Serial.print(".");
  }

  // Connect to Firebase:
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH); 

  // Connect to Time client:
  timeClient.begin();
  timeClient.setTimeOffset(3600);

  // Initialize Fall Detector:
  Serial.println("Initialize MPU6050");

  while(!mpu.begin(MPU6050_SCALE_2000DPS, MPU6050_RANGE_16G))
  {
    Serial.println("Could not find a valid MPU6050 sensor, check wiring!");
    delay(500);
  }

  mpu.setAccelPowerOnDelay(MPU6050_DELAY_3MS);
  mpu.setIntFreeFallEnabled(true);
  mpu.setIntZeroMotionEnabled(false);
  mpu.setIntMotionEnabled(false);
  mpu.setDHPFMode(MPU6050_DHPF_5HZ);
  mpu.setFreeFallDetectionThreshold(17);
  mpu.setFreeFallDetectionDuration(1);  

  attachInterrupt(12, detectFall, RISING);
}

void loop() 
{
  if(isFallDetected)
  {
    isFallDetected = false;
    fallCounter = 0;
    handleClientCommunication();
    writeFallDataToFirebase();
  }
  
  if(detectPulseAnomaly()){
    handleClientCommunication();
    writePulseAnomalyToDatabase();
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
  isPulseAnomaly = false;
  methodCounter++;
  maxValue = 0;
  startTime = millis();
  
  while(millis() < startTime + 5000 && fallCounter == 0){
    mes = analogRead(sensorPin);
    if(mes > maxValue){
      maxValue = mes;
    }
    if(mes < minValue){
      minValue = mes;
    }
    delay(10);
  }

  maxValue = (maxValue + minValue)/2;
  pulseCount = 0;
  firstRead = analogRead(sensorPin);
  
  while(millis() < startTime + 15000 && fallCounter == 0){
    delay(160);
    secondRead = analogRead(sensorPin);
    if(secondRead > maxValue && firstRead < maxValue) {
      pulseCount++;
      Serial.println(pulseCount);
    }
    firstRead = secondRead;
  }

  if(fallCounter == 0){
    pulse = pulseCount * 6;
    Serial.print("pulse: ");
    Serial.println(pulse);
    writePulseToDatabse();
  
    if((pulse < 60 || pulse > 100) && pulse != 0) {
      isPulseAnomaly = true;
    }
  }
  
  return isPulseAnomaly;
}

void writePulseToDatabse(){
  Firebase.setInt("100/current_pulse", pulse);
  // handle error 
  if (Firebase.failed()) { 
      Serial.print("pushing /current_pulse failed:"); 
      Serial.println(Firebase.error());   
      return; 
   } 
  delay(1000);
}

void detectFall(){
  Vector rawAccel = mpu.readRawAccel();
  Activites act = mpu.readActivites();
  Serial.println();
  Serial.println("==========================================================");
  Serial.println("Detected Fall!");
  Serial.println("==========================================================");
  Serial.println();
  isFallDetected = true;
  fallCounter = -1;
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

void writePulseAnomalyToDatabase(){
  Firebase.setFloat("100/pulse_history/" + dayStamp + " " + timeStamp + "/heart_rate", pulse);
  
  // handle error 
  if (Firebase.failed()) { 
      Serial.print("pushing /logs failed:"); 
      Serial.println(Firebase.error());   
      return; 
  } 
  delay(1000); 
}
