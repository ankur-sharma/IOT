package org.ankur.paho.sampl.publish;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttPublish {

	private static MqttClient client;

	public static MqttClient getClient() {
		return client;
	}

    public void publish()	 {

        String topic        = "MQTTSample";
        int qos             = 2;

        try {
        	MqttClient client = new MqttClient("tcp://localhost:1883", "Pub");
        	
            String content = String.valueOf(12); //LightSensor.readLDR());
            
			System.out.println("Publishing message: "+content);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            
            client.publish(topic, message);
            System.out.println("Message published");
            
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }
}