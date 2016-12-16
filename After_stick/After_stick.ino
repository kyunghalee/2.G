
long previousMillis = 0;
const long INTERVAL_SEC = 1000;

int val = 0;
int teram = 0;



// 초음파 센서 핀 설정
const int sonicTriggerPin1 = 27;
const int sonicEchoPin1 = 26;
const int sonicTriggerPin2 = 25;
const int sonicEchoPin2 = 24;
const int sonicTriggerPin3 = 23;
const int sonicEchoPin3 = 22;
const int sonicTriggerPin4 = 21;
const int sonicEchoPin4 = 20;
const int sonicTriggerPin5 = 15;
const int sonicEchoPin5 = 14;
const int sonicTriggerPin6 = 17;
const int sonicEchoPin6 = 16;


// tilt 센서
   int tilt = 3;  // Connect Tilt sensor to Pin3
   //led
   int led = 28;

  //스위치  
   int sw = 29;

// 진동 센서 핀 설정
const int vibePin1 = 53;
const int vibePin2 = 52;
const int vibePin3 = 51;
const int vibePin4 = 50;
const int vibePin5 = 49;
const int vibePin6 = 48;

// 초음파 허용거리 기본값
long inLimitDistance = 50;
long outLimitDistance = 100;


// 측정거리
long cm = 0;

String data = "";

void setup()
{
  Serial.begin(9600);
  Serial1.begin(9600);



  // 진동 핀 모드 설정
  pinMode(vibePin1, OUTPUT );
  pinMode(vibePin2, OUTPUT );
  pinMode(vibePin3, OUTPUT );
  pinMode(vibePin4, OUTPUT );
  pinMode(vibePin5, OUTPUT );
  pinMode(vibePin6, OUTPUT );
  
  // 초음파 핀모드
  pinMode(sonicTriggerPin1, OUTPUT); // 센서 Trig 핀
  pinMode(sonicEchoPin1, INPUT); // 센서 Echo 핀
  pinMode(sonicTriggerPin2, OUTPUT); // 센서 Trig 핀
  pinMode(sonicEchoPin2, INPUT); // 센서 Echo 핀
  pinMode(sonicTriggerPin3, OUTPUT); // 센서 Trig 핀
  pinMode(sonicEchoPin3, INPUT); // 센서 Echo 핀
  pinMode(sonicTriggerPin4, OUTPUT); // 센서 Trig 핀
  pinMode(sonicEchoPin4, INPUT); // 센서 Echo 핀
  pinMode(sonicTriggerPin5, OUTPUT); // 센서 Trig 핀
  pinMode(sonicEchoPin5, INPUT); // 센서 Echo 핀
  pinMode(sonicTriggerPin6, OUTPUT); // 센서 Trig 핀
  pinMode(sonicEchoPin6, INPUT); // 센서 Echo 핀

  // tilt 센서
  pinMode(tilt, INPUT);       // Set digital pin 3 to input mode
  pinMode(led,OUTPUT); //led 센서
  pinMode(sw,INPUT); //버튼스위치센서
}

