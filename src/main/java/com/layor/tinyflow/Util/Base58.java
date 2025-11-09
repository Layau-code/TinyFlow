package com.layor.tinyflow.Util;

    public final class Base58 {
        private static final char[] ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();

        private Base58() {}

        public static String encode(long value) {
            if (value == 0) return String.valueOf(ALPHABET[0]);
            StringBuilder sb = new StringBuilder();
            long v = value;
            while (v > 0) {
                int mod = (int) (v % 58);
                sb.append(ALPHABET[mod]);
                v /= 58;
            }
            return sb.reverse().toString();
        }
    }

