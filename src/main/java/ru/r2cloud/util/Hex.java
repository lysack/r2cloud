package ru.r2cloud.util;

public class Hex {

    private static final char[] HEX_CHARS = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    public static String encode(byte[] bytes) {
        final int nBytes = bytes.length;
        char[] result = new char[2*nBytes];

        int j = 0;
        for (int i=0; i < nBytes; i++) {
            // Char for top 4 bits
            result[j++] = HEX_CHARS[(0xF0 & bytes[i]) >>> 4 ];
            // Bottom 4
            result[j++] = HEX_CHARS[(0x0F & bytes[i])];
        }

        return new String(result);
    }
    
    private Hex() {
    	//do nothing
    }
}
