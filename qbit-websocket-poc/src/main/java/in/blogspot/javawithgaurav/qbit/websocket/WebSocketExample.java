package in.blogspot.javawithgaurav.qbit.websocket;

import java.util.concurrent.TimeUnit;

import io.advantageous.boon.core.Sys;
import io.advantageous.qbit.annotation.PathVariable;
import io.advantageous.qbit.annotation.RequestMapping;
import io.advantageous.qbit.client.Client;
import io.advantageous.qbit.client.ClientBuilder;
import io.advantageous.qbit.reactive.Callback;
import io.advantageous.qbit.server.EndpointServerBuilder;
import io.advantageous.qbit.server.ServiceEndpointServer;
import io.advantageous.qbit.service.ServiceQueue;
import io.advantageous.qbit.service.health.HealthServiceAsync;
import io.advantageous.qbit.service.health.HealthServiceBuilder;
import io.advantageous.qbit.system.QBitSystemManager;

public class WebSocketExample {
	public static void main(String[] args) {
		final String host = "localhost";
		final int port = 8089;

		QBitSystemManager qBitSystemManager = new QBitSystemManager();

		HealthServiceBuilder hsb = HealthServiceBuilder.healthServiceBuilder().setRecheckInterval(30)
				.setTimeUnit(TimeUnit.SECONDS);
		ServiceQueue serviceQueue = hsb.getServiceBuilder().setServiceObject(hsb.getImplementation())
				.setSystemManager(qBitSystemManager).build();
		HealthServiceAsync healthServiceAsync = hsb.setServiceQueue(serviceQueue).build();

		// Service Build and Start
		final ServiceEndpointServer server = EndpointServerBuilder.endpointServerBuilder()
				.setSystemManager(qBitSystemManager).setPort(port).setHost(host).setHealthService(healthServiceAsync)
				.build();
		server.initServices(new AdderService());
		server.start();

		// Create Qbit Client for Websocket
		Client client = ClientBuilder.clientBuilder().setHost(host).setPort(port).setTimeoutSeconds(15)
				.setKeepAlive(true).setAutoFlush(true).setFlushInterval(5).setProtocolBatchSize(1).build();
		//Create a proxy to service
		final AdderServiceClientInterface adderService = client.createProxy(AdderServiceClientInterface.class, "adder-service");
		client.start();
		
		adderService.add((r) -> System.out.println("Result from adding => " + r), 100, 200);
		adderService.clientProxyFlush();
		Sys.sleep(8000l);
		
		adderService.add((r) -> System.out.println("Result from adding => " + r), 101, 201);
		adderService.clientProxyFlush();
		
		adderService.add((r) -> System.out.println("Result from adding => " + r), 102, 202);
		adderService.clientProxyFlush();
		
		adderService.add((r) -> System.out.println("Result from adding => " + r), 103, 203);
		adderService.clientProxyFlush();
		
		adderService.add((r) -> System.out.println("Result from adding => " + r), 1000, 2000);
		adderService.clientProxyFlush();
		
		adderService.add((r) -> System.out.println("Result from adding => " + r), 1011, 2011);
		adderService.clientProxyFlush();
		client.flush();
		
		Sys.sleep(5000);
		client.stop();
		server.stop();
		qBitSystemManager.shutDown();
	}
	
	interface AdderServiceClientInterface {
		void add(Callback<Integer> callback, int a, int b);
		void clientProxyFlush();
	}

	@RequestMapping("/adder-service")
	static class AdderService {
		@RequestMapping("/add/{0}/{1}")
		public int add(@PathVariable int a, @PathVariable int b) {
			return a + b;
		}
	}
}
