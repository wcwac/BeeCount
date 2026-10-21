import 'dart:convert';
import 'dart:typed_data';

import 'package:beecount/services/import/file_reader.dart';
import 'package:file_picker/file_picker.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:share_plus/share_plus.dart' show XFile;

// Providers can return a URI without a local path. Reading must go through the
// picked file's asynchronous API rather than the removed eager `bytes` field.
final class _MemoryPickedFile extends PlatformFile {
  _MemoryPickedFile(this.name, List<int> bytes)
      : _bytes = Uint8List.fromList(bytes);

  @override
  final String name;
  final Uint8List _bytes;
  int reads = 0;

  @override
  Uri get uri => Uri.parse('memory:///$name');

  @override
  XFile get xFile => XFile.fromData(_bytes, name: name);

  @override
  int? lengthSync() => _bytes.length;

  @override
  Future<int?> length() async => _bytes.length;

  @override
  Future<Uint8List> readAsBytes() async {
    reads++;
    return _bytes;
  }

  @override
  Stream<Uint8List> readAsByteStream() => Stream.value(_bytes);
}

void main() {
  test('reads a UTF-8 BOM bill from a picked file without a local path', () async {
    const bill = '金额,商户\r\n12.50,午餐\r\n';
    final picked = _MemoryPickedFile('bill.csv', [
      0xef,
      0xbb,
      0xbf,
      ...utf8.encode(bill),
    ]);

    expect(picked.path, isNull);
    expect(await FileReaderService.readFile(picked), bill);
    expect(picked.reads, 1);
  });

  test('passes XLSX bytes from a picked file to the workbook converter', () async {
    final picked = _MemoryPickedFile('bill.XLSX', [0x50, 0x4b, 3, 4]);
    Uint8List? received;

    final result = await FileReaderService.readFile(
      picked,
      xlsxConverter: (bytes) {
        received = bytes;
        return '金额,商户\r\n12.50,午餐';
      },
    );

    expect(received, [0x50, 0x4b, 3, 4]);
    expect(result, '金额,商户\r\n12.50,午餐');
    expect(picked.reads, 1);
  });
}
