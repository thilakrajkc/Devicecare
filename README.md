# Devicecare
Device care application code which has following functions and features

1.Serial Port ( to read and write the data fron the port)
2.Service which runs in background even if the application is closed or for boot complete
3.MQTT for sernding the serialport data to the Cloud/Broker

MQTT also includes,
Initial connection to the broker
Publish the msg to the broker on the connection succesfull
Subscribe to receive the msg from the Publisher for perticular topic
Auto receive the msg from broker
Unsubscribe from topic
Stop connection
