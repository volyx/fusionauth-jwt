package io.fusionauth.jwt.json;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Beware: this is NOT a complete and fully compliant JSON parser!
 * Its main purpose is to decode typical simple queries without third-party dependencies.
 */
public class JsonReader {
    protected byte[] array;
    protected int offset;
    protected int next;

    public JsonReader(byte[] array) {
        this(array, 0);
    }

    public JsonReader(byte[] array, int offset) {
        this.array = array;
        this.offset = offset;
        skipWhitespace();
    }

    protected int read() {
        int b = next;
        next = offset < array.length ? array[offset++] & 0xff : -1;
        return b;
    }

    public final int next() {
        return next;
    }

    public final int skipWhitespace() {
        while (next <= ' ' && next != -1) {
            read();
        }
        return next;
    }

    public final IOException exception(String message) {
        return new IOException(message + " at " + offset);
    }

    public final void expect(int b, String message) throws IOException {
        if (read() != b) {
            throw exception(message);
        }
    }

    public final boolean readBoolean() throws IOException {
        int b = read();
        if (b == 't' && read() == 'r' && read() == 'u' && read() == 'e') {
            return true;
        } else if (b == 'f' && read() == 'a' && read() == 'l' && read() == 's' && read() == 'e') {
            return false;
        }
        throw exception("Expected boolean");
    }

    public final byte readByte() throws IOException {
        return Byte.parseByte(readNumber());
    }

    public final short readShort() throws IOException {
        return Short.parseShort(readNumber());
    }

    public final char readChar() throws IOException {
        return readString().charAt(0);
    }

    public final int readInt() throws IOException {
        return Integer.parseInt(readNumber());
    }

    public final long readLong() throws IOException {
        return Long.parseLong(readNumber());
    }

    public float readFloat() throws IOException {
        return Float.parseFloat(readNumber());
    }

    public final double readDouble() throws IOException {
        return Double.parseDouble(readNumber());
    }

    public final String readNumber() throws IOException {
        StringBuilder sb = new StringBuilder();

        // Sign
        if (next == '-') {
            sb.append((char) read());
        }

        // Integer
        int nochars = sb.length();
        while (next >= '0' && next <= '9') {
            sb.append((char) read());
        }

        // Fraction
        if (next == '.') {
            sb.append((char) read());
            if (!(next >= '0' && next <= '9')) throw exception("Expected number");
            do {
                sb.append((char) read());
            } while (next >= '0' && next <= '9');
        }

        if (sb.length() <= nochars) {
            throw exception("Expected number");
        }

        // Exponent
        if (next == 'e' || next == 'E') {
            sb.append((char) read());
            if (next == '-' || next == '+') {
                sb.append((char) read());
            }
            if (!(next >= '0' && next <= '9')) throw exception("Expected number");
            do {
                sb.append((char) read());
            } while (next >= '0' && next <= '9');
        }

        return sb.toString();
    }

    public final int readHexChar() throws IOException {
        int b = read();
        if (b >= '0' && b <= '9') {
            return b - '0';
        } else if (b >= 'A' && b <= 'F') {
            return b - 'A';
        } else if (b >= 'a' && b <= 'f') {
            return b - 'a';
        }
        throw exception("Invalid escape character");
    }

    public final char readEscapeChar() throws IOException {
        int b = read();
        switch (b) {
            case 'b':
                return '\b';
            case 'f':
                return '\f';
            case 'n':
                return '\n';
            case 'r':
                return '\r';
            case 't':
                return '\t';
            case 'u':
                return (char) (readHexChar() << 12 | readHexChar() << 8 | readHexChar() << 4 | readHexChar());
            default:
                return (char) b;
        }
    }

    public String readString() throws IOException {
        StringBuilder sb = new StringBuilder();
        expect('\"', "Expected string");
        while (next >= 0 && next != '\"') {
            int b = read();
            if ((b & 0x80) == 0) {
                sb.append(b == '\\' ? readEscapeChar() : (char) b);
            } else if ((b & 0xe0) == 0xc0) {
                sb.append((char) ((b & 0x1f) << 6 | (read() & 0x3f)));
            } else if ((b & 0xf0) == 0xe0) {
                sb.append((char) ((b & 0x0f) << 12 | (read() & 0x3f) << 6 | (read() & 0x3f)));
            } else {
                int v = (b & 0x07) << 18 | (read() & 0x3f) << 12 | (read() & 0x3f) << 6 | (read() & 0x3f);
                sb.append((char) (0xd800 | (v - 0x10000) >>> 10)).append((char) (0xdc00 | (v & 0x3ff)));
            }
        }
        expect('\"', "Unexpected end of string");
        return sb.toString();
    }

    public ArrayList<Object> readArray() throws IOException, ClassNotFoundException {
        ArrayList<Object> result = new ArrayList<>();
        expect('[', "Expected array");
        for (boolean needComma = false; skipWhitespace() != ']'; needComma = true) {
            if (needComma) {
                expect(',', "Unexpected end of array");
                skipWhitespace();
            }
            result.add(readObject());
        }
        read();
        return result;
    }

    public Map<String, Object> readMap() throws IOException, ClassNotFoundException {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        expect('{', "Expected map");
        for (boolean needComma = false; skipWhitespace() != '}'; needComma = true) {
            if (needComma) {
                expect(',', "Unexpected end of map");
                skipWhitespace();
            }

            String key = readString();
            skipWhitespace();
            expect(':', "Expected key-value pair");
            skipWhitespace();
            result.put(key, readObject());
        }
        read();
        return result;
    }

    public Object readObject() throws IOException, ClassNotFoundException {
        switch (next) {
            case 'n':
                if (read() != 'n' || read() != 'u' || read() != 'l' || read() != 'l') {
                    throw exception("Expected null");
                }
                return null;
            case 'f':
            case 't':
                return readBoolean();
            case '\"':
                return readString();
            case '[':
                return readArray();
            case '{':
                return readMap();
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case '-':
            case '.':
                return parseNumber(readNumber());
        }
        throw exception("Expected JSON object");
    }
    private static Number parseNumber(String number) {
        int length = number.length();
        for (int i = 0; i < length; i++) {
            char c = number.charAt(i);
            if (c == '.' || c > '9') {
                return new BigDecimal(number);
//                return Double.parseDouble(number);
            }
        }
        return new BigInteger(number);
//        long n = Long.parseLong(number);
//        return n == (int) n ? Integer.valueOf((int) n) : Long.valueOf(n);
    }
}