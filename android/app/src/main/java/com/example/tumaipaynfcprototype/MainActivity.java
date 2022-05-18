package com.example.paynfcprototype;

import static com.example.paynfcprototype.CardService.ByteArrayToHexString;
import static com.example.paynfcprototype.CardService.HexStringToByteArray;

import android.content.SharedPreferences;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.preference.PreferenceManager;

import io.flutter.embedding.android.FlutterActivity;

import androidx.annotation.NonNull;

import java.util.Map;

import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugins.GeneratedPluginRegistrant;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "com.nfc.pay";

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);
        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL).setMethodCallHandler(
                (call, result) -> {
                    final Map<String, Object> arg = call.arguments();
                    String val = (String) arg.get("val");

//                    to pass objects
//                    Object val = arg.get("val");

                    if (call.method.equals("nigelreign")) {
                        NdefRecord payload = getMediaRecord(val, "bytes");
                        String ndefMessage = ByteArrayToHexString(payload.getPayload());
                        System.out.println(ByteArrayToHexString(payload.getPayload()));

                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("ndefMessage", ndefMessage);
                        editor.apply();

                        NdefRecord mRecord = NdefRecord.createTextRecord("en", val);
                        NdefMessage mMsg = new NdefMessage(mRecord);

                        System.out.println(mMsg.toString());

                    } else {
                        System.out.println("ziwan bro");
                    }
                }
        );
    }

    public static NdefRecord getMediaRecord(String messageToWrite,
                                            String type) {

        byte[] textBytes = messageToWrite.getBytes();
        NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
                type.getBytes(), new byte[]{}, textBytes);
        return textRecord;
    }
}
