/// A selected image already copied into private, content-addressed storage.
/// File I/O is complete; its database row can join the transaction being saved.
class PreparedAttachment {
  final String fileName;
  final String originalName;
  final int fileSize;
  final int? width;
  final int? height;
  final int sortOrder;

  const PreparedAttachment({
    required this.fileName,
    required this.originalName,
    required this.fileSize,
    required this.sortOrder,
    this.width,
    this.height,
  });
}
