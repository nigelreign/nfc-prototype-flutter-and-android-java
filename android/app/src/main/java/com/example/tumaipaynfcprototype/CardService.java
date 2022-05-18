package com.example.paynfcprototype;

import android.content.Context;
import android.content.SharedPreferences;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.example.paynfcprototype.common.logger.Log;

import java.util.Arrays;

public class CardService extends HostApduService {
    private static final String TAG = "CardService";

    /* NDEF message definition
     * 0x00, 0x0B,                 NDEF message size
     * 0xD1,                       NDEF RECORD HEADER MB/ME/CF/1/IL/TNF
     * 0x01,                       TYPE LENGTH
     * 0x07,                       PAYLOAD LENTGH
     * 'T',                        TYPE
     * 0x02,                       Language length
     * 'e', 'n',                   Language
     * 'T', 'e', 's', 't'          Text
     */


    private static final byte[] T4T_NDEF_EMU_APP_Select = HexStringToByteArray("00A4040007D2760000850101");
    private static final byte[] T4T_NDEF_EMU_CC = HexStringToByteArray("000F2000FF00FF0406E10400FF00FF");
    private static final byte[] T4T_NDEF_EMU_CC_Select = HexStringToByteArray("00A4000C02E103");
    private static final byte[] T4T_NDEF_EMU_NDEF_Select = HexStringToByteArray("00A4000C02E104");
    private static final byte[] T4T_NDEF_EMU_Read = HexStringToByteArray("00B0");
    private static final byte[] SELECT_OK_SW = HexStringToByteArray("9000");
    private static final byte[] UNKNOWN_CMD_SW = HexStringToByteArray("0000");

    private enum NDEF_state {
        Ready,
        NDEF_Application_Selected,
        CC_Selected,
        NDEF_Selected
    }

    private static NDEF_state eT4T_NDEF_EMU_State = NDEF_state.Ready;

    @Override
    public void onDeactivated(int reason) {
    }

    private boolean IsEqual(byte[] a, byte[] b, int n) {
        for (int i = 0; i < n; i++) {
            if (a[i] != b[i]) return false;
        }
        return true;
    }

    // BEGIN_INCLUDE(processCommandApdu)
    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        byte[] Answer = UNKNOWN_CMD_SW;

        Log.i(TAG, "Received APDU: " + ByteArrayToHexString(commandApdu));

        if (IsEqual(T4T_NDEF_EMU_APP_Select, commandApdu, T4T_NDEF_EMU_APP_Select.length)) {
            eT4T_NDEF_EMU_State = NDEF_state.NDEF_Application_Selected;
            Answer = SELECT_OK_SW;
        } else if (IsEqual(T4T_NDEF_EMU_CC_Select, commandApdu, T4T_NDEF_EMU_CC_Select.length)) {
            if (eT4T_NDEF_EMU_State == NDEF_state.NDEF_Application_Selected) {
                eT4T_NDEF_EMU_State = NDEF_state.CC_Selected;
                Answer = SELECT_OK_SW;
            } else {
                eT4T_NDEF_EMU_State = NDEF_state.Ready;
            }
        } else if (IsEqual(T4T_NDEF_EMU_NDEF_Select, commandApdu, T4T_NDEF_EMU_NDEF_Select.length)) {
            eT4T_NDEF_EMU_State = NDEF_state.NDEF_Selected;
            Answer = SELECT_OK_SW;
        } else if (IsEqual(T4T_NDEF_EMU_Read, commandApdu, T4T_NDEF_EMU_Read.length)) {
            if (eT4T_NDEF_EMU_State == NDEF_state.CC_Selected) {
                Answer = ConcatArrays(T4T_NDEF_EMU_CC, SELECT_OK_SW);
            } else if (eT4T_NDEF_EMU_State == NDEF_state.NDEF_Selected) {
                int offset = (commandApdu[2] << 8) + commandApdu[3];
                int length = commandApdu[4];

                Log.i(TAG, "Reading NDEF file offset = " + offset + " length = " + length);

                byte[] NDEF_MESSAGE = getNdefMessage(getApplicationContext());

                if (length <= (NDEF_MESSAGE.length + offset + 2)) {
                    byte[] temp = new byte[length];
                    System.arraycopy(NDEF_MESSAGE, offset, temp, 0, temp.length);
                    Answer = ConcatArrays(temp, SELECT_OK_SW);
                } else {
                    eT4T_NDEF_EMU_State = NDEF_state.Ready;
                }
            } else {
                eT4T_NDEF_EMU_State = NDEF_state.Ready;
            }
        }

        Log.i(TAG, "state = " + eT4T_NDEF_EMU_State);
        Log.i(TAG, "Returned APDU raw: " + Arrays.toString(Answer));
        Log.i(TAG, "Returned APDU: " + ByteArrayToHexString(Answer));
        Log.i(TAG, "Returned APDU: " + ByteArrayToHexString(getNdefMessage(getApplicationContext())));

        System.out.println(ByteArrayToHexString(getNdefMessage(getApplicationContext())));

        return Answer;
    }

    public static byte[] getNdefMessage(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String ndefMessage = preferences.getString("ndefMessage", "");
//        String message = "000BD101075402656E696E74" + ndefMessage;
//        000BD101075402656E54657374
        byte[] NDEF_MESSAGE = HexStringToByteArray(ndefMessage);

        return NDEF_MESSAGE;
    }


    public static String ByteArrayToHexString(byte[] bytes) {
        final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] hexChars = new char[bytes.length * 2]; // Each byte has two hex characters (nibbles)
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF; // Cast bytes[j] to int, treating as unsigned value
            hexChars[j * 2] = hexArray[v >>> 4]; // Select hex character from upper nibble
            hexChars[j * 2 + 1] = hexArray[v & 0x0F]; // Select hex character from lower nibble
        }
        return new String(hexChars);
    }

    public static byte[] HexStringToByteArray(String s) throws IllegalArgumentException {
        int len = s.length();
        if (len % 2 == 1) {
            throw new IllegalArgumentException("Hex string must have even number of characters");
        }
        byte[] data = new byte[len / 2]; // Allocate 1 byte per 2 hex characters
        for (int i = 0; i < len; i += 2) {
            // Convert each character into a integer (base-16), then bit-shift into place
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static byte[] ConcatArrays(byte[] first, byte[]... rest) {
        int totalLength = first.length;
        for (byte[] array : rest) {
            totalLength += array.length;
        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }
}
