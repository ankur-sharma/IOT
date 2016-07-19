package org.ankur.kura.jmsdemo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.jms.JmsConfiguration;
import org.apache.camel.component.jms.JmsEndpoint;
import org.apache.camel.component.kura.KuraRouter;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.osgi.framework.BundleContext;

public class Activator extends KuraRouter {

	private static final String JMS_CORRELATION_ID = "JMSCorrelationID";
	private static final String MQ_SERVER = "demo-local";
	private static final String BROKER_URL = "tcp://" + MQ_SERVER + ":61616";
	private static final String SKETCH_URL = "http://localhost:83";
	private static final String CLIENT_ID = "Edison";
	private static final String TRUCKER_ID = "truckerid";
	private static final String TRUCKER_ID_HEADER = "ID";
	private static final String TRUCKER_TOPIC = "TRUCKER";
	private static final String APPROVAL_TOPIC = "APPROVAL";
	private static final String CORRID_TOPIC = "CORRID";
	private static double rnd = Math.random();

	private static String CORRELATIONID;
	
	private JmsComponent jmsComponent;
	private ConnectionFactory connectionFactory;
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		super.start(bundleContext);
		write("Bundle Started at " + getNow());
		write(getContext());
		java.security.Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider()); //this dynamically specifies the security provider.
	}

	private String getNow() {
		long yourmilliseconds = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");    
		Date resultdate = new Date(yourmilliseconds);
		return sdf.format(resultdate);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
//		super.stop(bundleContext);
	}


	
	private void init() {
		write("init");
		connectionFactory = new ActiveMQConnectionFactory(BROKER_URL);
		if (connectionFactory != null) {
			write("Connecting " + BROKER_URL);
		}
		jmsComponent = JmsComponent.jmsComponent(connectionFactory);
		getContext().addComponent("mq", jmsComponent);
	}
	
	@Override
	public void configure() throws Exception {
		write("starting configure");
		if (jmsComponent == null) {
			init();
		}

		JmsConfiguration configuration = new JmsConfiguration();
		configuration.setListenerConnectionFactory(connectionFactory);
		configuration.setTemplateConnectionFactory(connectionFactory);
		
		from("mq:topic:"+CORRID_TOPIC).process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws Exception {
				write(exchange.getFromEndpoint().getEndpointKey());
				write(exchange.getFromEndpoint().getEndpointUri());
				write("Got CorrId");
				Message message = exchange.getIn();
				CORRELATIONID = (String) message.getHeader(JMS_CORRELATION_ID);
				write("CORRELATIONID = " + CORRELATIONID);
			}
		}).to("mock:result");
		
		final JmsEndpoint truckerEndpoint = new JmsEndpoint("mq:topic:" +TRUCKER_TOPIC, jmsComponent, TRUCKER_TOPIC, true, configuration);
		truckerEndpoint.setCamelContext(getContext());
		from("timer:ticktock?delay=15000&period=5000").process(new Processor() {

			@Override
			public void process(Exchange exchange) throws Exception {
				String truckInfo = "en1234567"; getNFC();
				write("truckInfo is " + truckInfo);
				exchange.getIn().setHeader(TRUCKER_ID, truckInfo);
				write("Setting CORRELATIONID = " + CORRELATIONID);
				exchange.getIn().setHeader(JMS_CORRELATION_ID, CORRELATIONID);
			}

		}).filter().method(new MessageFilter(), "filter").to(truckerEndpoint);
		
		from("mq:topic:"+APPROVAL_TOPIC).process(new Processor() {
			
			@Override
			public void process(Exchange exchange) throws Exception {
				write(exchange.getFromEndpoint().getEndpointKey());
				write(exchange.getFromEndpoint().getEndpointUri());
				write("Got Approval");
				Message message = exchange.getIn();
				String truckerId = (String) message.getHeader(TRUCKER_ID);
				write("Approval received for " + truckerId);
				sendApproval(truckerId);
			}

		}).to("mock:result");

	}

	private void sendApproval(String truckerId) {
		try {
			HttpGet getRequest = new HttpGet(SKETCH_URL);
			getRequest.addHeader(TRUCKER_ID_HEADER, truckerId);
			CloseableHttpClient httpclient = HttpClients.createDefault();
			CloseableHttpResponse response = httpclient.execute(getRequest);
			write(response.getStatusLine());
			response.close();
		} catch (IOException e) {
			write(e.getMessage());
		}
		
	}
	
	public class MessageFilter {

		public MessageFilter() {
			System.out.println("Filter created");
		}

		public boolean filter(Exchange exchange) {
			String truckerid = (String) exchange.getIn().getHeader(TRUCKER_ID);
			write("TruckerId is " + truckerid);
			if (truckerid != null && truckerid.length() > 0) {
				System.out.println("Filter object " + truckerid.length());
				write("message OK");
				return true;
			}
			write("message BAD");
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
			write(e.getMessage());
		}
		return "";
	}
	
	protected void write(Object obj) {
		String string = "[org.ankur.camel.jmsdemo ] " + rnd + " : " + obj;
		System.out.println(string);
	}
}
