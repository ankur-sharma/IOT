import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttSubscribeSample implements MqttCallback {

	public static void main(String[] args) throws InterruptedException {
		MqttSubscribeSample demo = new MqttSubscribeSample();
		demo.demo();
		while(true) {
			Thread.sleep(1000);
		}
	}
    public void demo() throws InterruptedException {

        String topic        = "MQTTSample";
        String content      = "Message from MqttPublishSample" + System.currentTimeMillis();
        int qos             = 2;
        String broker       = "tcp://192.168.0.102:1883";
        String clientId     = "TRUCKER";
        MemoryPersistence persistence = new MemoryPersistence();

        try {
        	MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
        	sampleClient.setCallback(this);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: "+broker);
            sampleClient.connect(connOpts);
            System.out.println("Connected");
            
            System.out.println("Subscribing");
            sampleClient.subscribe(topic, qos);
            
            System.out.println("Publishing message: "+content);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            sampleClient.subscribe(topic);
//            sampleClient.publish(topic, message);
//            System.out.println("Message published");
//            sampleClient.disconnect();
//            System.out.println("Disconnected");
//            System.exit(0);
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }

	@Override
	public void connectionLost(Throwable paramThrowable) {
		System.out.println("Conneciton Lost" + paramThrowable.getLocalizedMessage());
		
	}

	@Override
	public void messageArrived(String paramString, MqttMessage paramMqttMessage)
			throws Exception {
		System.out.println("Message arrvied");
		System.out.println(paramString);
		System.out.println(paramMqttMessage);
		
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken paramIMqttDeliveryToken) {
		System.out.println("delivery complete");
		
	}
}