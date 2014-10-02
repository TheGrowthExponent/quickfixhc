/*******************************************************************************
 * 
 ******************************************************************************/

package au.com.hotcopper;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.ConfigError;
import quickfix.DataDictionaryProvider;
import quickfix.DoNotSend;
import quickfix.FieldConvertError;
import quickfix.FieldNotFound;
import quickfix.FixVersions;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.LogUtil;
import quickfix.Message;
import quickfix.MessageUtils;
import quickfix.RejectLogon;
import quickfix.Session;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.SessionSettings;
import quickfix.UnsupportedMessageType;
import quickfix.examples.executor.MarketDataProvider;
import quickfix.field.ApplVerID;
import quickfix.field.AvgPx;
import quickfix.field.CumQty;
import quickfix.field.ExecID;
import quickfix.field.ExecTransType;
import quickfix.field.ExecType;
import quickfix.field.LastPx;
import quickfix.field.LastQty;
import quickfix.field.LastShares;
import quickfix.field.LeavesQty;
import quickfix.field.OrdStatus;
import quickfix.field.OrdType;
import quickfix.field.OrderID;
import quickfix.field.OrderQty;
import quickfix.field.Price;
import quickfix.field.SecurityReqID;
import quickfix.field.SecurityListRequestType;
import quickfix.field.Side;
import quickfix.field.Symbol;

/**
 * @author Jason Pascoe
 *
 */
public class Application extends quickfix.MessageCracker implements
		quickfix.Application {
	private final Logger log = LoggerFactory.getLogger(getClass());
	private MarketDataProvider marketDataProvider;

	public Application(SessionSettings settings) throws ConfigError, FieldConvertError {
		initializeMarketDataProvider(settings);
	}

	private void initializeMarketDataProvider(SessionSettings settings)
			throws ConfigError, FieldConvertError {
	}

	public void onCreate(SessionID sessionID) {
		Session.lookupSession(sessionID).getLog();
	}

	public void onLogon(SessionID sessionID) {
	}

	public void onLogout(SessionID sessionID) {
	}

	public void toAdmin(quickfix.Message message, SessionID sessionID) {
	}

	public void toApp(quickfix.Message message, SessionID sessionID)
			throws DoNotSend {
	}

	public void fromAdmin(quickfix.Message message, SessionID sessionID)
			throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue,
			RejectLogon {
	}

	public void fromApp(quickfix.Message message, SessionID sessionID)
			throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue,
			UnsupportedMessageType {
		crack(message, sessionID);
	}

	private Price getPrice(Message message) throws FieldNotFound {
		Price price;
		if (message.getChar(OrdType.FIELD) == OrdType.LIMIT) {
			price = new Price(message.getDouble(Price.FIELD));
		} else {
			if (marketDataProvider == null) {
				throw new RuntimeException(
						"No market data provider specified for market order");
			}
			char side = message.getChar(Side.FIELD);
			if (side == Side.BUY) {
				price = new Price(marketDataProvider.getAsk(message
						.getString(Symbol.FIELD)));
			} else if (side == Side.SELL || side == Side.SELL_SHORT) {
				price = new Price(marketDataProvider.getBid(message
						.getString(Symbol.FIELD)));
			} else {
				throw new RuntimeException("Invalid order side: " + side);
			}
		}
		return price;
	}

	private void sendMessage(SessionID sessionID, Message message) {
		try {
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
			log.error(e.getMessage(), e);
		}
	}

	private ApplVerID getApplVerID(Session session, Message message) {
		String beginString = session.getSessionID().getBeginString();
		if (FixVersions.BEGINSTRING_FIXT11.equals(beginString)) {
			return new ApplVerID(ApplVerID.FIX50);
		} else {
			return MessageUtils.toApplVerID(beginString);
		}
	}

	void sendSecurityListRequest() throws SessionNotFound
	{
	  quickfix.fix44.SecurityListRequest message = new quickfix.fix44.SecurityListRequest(
		new SecurityReqID("123"),
		new SecurityListRequestType(0));

	  Session.sendToTarget(message);
	}

}
