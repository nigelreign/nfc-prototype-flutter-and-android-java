import 'dart:typed_data';

import 'package:flutter/material.dart';
import 'package:nfc_manager/nfc_manager.dart';
import 'RecordReader.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(NFCReader());
}

class NFCReader extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => NFCReaderState();
}

class NFCReaderState extends State<NFCReader> {
  String result = "";

  @override
  void initState() {
    NfcManager.instance.startSession(onDiscovered: (NfcTag tag) async {
      Object? tech;
      // result.value = tag.data;
      tech = Ndef.from(tag);
      if (tech is Ndef) {
        final cachedMessage = tech.cachedMessage;
        if (cachedMessage != null) {
          Iterable.generate(cachedMessage.records.length).forEach((i) {
            final record = cachedMessage.records[i];
            final _record = Record.fromNdef(record);
            print("===what===");
            if (_record is WellknownTextRecord) {
              setState(() {
                result = _record.text;
              });
              print("result ${result}");
            }
          });
        }
      }
      NfcManager.instance.stopSession();
    });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: Text('NFC READER')),
        body: SafeArea(
          child: Flex(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            direction: Axis.vertical,
            children: [
              SizedBox(height: 5),
              Text(
                "Tap your phone or card",
                style: TextStyle(
                  color: Colors.black,
                  fontWeight: FontWeight.bold,
                  fontSize: 24,
                ),
              ),
              SizedBox(height: 5),
              Flexible(
                flex: 2,
                child: Text(
                  "This is the text from the emulator: ${result}",
                  style: TextStyle(
                    color: Colors.black,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
              Flexible(
                flex: 3,
                child: GridView.count(
                  padding: EdgeInsets.all(4),
                  crossAxisCount: 2,
                  childAspectRatio: 4,
                  crossAxisSpacing: 4,
                  mainAxisSpacing: 4,
                  children: [
                    ElevatedButton(
                        child: Text('Tag Read'), onPressed: _tagRead),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  void _tagRead() {
    setState(() {
      result = '';
    });
    // NfcManager.instance.startSession(onDiscovered: (NfcTag tag) async {
    //   Object? tech;
    //   // result.value = tag.data;
    //   tech = Ndef.from(tag);
    //   if (tech is Ndef) {
    //     final cachedMessage = tech.cachedMessage;
    //     if (cachedMessage != null) {
    //       Iterable.generate(cachedMessage.records.length).forEach((i) {
    //         final record = cachedMessage.records[i];
    //         final _record = Record.fromNdef(record);
    //         print("===what===");
    //         if (_record is WellknownTextRecord) {
    //           setState(() {
    //             result = _record.text;
    //           });
    //           print("result ${result}");
    //         }
    //       });
    //     }
    //   }
    //
    //   NfcManager.instance.stopSession();
    // });
  }
}
