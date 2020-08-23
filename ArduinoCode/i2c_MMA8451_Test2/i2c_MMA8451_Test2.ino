#include <Wire.h>
#include "i2c.h"
#include "i2c_MMA8451.h"
MMA8451 mma8451;

unsigned long currentTime = 0, timeAtFall = 0;
float currentAcc = 0;
int fallCount = 0;
float maxFallValue = 0;
const float upperThreshold = 6.00;
const float lowerThreshold = 3.00;
const int delayTime = 50;


void setup()
{
    Serial.begin(115200);

    Serial.print("Probe MMA8451: ");
    if (mma8451.initialize()) {
      Serial.println("Sensor found!");
      mma8451.setSensibility(8);
    }
    else
    {
        Serial.println("Sensor missing");
        // TODO: think if the server would care if the sensor is missing 
        // (I think yes, would alert the server that the bracelet has a problem and needs fixing)
        while(1) {};
    }
}

void loop()
{   
    static float xyz_g[3];
    
    mma8451.getMeasurement(xyz_g);
    currentAcc = sqrt(pow(xyz_g[0], 2) + pow(xyz_g[1], 2) + pow(xyz_g[2], 2));

    // Suspected A fall happend (fall threshold: value >= 6)
    if (currentAcc >= upperThreshold && fallCount == 0) {
      Serial.println(currentAcc);
      fallCount++;
      timeAtFall = millis();
      currentTime = millis();

      // Check for 2 seconds if an aftershock occured (aftershock threshold: value <=3
      while (currentTime - timeAtFall < 2000 ) {
        mma8451.getMeasurement(xyz_g);
        currentAcc = sqrt(pow(xyz_g[0], 2) + pow(xyz_g[1], 2) + pow(xyz_g[2], 2));
        Serial.println(currentAcc);
        if (maxFallValue < currentAcc) {
          maxFallValue = currentAcc;
        }
        currentTime = millis();
        delay(delayTime);
      }

      // Take the maximun value of the testing above, if bigger then 3 suspected as an after shock
      if (maxFallValue >= lowerThreshold) {
        delay(50);
        mma8451.getMeasurement(xyz_g);
        currentAcc = sqrt(pow(xyz_g[0], 2) + pow(xyz_g[1], 2) + pow(xyz_g[2], 2));
        Serial.println(currentAcc);
        // Check if the currentAcc is below the aftershock threshold, if so a fall was detected.
        if (currentAcc < lowerThreshold) {
          Serial.println("Fall Detected!");
        }
      }
      // Detected continuous movment and not a fall
      else if (maxFallValue >= upperThreshold) {
        fallCount = 0;
      }
    }

    fallCount = 0;
}
