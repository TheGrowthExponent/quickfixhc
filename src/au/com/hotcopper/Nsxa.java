/*******************************************************************************
 * 
 ******************************************************************************/

package au.com.hotcopper;

import static quickfix.Initiator.SETTING_RECONNECT_INTERVAL;
import static quickfix.Initiator.SETTING_SOCKET_CONNECT_HOST;
import static quickfix.Initiator.SETTING_SOCKET_CONNECT_PORT;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.JMException;
import javax.management.ObjectName;

import org.quickfixj.jmx.JmxExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.ConfigError;
import quickfix.DefaultMessageFactory;
import quickfix.FieldConvertError;
import quickfix.FileStoreFactory;
import quickfix.LogFactory;
import quickfix.MessageFactory;
import quickfix.MessageStoreFactory;
import quickfix.RuntimeError;
import quickfix.ScreenLogFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.SocketInitiator;
import quickfix.mina.initiator.AbstractSocketInitiator;
import quickfix.mina.initiator.IoSessionInitiator;

public class Nsxa {
    private final static Logger log = LoggerFactory.getLogger(Nsxa.class);
    private final SocketInitiator initiator;
    
    private final JmxExporter jmxExporter;
    private final ObjectName connectorObjectName;
    
    public Nsxa(SessionSettings settings) throws ConfigError, FieldConvertError, JMException {
        Application application = new Application(settings);
        MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new ScreenLogFactory(true, true, true);
        MessageFactory messageFactory = new DefaultMessageFactory();

        initiator = new SocketInitiator(application, messageStoreFactory, settings, logFactory,
                messageFactory);

        //InetSocketAddress address = getInitiatorSocketAddress(settings, sessionID);

        //configureDynamicSessions(settings, application, messageStoreFactory, logFactory, messageFactory);

        jmxExporter = new JmxExporter();
        connectorObjectName = jmxExporter.register(initiator);
        log.info("Acceptor registered with JMX, name=" + connectorObjectName);
    }

    /*
    private void configureDynamicSessions(SessionSettings settings, Application application,
            MessageStoreFactory messageStoreFactory, LogFactory logFactory,
            MessageFactory messageFactory) throws ConfigError, FieldConvertError {
        //
        // If a session template is detected in the settings, then
        // set up a dynamic session provider.
        //

        Iterator<SessionID> sectionIterator = settings.sectionIterator();
        while (sectionIterator.hasNext()) {
            SessionID sessionID = sectionIterator.next();
            InetSocketAddress address = getInitiatorSocketAddress(settings, sessionID);
        }
    }
    */

    /*
    private InetSocketAddress getInitiatorSocketAddress(SessionSettings settings, SessionID sessionID)
            throws ConfigError, FieldConvertError {
        String initiatorHost = "0.0.0.0";
        if (settings.isSetting(sessionID, SETTING_SOCKET_CONNECT_HOST)) {
        	initiatorHost = settings.getString(sessionID, SETTING_SOCKET_CONNECT_HOST);
        }
        int initiatorPort = (int) settings.getLong(sessionID, SETTING_SOCKET_CONNECT_PORT);

        InetSocketAddress address = new InetSocketAddress(initiatorHost, initiatorPort);
        return address;
    }
    */
    
    private void start() throws RuntimeError, ConfigError {
    	initiator.start();
    }

    private void stop() {
        try {
            jmxExporter.getMBeanServer().unregisterMBean(connectorObjectName);
        } catch (Exception e) {
            log.error("Failed to unregister acceptor from JMX", e);
        }
        initiator.stop();
    }

    public static void main(String args[]) throws Exception {
        try {
            InputStream inputStream = getSettingsInputStream(args);
            SessionSettings settings = new SessionSettings(inputStream);
            inputStream.close();

            Nsxa nsxa = new Nsxa(settings);
            nsxa.start();

            System.out.println("press <enter> to quit");
            System.in.read();

            nsxa.stop();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private static InputStream getSettingsInputStream(String[] args) throws FileNotFoundException {
        InputStream inputStream = null;
        if (args.length == 0) {
            inputStream = Nsxa.class.getResourceAsStream("config.cfg");
        } else if (args.length == 1) {
            inputStream = new FileInputStream(args[0]);
        }
        if (inputStream == null) {
            System.out.println("usage: " + Nsxa.class.getName() + " [configFile].");
            System.exit(1);
        }
        return inputStream;
    }
}
