/*******************************************************************************
 * 
 ******************************************************************************/

package au.com.hotcopper;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Future;

import org.apache.mina.common.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.Application;
import quickfix.ConfigError;
import quickfix.DataDictionaryProvider;
import quickfix.DefaultMessageFactory;
import quickfix.DoNotSend;
import quickfix.FieldConvertError;
import quickfix.FieldNotFound;
import quickfix.FixVersions;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Initiator;
import quickfix.LogUtil;
import quickfix.Message;
import quickfix.MessageUtils;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.SessionSettings;
import quickfix.UnsupportedMessageType;
import quickfix.field.ApplVerID;
import quickfix.field.AvgPx;
import quickfix.field.BeginString;
import quickfix.field.BusinessRejectReason;
import quickfix.field.ClOrdID;
import quickfix.field.CumQty;
import quickfix.field.CxlType;
import quickfix.field.DeliverToCompID;
import quickfix.field.EncryptMethod;
import quickfix.field.ExecID;
import quickfix.field.ExecTransType;
import quickfix.field.ExecType;
import quickfix.field.HandlInst;
import quickfix.field.HeartBtInt;
import quickfix.field.LastPx;
import quickfix.field.LastQty;
import quickfix.field.LastShares;
import quickfix.field.LeavesQty;
import quickfix.field.LocateReqd;
import quickfix.field.MsgSeqNum;
import quickfix.field.MsgType;
import quickfix.field.NextExpectedMsgSeqNum;
import quickfix.field.OrdStatus;
import quickfix.field.OrdType;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.OrigClOrdID;
import quickfix.field.Password;
import quickfix.field.Price;
import quickfix.field.RefMsgType;
import quickfix.field.RefSeqNum;
import quickfix.field.ResetSeqNumFlag;
import quickfix.field.SecurityReqID;
import quickfix.field.SecurityListRequestType;
import quickfix.field.SenderCompID;
import quickfix.field.SessionRejectReason;
import quickfix.field.Side;
import quickfix.field.StopPx;
import quickfix.field.Symbol;
import quickfix.field.TargetCompID;
import quickfix.field.Text;
import quickfix.field.TimeInForce;
import quickfix.field.TransactTime;
import quickfix.field.UserRequestID;
import quickfix.field.UserRequestType;
import quickfix.field.UserStatus;
import quickfix.field.UserStatusText;
import quickfix.field.Username;
import quickfix.fix44.Logon;

/**
 * @author Jason Pascoe
 *
 */
