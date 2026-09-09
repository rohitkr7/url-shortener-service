package com.portfolio.urlshortener.util;

public final class Base62Encoder {

    private static final String BASE62_CHARS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = BASE62_CHARS.length();

    private Base62Encoder() {}

    public static String encode(long value) {
        if (value <= 0) {
            return "0";
        }
        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            int remainder = (int) (value % BASE);
            sb.append(BASE62_CHARS.charAt(remainder));
            value /= BASE;
        }
        return sb.reverse().toString();
    }
}