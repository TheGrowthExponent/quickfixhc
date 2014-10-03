package au.com.hotcopper;

import quickfix.SessionID;
import quickfix.examples.banzai.Order;
import quickfix.field.MDEntryDate;
import quickfix.field.MDEntryPx;
import quickfix.field.MDEntrySize;
import quickfix.field.OrderID;
import quickfix.field.Symbol;

public class MarketData {
    private SessionID sessionID = null;
    private Symbol symbol = null;
    private boolean isNew = true;
    private String message = null;
    private String ID = null;
    private String originalID = null;
    private static int nextID = 1;
    private MDEntryPx mDEntryPx = null;
    private MDEntrySize mDEntrySize = null;
    private MDEntryDate mDEntryDate = null;
    private OrderID orderID = null;

    public MarketData() {
        ID = generateID();
    }
    
    public MarketData(String ID) {
        this.ID = ID;
    }

    public Object clone() {
        try {
        	MarketData marketData = (MarketData)super.clone();
        	marketData.setOriginalID(getID());
        	marketData.setID(marketData.generateID());
            return marketData;
        } catch(CloneNotSupportedException e) {}
        return null;
    }

    public String generateID() {
        return Long.valueOf(System.currentTimeMillis()+(nextID++)).toString();
    }
    public SessionID getSessionID() {
        return sessionID;
    }
    public void setSessionID(SessionID sessionID) {
        this.sessionID = sessionID;
    }
    public Symbol getSymbol() {
        return symbol;
    }
    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }
    public MDEntryPx getMDEntryPx() {
        return mDEntryPx;
    }
    public void setMDEntryPx(MDEntryPx mDEntryPx) {
        this.mDEntryPx = mDEntryPx;
    }
    public MDEntrySize getMDEntrySize() {
        return mDEntrySize;
    }
    public void setMDEntrySize(MDEntrySize mDEntrySize) {
        this.mDEntrySize = mDEntrySize;
    }
    public MDEntryDate getMDEntryDate() {
        return mDEntryDate;
    }
    public void setMDEntryDate(MDEntryDate mDEntryDate) {
        this.mDEntryDate = mDEntryDate;
    }
    public OrderID getOrderID() {
        return orderID;
    }
    public void setOrderID(OrderID orderID) {
        this.orderID = orderID;
    }
    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }
    public boolean isNew() {
        return isNew;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getMessage() {
        return message;
    }
    public void setID(String ID) {
        this.ID = ID;
    }
    public String getID() {
        return ID;
    }
    public void setOriginalID(String originalID) {
        this.originalID = originalID;
    }
    public String getOriginalID() {
        return originalID;
    }
}