public class NsxaApplication extends quickfix.MessageCracker implements
		quickfix.Application {

	private DefaultMessageFactory messageFactory = new DefaultMessageFactory();
	private ObservableLogon observableLogon = new ObservableLogon();
	private boolean isAvailable = true;
	private boolean isMissingField;
	private IdGenerator idGenerator = new IdGenerator();
	protected final Logger log = LoggerFactory.getLogger(getClass());

	public NsxaApplication() {
		System.out.println("Application");
	}

	public void onCreate(SessionID sessionID) {
		Session.lookupSession(sessionID).getLog();
		System.out.println("onCreate");
	}

	public void onLogon(SessionID sessionID) {
		System.out.println("onLogon");
		observableLogon.logon(sessionID);
		try {
			sendUserRequest(sessionID);
		} catch (SessionNotFound e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void onLogout(SessionID sessionID) {
		System.out.println("onLogout");
		observableLogon.logoff(sessionID);
	}

	public void toAdmin(quickfix.Message message, SessionID sessionID) {
		System.out.println("toAdmin");
		MessageProcessor messageProcessor = new MessageProcessor(message,
				sessionID);
		messageProcessor.process();
	}

	public void toApp(quickfix.Message message, SessionID sessionID)
			throws DoNotSend {
		System.out.println("toApp");
		MessageProcessor messageProcessor = new MessageProcessor(message,
				sessionID);
		messageProcessor.process();
	}

	public void fromAdmin(quickfix.Message message, SessionID sessionID)
			throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue,
			RejectLogon {
		System.out.println("fromAdmin");
		MessageProcessor messageProcessor = new MessageProcessor(message,
				sessionID);
		messageProcessor.run();
	}

	public void fromApp(quickfix.Message message, SessionID sessionID)
			throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue,
			UnsupportedMessageType {
		System.out.println("fromApp");
		crack(message, sessionID);
		// MessageProcessor messageProcessor = new MessageProcessor(message,
		// sessionID);
		// messageProcessor.run();
	}

	public class MessageProcessor implements Runnable {
		private quickfix.Message message;
		private SessionID sessionID;

		public MessageProcessor(quickfix.Message message, SessionID sessionID) {
			this.message = message;
			this.sessionID = sessionID;
		}

		public void run() {
			try {
				MsgType msgType = new MsgType();
				if (isAvailable) {
					if (message.getHeader().isSetField(DeliverToCompID.FIELD)) {
						// Send Message
						// sendSessionReject(message,
						// SessionRejectReason.COMPID_PROBLEM);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void process() {
			try {
				MsgType msgType = new MsgType();
				if (isAvailable) {
					if (message.getHeader().isSetField(DeliverToCompID.FIELD)) {
						// Send Message
						sendSessionReject(message,
								SessionRejectReason.COMPID_PROBLEM);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	protected void onMessage(quickfix.Message message, SessionID sessionID)
			throws FieldNotFound, IncorrectTagValue {
		if (message.getHeader().isSetField(MsgType.FIELD)) {
			String msgType = message.getHeader().getString(MsgType.FIELD);
			switch (msgType) {
				case (MsgType.MARKET_DATA_SNAPSHOT_FULL_REFRESH): {
					System.out.println("MARKET_DATA_SNAPSHOT_FULL_REFRESH");
					log.debug("MARKET_DATA_SNAPSHOT_FULL_REFRESH");
				}
				case (MsgType.MARKET_DATA_INCREMENTAL_REFRESH): {
					System.out.println("MARKET_DATA_INCREMENTAL_REFRESH");
					log.debug("MARKET_DATA_INCREMENTAL_REFRESH");
				}
				case (MsgType.SECURITY_LIST): {
					System.out.println("SECURITY_LIST");
					log.debug("SECURITY_LIST");
				}
				case (MsgType.NEWS): {
					System.out.println("NEWS");
					log.debug("NEWS");
				}
			}
		}
		//throw new UnsupportedMessageType();
	}

	private void sendSessionReject(Message message, int rejectReason)
			throws FieldNotFound, SessionNotFound {
		Message reply = createMessage(message, MsgType.REJECT);
		reverseRoute(message, reply);
		String refSeqNum = message.getHeader().getString(MsgSeqNum.FIELD);
		reply.setString(RefSeqNum.FIELD, refSeqNum);
		reply.setString(RefMsgType.FIELD,
				message.getHeader().getString(MsgType.FIELD));
		reply.setInt(SessionRejectReason.FIELD, rejectReason);
		Session.sendToTarget(reply);
	}

	private void sendBusinessReject(Message message, int rejectReason,
			String rejectText) throws FieldNotFound, SessionNotFound {
		Message reply = createMessage(message, MsgType.BUSINESS_MESSAGE_REJECT);
		reverseRoute(message, reply);
		String refSeqNum = message.getHeader().getString(MsgSeqNum.FIELD);
		reply.setString(RefSeqNum.FIELD, refSeqNum);
		reply.setString(RefMsgType.FIELD,
				message.getHeader().getString(MsgType.FIELD));
		reply.setInt(BusinessRejectReason.FIELD, rejectReason);
		reply.setString(Text.FIELD, rejectText);
		Session.sendToTarget(reply);
	}

	private Message createMessage(Message message, String msgType)
			throws FieldNotFound {
		return messageFactory.create(
				message.getHeader().getString(BeginString.FIELD), msgType);
	}

	private void reverseRoute(Message message, Message reply)
			throws FieldNotFound {
		reply.getHeader().setString(SenderCompID.FIELD,
				message.getHeader().getString(TargetCompID.FIELD));
		reply.getHeader().setString(TargetCompID.FIELD,
				message.getHeader().getString(SenderCompID.FIELD));
	}

	private void send(quickfix.Message message, SessionID sessionID) {
		try {
			Session.sendToTarget(message, sessionID);
		} catch (SessionNotFound e) {
			System.out.println(e);
		}
	}

	public void addLogonObserver(Observer observer) {
		observableLogon.addObserver(observer);
	}

	public void deleteLogonObserver(Observer observer) {
		observableLogon.deleteObserver(observer);
	}

	private static class ObservableLogon extends Observable {
		private HashSet<SessionID> set = new HashSet<SessionID>();

		public void logon(SessionID sessionID) {
			set.add(sessionID);
			setChanged();
			notifyObservers(new LogonEvent(sessionID, true));
			clearChanged();
		}

		public void logoff(SessionID sessionID) {
			set.remove(sessionID);
			setChanged();
			notifyObservers(new LogonEvent(sessionID, false));
			clearChanged();
		}
	}

	public boolean isMissingField() {
		return isMissingField;
	}

	public void setMissingField(boolean isMissingField) {
		this.isMissingField = isMissingField;
	}

	public boolean isAvailable() {
		return isAvailable;
	}

	public void setAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}

	private void sendUserRequest(SessionID sessionID) throws SessionNotFound {
		try {
			UserRequestID userRequestID = new UserRequestID(
					idGenerator.genUserID());
			UserRequestType userRequestType = new UserRequestType(
					UserRequestType.LOGONUSER);
			Username username = new Username("hot_ven1");
			Password password = new Password("welcome2");

			quickfix.fix44.UserRequest userRequest = new quickfix.fix44.UserRequest(
					userRequestID, userRequestType, username);
			userRequest.set(password);

			Session.sendToTarget(userRequest, sessionID);
		} catch (SessionNotFound e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void sendSecurityListRequest(SessionID sessionID)
			throws SessionNotFound {
		SecurityReqID securityReqID = new SecurityReqID(
				idGenerator.genSecurityListID());
		SecurityListRequestType securityListRequestType = new SecurityListRequestType(
				0);

		quickfix.fix44.SecurityListRequest securityListRequest = new quickfix.fix44.SecurityListRequest(
				securityReqID, securityListRequestType);

		Session.sendToTarget(securityListRequest, sessionID);
	}

	private void sendMessage(SessionID sessionID, Message message) {
		try {
			System.out.println("sendMessage");
			Session session = Session.lookupSession(sessionID);
			if (session == null) {
				throw new SessionNotFound(sessionID.toString());
			}

			DataDictionaryProvider dataDictionaryProvider = session
					.getDataDictionaryProvider();
			if (dataDictionaryProvider != null) {
				try {
					dataDictionaryProvider.getApplicationDataDictionary(
							getApplVerID(session, message)).validate(message,
							true);
				} catch (Exception e) {
					LogUtil.logThrowable(
							sessionID,
							"Outgoing message failed validation: "
									+ e.getMessage(), e);
					return;
				}
			}

			session.send(message);
		} catch (SessionNotFound e) {

		}
	}

	private ApplVerID getApplVerID(Session session, Message message) {
		System.out.println("ApplVerID");
		String beginString = session.getSessionID().getBeginString();
		if (FixVersions.BEGINSTRING_FIXT11.equals(beginString)) {
			return new ApplVerID(ApplVerID.FIX50);
		} else {
			return MessageUtils.toApplVerID(beginString);
		}
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
}
