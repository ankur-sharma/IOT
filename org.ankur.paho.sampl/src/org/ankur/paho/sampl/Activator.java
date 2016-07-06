package org.ankur.paho.sampl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.kura.KuraRouter;
import org.apache.camel.component.paho.PahoComponent;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.osgi.framework.BundleContext;

public class Activator extends KuraRouter {

	private static final String MQTT_SERVER = "192.168.0.102";
	private static final String BROKER_URL = "tcp://" + MQTT_SERVER + ":1883";
	private static final String CLIENT_ID = "Edison";
	private static final String TOPIC = "TRUCKER";
	private PahoComponent pahoComponent;

	@Override
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
	}

	@Override
	public void stop(BundleContext bundleContext) throws Exception {
		write("Disconnected");
		super.stop(bundleContext);
	}

	@Override
	protected CamelContext createCamelContext() {
		setContext(super.createCamelContext());
		pahoComponent = new PahoComponent();
		pahoComponent.setBrokerUrl(BROKER_URL);
		pahoComponent.setClientId(CLIENT_ID);
		pahoComponent.setConnectOptions(new MqttConnectOptions());
		pahoComponent.setCamelContext(getContext());
		return getContext();
	}
	
	@Override
	public void configure() throws Exception {
		super.configure();
		write("creating context and sending message");
		from("timer:ticktock?delay=5000&period=5000").process(new Processor() {

			@Override
			public void process(Exchange exchange) throws Exception {
//				String message = String.valueOf(LightSensor.readLDR());
				String message = getNFC();
				write("sending message:" + message);
				exchange.getIn().setBody(message.getBytes(), byte[].class);
			}

		}).filter().method(new MessageFilter(), "filter").to(pahoComponent.createEndpoint("paho:"+TOPIC));
	}
	
	public class MessageFilter {
		
		public MessageFilter() {
			System.out.println("Filter created");
		}
		
		public boolean filter(Exchange exchange) {
			Object object = exchange.getIn().getBody();
			byte[] message = (byte[])(object);
			System.out.println("Filter object " + message.length );
			if (message.length > 0)
				return true;
			return false;
		}
	}
	
	private String getNFC() {
		try {
			URL url = new URL("http://localhost:83");
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			String data = "";
			String nfc = null;
			while (true) {
				data = br.readLine();
				if (data == null)
					break;
				write(data);
				if (data.length() > 0)
					nfc = data.trim();
			}
			return nfc;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	protected void write(Object obj) {
		String string = "[org.ankur.paho.sampl] " + obj;
		log.debug(string);
		System.out.println(string);
	}

}
