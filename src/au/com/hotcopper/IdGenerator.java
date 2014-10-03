/*******************************************************************************
 * Copyright (c) quickfixengine.org  All rights reserved. 
 * 
 * This file is part of the QuickFIX FIX Engine 
 * 
 * This file may be distributed under the terms of the quickfixengine.org 
 * license as defined by quickfixengine.org and appearing in the file 
 * LICENSE included in the packaging of this file. 
 * 
 * This file is provided AS IS with NO WARRANTY OF ANY KIND, INCLUDING 
 * THE WARRANTY OF DESIGN, MERCHANTABILITY AND FITNESS FOR A 
 * PARTICULAR PURPOSE. 
 * 
 * See http://www.quickfixengine.org/LICENSE for licensing information. 
 * 
 * Contact ask@quickfixengine.org if any conditions of this licensing 
 * are not clear to you.
 ******************************************************************************/

package au.com.hotcopper;

public class IdGenerator {
    private int userIdCounter = 0;
    private int securityListIdCounter = 0;
    private int marketDataIdCounter = 0;
    
    public String genUserID() {
        return Integer.toString(userIdCounter++);
    }

    public String genSecurityListID() {
        return Integer.toString(securityListIdCounter++);
    }

    public String genMarketDataID() {
        return Integer.toString(marketDataIdCounter++);
    }
}
