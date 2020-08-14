package com.sk.weichat.helper;

import com.sk.weichat.util.Base64;
import com.sk.weichat.util.secure.MAC;
import com.sk.weichat.util.secure.Parameter;

import java.util.Map;

public class MessageSecureHelper {
    public static void mac(String messageKey, Map<String, Object> message) {
        String mac = MAC.encodeBase64((Parameter.joinObjectValues(message)).getBytes(), Base64.decode(messageKey));
        message.put("mac", mac);
    }
}
