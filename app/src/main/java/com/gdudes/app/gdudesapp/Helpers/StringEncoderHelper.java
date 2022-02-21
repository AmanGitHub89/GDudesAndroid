package com.gdudes.app.gdudesapp.Helpers;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class StringEncoderHelper {
    public static final String ALLOWED_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-_.!~*()";

    public static String encodeURIComponent(String input) {
        try {
            if (input == null || input.equals("")) {
                return input;
            }
            return URLEncoder.encode(input, "utf-8");

//            int l = input.length();
//            StringBuilder o = new StringBuilder(l * 3);
//            try {
//                for (int i = 0; i < l; i++) {
//                    String e = input.substring(i, i + 1);
//                    if (ALLOWED_CHARS.indexOf(e) == -1) {
//                        byte[] b = e.getBytes("utf-8");
//                        o.append(getHex(b));
//                        continue;
//                    }
//                    o.append(e);
//                }
//                return o.toString();
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//                GDLogHelper.LogException(e);
//            }
//            return input;
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            return input;
        }
    }

    private static String getHex(byte buf[]) {
        try {
            StringBuilder o = new StringBuilder(buf.length * 3);
            for (int i = 0; i < buf.length; i++) {
                int n = (int) buf[i] & 0xff;
                o.append("%");
                if (n < 0x10) {
                    o.append("0");
                }
                o.append(Long.toString(n, 16).toUpperCase());
            }
            return o.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            return "";
        }
    }

    public static String decodeURIComponent(String encodedURI) {
//        char actualChar;
//
//        StringBuffer buffer = new StringBuffer();
//
//        int bytePattern, sumb = 0;
//
//        for (int i = 0, more = -1; i < encodedURI.length(); i++) {
//            actualChar = encodedURI.charAt(i);
//
//            switch (actualChar) {
//                case '%': {
//                    actualChar = encodedURI.charAt(++i);
//                    int hb = (Character.isDigit(actualChar) ? actualChar - '0'
//                            : 10 + Character.toLowerCase(actualChar) - 'a') & 0xF;
//                    actualChar = encodedURI.charAt(++i);
//                    int lb = (Character.isDigit(actualChar) ? actualChar - '0'
//                            : 10 + Character.toLowerCase(actualChar) - 'a') & 0xF;
//                    bytePattern = (hb << 4) | lb;
//                    break;
//                }
//                case '+': {
//                    bytePattern = ' ';
//                    break;
//                }
//                default: {
//                    bytePattern = actualChar;
//                }
//            }
//
//            if ((bytePattern & 0xc0) == 0x80) { // 10xxxxxx
//                sumb = (sumb << 6) | (bytePattern & 0x3f);
//                if (--more == 0)
//                    buffer.append((char) sumb);
//            } else if ((bytePattern & 0x80) == 0x00) { // 0xxxxxxx
//                buffer.append((char) bytePattern);
//            } else if ((bytePattern & 0xe0) == 0xc0) { // 110xxxxx
//                sumb = bytePattern & 0x1f;
//                more = 1;
//            } else if ((bytePattern & 0xf0) == 0xe0) { // 1110xxxx
//                sumb = bytePattern & 0x0f;
//                more = 2;
//            } else if ((bytePattern & 0xf8) == 0xf0) { // 11110xxx
//                sumb = bytePattern & 0x07;
//                more = 3;
//            } else if ((bytePattern & 0xfc) == 0xf8) { // 111110xx
//                sumb = bytePattern & 0x03;
//                more = 4;
//            } else { // 1111110x
//                sumb = bytePattern & 0x01;
//                more = 5;
//            }
//        }
//        return buffer.toString();
        try {
            if (encodedURI == null) {
                return "";
            }

            String result = "";

            try {
                result = URLDecoder.decode(encodedURI, "UTF-8");
            }

            // This exception should never occur.
            catch (UnsupportedEncodingException e) {
                result = encodedURI;
            }

            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            return "";
        }
    }

    public static String doubleDecodeURIComponent(String encodedURI) {
        try {
            if (encodedURI == null) {
                return "";
            }
            String firstDecode = decodeURIComponent(encodedURI);

            String secondDecode = decodeURIComponent(EncodeOnlyForPercentage(firstDecode));

            return secondDecode;
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            return "";
        }
    }

    private static String EncodeOnlyForPercentage(String input) {
        try {
            if (input == null || input.equals("")) {
                return input;
            }

            int l = input.length();
            StringBuilder o = new StringBuilder(l * 3);
            try {
                for (int i = 0; i < l; i++) {
                    String e = input.substring(i, i + 1);
                    if (!e.equals("%")) {
                        o.append(e);
                        continue;
                    }
                    if (input.substring(i).length() >= 12) {
                        //Could be a smiley code ahead. Do not encode.
                        String checkSmileyCode = input.substring(i, i + 12);
                        if (GDSmileyHelper.IsSmileyCode(checkSmileyCode)) {
                            o.append(checkSmileyCode);
                            i = i + 11;
                            continue;
                        }
                    }
                    //This is a loner percentage symbol
                    byte[] b = e.getBytes("utf-8");
                    o.append(getHex(b));
                }
                return o.toString();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                GDLogHelper.LogException(e);
            }
            return input;
        } catch (Exception ex) {
            ex.printStackTrace();
            GDLogHelper.LogException(ex);
            return input;
        }
    }
}