void loop()
{

  //  if ( Serial1.available() )   {  Serial.write( Serial1.read() );  }
  //
  //    // listen for user input and send it to the HC-05
  //   if ( Serial.available() )   {  Serial1.write( Serial.read() );  }
  //

  // 블루투스 통신 수신값
  while (Serial1.available()) {
    data += String((char)Serial1.read());
    delay(1);
  }

  // 거리 설정
  if (data.indexOf("in:") > -1) {
    data = data.substring(3);
    inLimitDistance = (long)data.toInt();
    Serial.print("inLimitDistance : ");
    Serial.println(inLimitDistance);
    data = "";
  }

  // 거리 설정
  if (data.indexOf("out:") > -1) {
    data = data.substring(4);
    outLimitDistance = (long)data.toInt();
    Serial.print("outLimitDistance : ");
    Serial.println(outLimitDistance);
    data = "";
  }

  //sw 
 if (digitalRead(sw) == LOW) { //Read sensor value
             val++;
      for (val=0; val < 10; val ++ ){
    digitalWrite(led,HIGH);
    delay(1000);
    digitalWrite(led, LOW);
    delay(1000);
    }
     Serial.println("sw");
    Serial1.println("sw;");
    delay(1000);
  } else {
    //Serial.println("no tilt");
    digitalWrite(led,LOW);
   
  }
 

  if (digitalRead(tilt) == HIGH) { //Read sensor value
    Serial.println("tilt");
    Serial1.println("tilt;");
    delay(1000);
  } else {
    //Serial.println("no tilt");
  }


  unsigned long currentMillis = millis();
  if (currentMillis - previousMillis > INTERVAL_SEC) {
    resetVibe();
    cm = calcurateDistance(sonicTriggerPin1, sonicEchoPin1);
    if (cm > 10 && cm < inLimitDistance) {
      Serial.print("pin 1 :");
      Serial.println(cm);
      digitalWrite(vibePin1, HIGH);
    }
    delay(50); // 0.3초 대기 후 다시 측정
    cm = calcurateDistance(sonicTriggerPin2, sonicEchoPin2);
    if (cm > 10 && cm < inLimitDistance) {
      Serial.print("pin 2 :");
      Serial.println(cm);
      digitalWrite(vibePin2, HIGH);

      
    }
    delay(50); // 0.3초 대기 후 다시 측정
    cm = calcurateDistance(sonicTriggerPin3, sonicEchoPin3);
    if (cm > 10 && cm < inLimitDistance) {
      Serial.print("pin 3 :");
      Serial.println(cm);
      digitalWrite(vibePin3, HIGH);
    }
    delay(50); // 0.3초 대기 후 다시 측정
    cm = calcurateDistance(sonicTriggerPin4, sonicEchoPin4);
    if (cm > 10 && cm < outLimitDistance) {
      Serial.print("pin 4 :");
      Serial.println(cm);
      digitalWrite(vibePin4, HIGH);
    }
    delay(50); // 0.3초 대기 후 다시 측정
    cm = calcurateDistance(sonicTriggerPin5, sonicEchoPin5);
    if (cm > 10 && cm < outLimitDistance) {
      Serial.print("pin 5 :");
      Serial.println(cm);
      digitalWrite(vibePin5, HIGH);
    }

    delay(50); // 0.3초 대기 후 다시 측정
    cm = calcurateDistance(sonicTriggerPin6, sonicEchoPin6);
    if (cm > 10 && cm < outLimitDistance) {
      Serial.print("pin 6 :");
      Serial.println(cm);
      digitalWrite(vibePin6, HIGH);
    }


    //delay(1500); // 0.3초 대기 후 다시 측정delay(500); // 0.3초 대기 후 다시 측정

   // delay(500); // 0.3초 대기 후 다시 측정
  }



}

/**
 * 초음파 거리계산
 */
long calcurateDistance(int trigger, int echo) {
  long duration;
  digitalWrite(trigger, HIGH); // 센서에 Trig 신호 입력
  delayMicroseconds(10); // 10us 정도 유지
  digitalWrite(trigger, LOW); // Trig 신호 off

  duration = pulseIn(echo, HIGH); // Echo pin: HIGH->Low 간격을 측정
  return microsecondsToCentimeters(duration); // 거리(cm)로 변환
}


/**
 *  진동 모듈 리셋
 */
void resetVibe() {
  digitalWrite(vibePin1, LOW);
  digitalWrite(vibePin2, LOW);
  digitalWrite(vibePin3, LOW);
  digitalWrite(vibePin4, LOW);
  digitalWrite(vibePin5, LOW);
  digitalWrite(vibePin6, LOW);
}




long microsecondsToInches(long microseconds)
{
  // According to Parallax's datasheet for the PING))), there are
  // 73.746 microseconds per inch (i.e. sound travels at 1130 feet per
  // second).  This gives the distance travelled by the ping, outbound
  // and return, so we divide by 2 to get the distance of the obstacle.
  // See: http://www.parallax.com/dl/docs/prod/acc/28015-PING-v1.3.pdf
  return microseconds / 74 / 2;
}



long microsecondsToCentimeters(long microseconds)
{
  // The speed of sound is 340 m/s or 29 microseconds per centimeter.
  // The ping travels out and back, so to find the distance of the
  // object we take half of the distance travelled.
  return microseconds / 29 / 2;
}
