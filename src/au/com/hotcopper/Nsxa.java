/*******************************************************************************
 * 
 ******************************************************************************/

package au.com.hotcopper;

import static quickfix.Initiator.SETTING_RECONNECT_INTERVAL;
import static quickfix.Initiator.SETTING_SOCKET_CONNECT_HOST;
import static quickfix.Initiator.SETTING_SOCKET_CONNECT_PORT;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.management.JMException;
import javax.management.ObjectName;
import javax.swing.JFrame;
import javax.swing.UIManager;

import org.apache.mina.common.IoSession;
import org.quickfixj.jmx.JmxExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FieldConvertError;
import quickfix.FileStoreFactory;
import quickfix.Initiator;
import quickfix.LogFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.RuntimeError;
import quickfix.ScreenLogFactory;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;
import quickfix.examples.banzai.Banzai;
import quickfix.examples.banzai.BanzaiApplication;
import quickfix.examples.banzai.ExecutionTableModel;
import quickfix.examples.banzai.OrderTableModel;
import quickfix.examples.banzai.ui.BanzaiFrame;
import quickfix.field.SecurityListRequestType;
import quickfix.field.SecurityReqID;
import quickfix.mina.SessionConnector;
import quickfix.mina.initiator.AbstractSocketInitiator;
import quickfix.mina.initiator.IoSessionInitiator;

public class Nsxa {
	private static final CountDownLatch shutdownLatch = new CountDownLatch(1);

	/** enable logging for this class */
	private static Logger log = LoggerFactory.getLogger(Nsxa.class);
	private static Nsxa nsxa;
	private boolean initiatorStarted = false;
	private Initiator initiator = null;

	private JmxExporter jmxExporter;
	private ObjectName connectorObjectName;

	private Map<SessionID, Session> sessions = Collections.emptyMap();
    private final static ScheduledExecutorService scheduledExecutorService = Executors
            .newSingleThreadScheduledExecutor(new QFTimerThreadFactory());
	private Future<?> marketDataFuture;

	public Nsxa(String[] args) throws Exception {
		InputStream inputStream = getSettingsInputStream(args);

		SessionSettings settings = new SessionSettings(inputStream);
		inputStream.close();

		boolean logHeartbeats = Boolean.valueOf(
				System.getProperty("logHeartbeats", "true")).booleanValue();

		// OrderTableModel orderTableModel = new OrderTableModel();
		// ExecutionTableModel executionTableModel = new ExecutionTableModel();
		NsxaApplication application = new NsxaApplication();

		MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);
		LogFactory logFactory = new ScreenLogFactory(true, true, true,
				logHeartbeats);
		MessageFactory messageFactory = new DefaultMessageFactory();

		initiator = new SocketInitiator(application, messageStoreFactory,
				settings, logFactory, messageFactory);

		JmxExporter jmxExporter = new JmxExporter();
		connectorObjectName = jmxExporter.register(initiator);
		log.info("Initiator registered with JMX, name=" + connectorObjectName);
	}

	public synchronized void logon() {
		if (!initiatorStarted) {
			try {
				initiator.start();
				initiatorStarted = true;
			} catch (Exception e) {
				log.error("Logon failed", e);
			}
		} else {
			Iterator<SessionID> sessionIds = initiator.getSessions().iterator();
			while (sessionIds.hasNext()) {
				SessionID sessionId = (SessionID) sessionIds.next();
				Session.lookupSession(sessionId).logon();
			}
		}
	}

	public void logout() {
		Iterator<SessionID> sessionIds = initiator.getSessions().iterator();
		while (sessionIds.hasNext()) {
			SessionID sessionId = (SessionID) sessionIds.next();
			Session.lookupSession(sessionId).logout("user requested");
		}
	}

	public void start() throws RuntimeError, ConfigError {
		initiator.start();
		if (marketDataFuture == null) {
			marketDataFuture = scheduledExecutorService.scheduleWithFixedDelay(
					new MarketDataTask(), 0, 60, TimeUnit.SECONDS);
		}
	}

	public void stop(boolean force) {
		try {
			if (marketDataFuture != null) {
				marketDataFuture.cancel(true);
				marketDataFuture = null;
			}
			shutdownLatch.countDown();
			jmxExporter.getMBeanServer().unregisterMBean(connectorObjectName);
		} catch (Exception e) {
			log.error("Failed to unregister initiator from JMX", e);
		}
		// initiator.stop();
	}
	
	public void block() throws ConfigError, RuntimeError {
		// TODO Auto-generated method stub
		
	}

	public static Nsxa get() {
		return nsxa;
	}

	public static void main(String args[]) throws Exception {
		try {
			nsxa = new Nsxa(args);
			if (!System.getProperties().containsKey("openfix")) {
				nsxa.logon();
			}
			
			shutdownLatch.await();

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	private static InputStream getSettingsInputStream(String[] args)
			throws FileNotFoundException {
		InputStream inputStream = null;
		if (args.length == 0) {
			inputStream = Nsxa.class.getResourceAsStream("config.cfg");
		} else if (args.length == 1) {
			inputStream = new FileInputStream(args[0]);
		}
		if (inputStream == null) {
			System.out.println("usage: " + Nsxa.class.getName()
					+ " [configFile].");
			System.exit(1);
		}
		return inputStream;
	}

	protected void logError(SessionID sessionID, IoSession protocolSession,
			String message, Throwable t) {
		log.error(message + getLogSuffix(sessionID, protocolSession), t);
	}

	private String getLogSuffix(SessionID sessionID, IoSession protocolSession) {
		String suffix = ":";
		if (sessionID != null) {
			suffix += "sessionID=" + sessionID.toString() + ";";
		}
		if (protocolSession != null) {
			suffix += "address=" + protocolSession.getRemoteAddress();
		}
		return suffix;
	}

	private class MarketDataTask implements Runnable {
		public void run() {
			try {
				System.out.println("MarketDataTask");
				Iterator<quickfix.Session> sessionItr = sessions.values()
						.iterator();
				while (sessionItr.hasNext()) {
					quickfix.Session session = sessionItr.next();
					try {
						session.next();
					} catch (IOException e) {
						logError(session.getSessionID(), null,
								"Error in session timer processing", e);
						e.printStackTrace();
					}
				}
			} catch (Throwable e) {
				log.error("Error during timer processing", e);
				e.printStackTrace();
			}
		}
	}
	
    private static class QFTimerThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable, "QFJ Timer");
            thread.setDaemon(true);
            return thread;
        }

    }
}
