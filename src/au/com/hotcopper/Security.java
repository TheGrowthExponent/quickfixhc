package au.com.hotcopper;

import quickfix.SessionID;

public class Security {
    private SessionID sessionID = null;
    private String symbol = null;
    private boolean isNew = true;
    private String message = null;
    private String ID = null;
    private String originalID = null;
    private static int nextID = 1;
	
    public Security() {
        ID = generateID();
    }
    
    public Security(String ID) {
        this.ID = ID;
    }

    public Object clone() {
        try {
        	Security security = (Security)super.clone();
        	security.setOriginalID(getID());
        	security.setID(security.generateID());
            return security;
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
    public String getSymbol() {
        return symbol;
    }
    public void setSymbol(String symbol) {
        this.symbol = symbol;
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
