import 'dart:typed_data';

import 'package:beecount/utils/file_picker_helper.dart';
import 'package:file_picker/file_picker.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:share_plus/share_plus.dart' show XFile;

final class _ChunkedPickedFile extends PlatformFile {
  _ChunkedPickedFile(this.chunks, {this.reportedLength});

  final List<List<int>> chunks;
  final int? reportedLength;
  int chunksRead = 0;
  bool closed = false;

  @override
  String get name => 'backup.json';

  @override
  Uri get uri => Uri.parse('memory:///backup.json');

  @override
  XFile get xFile => throw UnsupportedError('The reader must use the stream.');

  @override
  int? lengthSync() => reportedLength;

  @override
  Future<int?> length() => throw UnsupportedError('Do not load unknown sizes.');

  @override
  Future<Uint8List> readAsBytes() => throw UnsupportedError('Do not read eagerly.');

  @override
  Stream<Uint8List> readAsByteStream() async* {
    try {
      for (final chunk in chunks) {
        chunksRead++;
        yield Uint8List.fromList(chunk);
      }
    } finally {
      closed = true;
    }
  }
}

void main() {
  test('accepts a pathless backup exactly at the byte cap', () async {
    final file = _ChunkedPickedFile([[1, 2], [3, 4]]);

    expect(await FilePickerHelper.readBytes(file, maxBytes: 4), [1, 2, 3, 4]);
    expect(file.closed, isTrue);
  });

  for (final reportedLength in <int?>[null, 1]) {
    test('cancels an oversized stream with reported length $reportedLength', () async {
      final file = _ChunkedPickedFile(
        [[1, 2], [3, 4, 5], [6]],
        reportedLength: reportedLength,
      );

      await expectLater(
        FilePickerHelper.readBytes(file, maxBytes: 4),
        throwsFormatException,
      );
      expect(file.chunksRead, 2);
      expect(file.closed, isTrue);
    });
  }

  test('rejects a known oversized backup before opening its stream', () async {
    final file = _ChunkedPickedFile([[1]], reportedLength: 5);

    await expectLater(
      FilePickerHelper.readBytes(file, maxBytes: 4),
      throwsFormatException,
    );
    expect(file.chunksRead, 0);
  });
}
