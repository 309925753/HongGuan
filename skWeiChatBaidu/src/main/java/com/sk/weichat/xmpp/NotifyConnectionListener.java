package com.sk.weichat.xmpp;

import org.jivesoftware.smack.XMPPConnection;

public interface NotifyConnectionListener {
    void notifyConnecting();

    void notifyConnected(XMPPConnection arg0);

    void notifyAuthenticated(XMPPConnection arg0);

    void notifyConnectionClosed();

    void notifyConnectionClosedOnError(Exception arg0);
}
