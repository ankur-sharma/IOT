package org.ankur.paho.sampl;

import org.ankur.paho.sampl.publish.LightSensor;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.kura.KuraRouter;
import org.apache.camel.component.paho.PahoComponent;
import org.eclipse.kura.message.KuraPayload;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.osgi.framework.BundleContext;

public class Activator extends KuraRouter {

	private static final String BROKER_URL = "tcp://192.168.0.103:1883";
	private static final String CLIENT_ID = "JavaSample";
	private static final String TOPIC = "LDR";
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
				String message = String.valueOf(LightSensor.readLDR());
				write("sending message:" + message);
				exchange.getIn().setBody(message.getBytes(), byte[].class);
			}

		}).to(pahoComponent.createEndpoint("paho:"+TOPIC));
	}

	protected void write(Object obj) {
		String string = "ankur1: " + obj;
		log.debug(string);
		System.out.println(string);
	}

}
