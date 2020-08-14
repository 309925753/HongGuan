package com.sk.weichat.xmpp.util;


public class XmppStringUtil {

    public static boolean isJID(String jid) {
        int i = jid.indexOf("@");
        if (i <= 0) {
            return false;
        }
        return true;
    }


    public static String getRoomJID(String from) {
        int i = from.indexOf("/");
        if (i <= 0) {
            return "";
        }
        return from.substring(0, i);
    }

    public static String getRoomJIDPrefix(String roomJid) {
        int i = roomJid.indexOf("@");
        if (i <= 0) {
            return "";
        }
        return roomJid.substring(0, i);
    }

    public static String getRoomUserNick(String from) {
        int i = from.indexOf("/");
        if (i <= 0) {
            return "";
        }
        return from.substring(i + 1, from.length());
    }

    public static String parseName(String XMPPAddress) {
        if (XMPPAddress == null) {
            return null;
        } else {
            int atIndex = XMPPAddress.lastIndexOf("@");
            return atIndex <= 0 ? "" : XMPPAddress.substring(0, atIndex);
        }
    }

    public static String parseServer(String XMPPAddress) {
        if (XMPPAddress == null) {
            return null;
        } else {
            int atIndex = XMPPAddress.lastIndexOf("@");
            if (atIndex + 1 > XMPPAddress.length()) {
                return "";
            } else {
                int slashIndex = XMPPAddress.indexOf("/");
                return slashIndex > 0 && slashIndex > atIndex ? XMPPAddress.substring(atIndex + 1, slashIndex) : XMPPAddress.substring(atIndex + 1);
            }
        }
    }

    public static String parseResource(String XMPPAddress) {
        if (XMPPAddress == null) {
            return null;
        } else {
            int slashIndex = XMPPAddress.indexOf("/");
            return slashIndex + 1 <= XMPPAddress.length() && slashIndex >= 0 ? XMPPAddress.substring(slashIndex + 1) : "";
        }
    }

    public static String parseBareAddress(String XMPPAddress) {
        if (XMPPAddress == null) {
            return null;
        } else {
            int slashIndex = XMPPAddress.indexOf("/");
            if (slashIndex < 0) {
                return XMPPAddress;
            } else {
                return slashIndex == 0 ? "" : XMPPAddress.substring(0, slashIndex);
            }
        }
    }

    public static boolean isFullJID(String jid) {
        return parseName(jid).length() > 0 && parseServer(jid).length() > 0 && parseResource(jid).length() > 0;
    }

    public static String escapeNode(String node) {
        if (node == null) {
            return null;
        } else {
            StringBuilder buf = new StringBuilder(node.length() + 8);
            int i = 0;

            for (int n = node.length(); i < n; ++i) {
                char c = node.charAt(i);
                switch (c) {
                    case '"':
                        buf.append("\\22");
                        break;
                    case '&':
                        buf.append("\\26");
                        break;
                    case '\'':
                        buf.append("\\27");
                        break;
                    case '/':
                        buf.append("\\2f");
                        break;
                    case ':':
                        buf.append("\\3a");
                        break;
                    case '<':
                        buf.append("\\3c");
                        break;
                    case '>':
                        buf.append("\\3e");
                        break;
                    case '@':
                        buf.append("\\40");
                        break;
                    case '\\':
                        buf.append("\\5c");
                        break;
                    default:
                        if (Character.isWhitespace(c)) {
                            buf.append("\\20");
                        } else {
                            buf.append(c);
                        }
                }
            }

            return buf.toString();
        }
    }

    public static String unescapeNode(String node) {
        if (node == null) {
            return null;
        } else {
            char[] nodeChars = node.toCharArray();
            StringBuilder buf = new StringBuilder(nodeChars.length);
            int i = 0;

            for (int n = nodeChars.length; i < n; ++i) {
                char c = node.charAt(i);
                if (c == '\\' && i + 2 < n) {
                    char c2 = nodeChars[i + 1];
                    char c3 = nodeChars[i + 2];
                    if (c2 == '2') {
                        switch (c3) {
                            case '0':
                                buf.append(' ');
                                i += 2;
                                continue;
                            case '2':
                                buf.append('"');
                                i += 2;
                                continue;
                            case '6':
                                buf.append('&');
                                i += 2;
                                continue;
                            case '7':
                                buf.append('\'');
                                i += 2;
                                continue;
                            case 'f':
                                buf.append('/');
                                i += 2;
                                continue;
                        }
                    } else if (c2 == '3') {
                        switch (c3) {
                            case 'a':
                                buf.append(':');
                                i += 2;
                                continue;
                            case 'b':
                            case 'd':
                            default:
                                break;
                            case 'c':
                                buf.append('<');
                                i += 2;
                                continue;
                            case 'e':
                                buf.append('>');
                                i += 2;
                                continue;
                        }
                    } else if (c2 == '4') {
                        if (c3 == '0') {
                            buf.append("@");
                            i += 2;
                            continue;
                        }
                    } else if (c2 == '5' && c3 == 'c') {
                        buf.append("\\");
                        i += 2;
                        continue;
                    }
                }

                buf.append(c);
            }

            return buf.toString();
        }
    }

    public static CharSequence escapeForXML(String string) {
        if (string == null) {
            return null;
        } else {
            char[] input = string.toCharArray();
            int len = input.length;
            StringBuilder out = new StringBuilder((int) ((double) len * 1.3D));
            int last = 0;
            int i = 0;

            while (i < len) {
                CharSequence toAppend = null;
                char ch = input[i];
                switch (ch) {
                    case '"':
                        toAppend = "&quot;";
                        break;
                    case '&':
                        toAppend = "&amp;";
                        break;
                    case '\'':
                        toAppend = "&apos;";
                        break;
                    case '<':
                        toAppend = "&lt;";
                        break;
                    case '>':
                        toAppend = "&gt;";
                }

                if (toAppend != null) {
                    if (i > last) {
                        out.append(input, last, i - last);
                    }

                    out.append(toAppend);
                    ++i;
                    last = i;
                } else {
                    ++i;
                }
            }

            if (last == 0) {
                return string;
            } else {
                if (i > last) {
                    out.append(input, last, i - last);
                }

                return out;
            }
        }
    }

    public static String encodeHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        byte[] var5 = bytes;
        int var4 = bytes.length;

        for (int var3 = 0; var3 < var4; ++var3) {
            byte aByte = var5[var3];
            if ((aByte & 255) < 16) {
                hex.append("0");
            }

            hex.append(Integer.toString(aByte & 255, 16));
        }

        return hex.toString();
    }

    public static boolean isNotEmpty(CharSequence cs) {
        return !isNullOrEmpty(cs);
    }

    public static boolean isNullOrEmpty(CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

}
