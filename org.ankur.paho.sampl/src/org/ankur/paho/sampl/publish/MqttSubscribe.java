package org.ankur.paho.sampl.publish;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttSubscribe implements MqttCallback {

    static String topic        = "TRUCKER";
    String content      = "Message from MqttPublishSample" + System.currentTimeMillis();
    static int qos             = 2;
    static String broker       = "tcp://localhost:1883";
    static String clientId     = "JavaSample";
    static MemoryPersistence persistence = new MemoryPersistence();
	
	public static void main(String[] args) throws MqttException {
		MqttClient client = new MqttClient(broker, clientId, persistence);
		MqttSubscribe sub = new MqttSubscribe();
		client.setCallback(sub);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        System.out.println("Connecting to broker: "+broker);
        client.connect(connOpts);
        System.out.println("Connected");
        
		client.setCallback(sub);
		client.subscribe(new String[] {"ActiveMQ.Advisory.Producer.Topic.temp", "temp", topic});

		
		while(true) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				//
			}
		}
	}


	@Override
	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		System.out.println("Topic = " +topic);
		String m = message.toString();
		System.out.println("Message:" + m);
		
		
	}
}
