"""Collect and validate cloud screen artifacts as data; never execute their contents."""
import argparse
import hashlib
import json
import os
from pathlib import Path, PurePosixPath
import shutil
import stat
import struct
import tarfile
import zlib

MAX_BYTES = 160 * 1024 * 1024
MAX_FILES = 256
MAX_PNG_BYTES = 6 * 1024 * 1024
MAX_DECODED_PNG_BYTES = 32 * 1024 * 1024


def extract(archive, destination):
    assert not any(path.is_symlink() for path in (destination, *destination.parents)), 'Evidence destination contains a symbolic link'
    destination.mkdir(parents=True, exist_ok=True)
    root = destination.resolve(strict=True)
    total, seen = 0, set()
    # No-follow directory-relative opens keep existing or replaced run folders
    # from redirecting writes outside the trusted extraction destination.
    root_fd = os.open(destination, os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW)
    try:
        with tarfile.open(archive, 'r:') as stream:
            for number, entry in enumerate(stream, 1):
                assert number <= MAX_FILES + 4, 'Too many cloud evidence entries'
                path = PurePosixPath(entry.name)
                assert not path.is_absolute() and '..' not in path.parts, 'Invalid evidence path'
                normalized = path.as_posix()
                assert normalized not in seen, 'Duplicate normalized evidence member: ' + normalized
                seen.add(normalized)
                if entry.isdir():
                    assert not path.parts or (len(path.parts) == 1 and path.parts[0].startswith('run-')), 'Unexpected evidence directory'
                    if path.parts:
                        assert not (root / path.parts[0]).is_symlink(), 'Evidence run folder is a symbolic link'
                    continue
                assert entry.isfile(), 'Evidence must contain only regular files'
                assert len(path.parts) == 2 and path.parts[0].startswith('run-'), 'Unexpected evidence folder'
                assert path.suffix in {'.json', '.jsonl', '.png'}, 'Unexpected evidence file'
                assert 0 <= entry.size <= MAX_PNG_BYTES, 'Evidence file exceeds its size budget'
                total += entry.size
                assert total <= MAX_BYTES, 'Evidence archive exceeds its size budget'
                try:
                    os.mkdir(path.parts[0], mode=0o700, dir_fd=root_fd)
                except FileExistsError:
                    pass
                run_fd = os.open(path.parts[0], os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW, dir_fd=root_fd)
                try:
                    filename = path.parts[1]
                    try:
                        previous = os.stat(filename, dir_fd=run_fd, follow_symlinks=False)
                    except FileNotFoundError:
                        previous = None
                    if previous is not None:
                        assert stat.S_ISREG(previous.st_mode), 'Existing evidence target is not a regular file'
                        # Replace the inode rather than truncate a possible hard link.
                        os.unlink(filename, dir_fd=run_fd)
                    target_fd = os.open(filename, os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW, 0o600, dir_fd=run_fd)
                    with os.fdopen(target_fd, 'wb') as output, stream.extractfile(entry) as source:
                        shutil.copyfileobj(source, output)
                finally:
                    os.close(run_fd)
    finally:
        os.close(root_fd)


def png_dimensions(raw):
    assert 32 <= len(raw) <= MAX_PNG_BYTES and raw[:8] == b'\x89PNG\r\n\x1a\n', 'Invalid screenshot PNG signature or size'
    position, header, palette, data, idat_closed, seen_idat = 8, None, None, bytearray(), False, False
    ended = False
    while position < len(raw):
        assert position + 12 <= len(raw), 'Truncated PNG chunk header'
        length = struct.unpack_from('>I', raw, position)[0]
        kind = raw[position + 4:position + 8]
        end = position + 12 + length
        assert length <= MAX_PNG_BYTES and end <= len(raw), 'PNG chunk exceeds its data boundary'
        assert len(kind) == 4 and all(65 <= value <= 90 or 97 <= value <= 122 for value in kind), 'Invalid PNG chunk type'
        payload = raw[position + 8:position + 8 + length]
        expected_crc = struct.unpack_from('>I', raw, position + 8 + length)[0]
        assert zlib.crc32(payload, zlib.crc32(kind)) & 0xffffffff == expected_crc, 'PNG chunk CRC mismatch'
        if header is None:
            assert kind == b'IHDR' and length == 13, 'PNG must start with one IHDR'
            header = struct.unpack('>IIBBBBB', payload)
        elif kind == b'IHDR':
            raise AssertionError('Duplicate PNG IHDR')
        elif kind == b'PLTE':
            assert palette is None and not seen_idat and 0 < length <= 768 and length % 3 == 0, 'Invalid PNG palette'
            palette = length // 3
        elif kind == b'IDAT':
            assert not idat_closed and len(data) + length <= MAX_PNG_BYTES, 'PNG IDAT must be consecutive and bounded'
            seen_idat = True
            data.extend(payload)
        elif kind == b'IEND':
            assert length == 0 and data and end == len(raw), 'PNG must end after nonempty IDAT data'
            ended = True
            break
        else:
            assert kind[0] & 32, 'Unknown critical PNG chunk'
        if kind not in (b'IDAT', b'IHDR') and seen_idat:
            idat_closed = True
        position = end
    assert header is not None and ended, 'Incomplete PNG screenshot'
    width, height, depth, color, compression, filtering, interlace = header
    assert 0 < min(width, height) <= max(width, height) <= 1600
    channels = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}
    depths = {0: {1, 2, 4, 8, 16}, 2: {8, 16}, 3: {1, 2, 4, 8}, 4: {8, 16}, 6: {8, 16}}
    assert color in channels and depth in depths[color] and compression == filtering == 0 and interlace in (0, 1), 'Unsupported PNG header'
    if color == 3:
        assert palette is not None and palette <= 1 << depth, 'Indexed PNG needs a valid palette'
    elif color in (0, 4):
        assert palette is None, 'Grayscale PNG cannot carry a palette'
    passes = [(0, 0, 1, 1)] if interlace == 0 else [(0, 0, 8, 8), (4, 0, 8, 8), (0, 4, 4, 8), (2, 0, 4, 4), (0, 2, 2, 4), (1, 0, 2, 2), (0, 1, 1, 2)]
    rows = []
    for x, y, dx, dy in passes:
        columns = max(0, (width - x + dx - 1) // dx)
        count = max(0, (height - y + dy - 1) // dy)
        if columns and count:
            row_bytes = (columns * channels[color] * depth + 7) // 8
            rows.extend([row_bytes] * count)
    expected_bytes = sum(length + 1 for length in rows)
    assert expected_bytes <= MAX_DECODED_PNG_BYTES
    decoder = zlib.decompressobj()
    decoded = decoder.decompress(data, expected_bytes + 1)
    assert len(decoded) == expected_bytes and decoder.eof and not decoder.unused_data and not decoder.unconsumed_tail, 'Invalid PNG image stream or decompressed size'
    offset = 0
    for length in rows:
        assert decoded[offset] <= 4, 'Invalid PNG row filter'
        offset += length + 1
    return width, height


def verify(folder, inventory):
    assert not any(path.is_symlink() for path in (folder, *folder.parents)), 'Evidence folder contains a symbolic link'
    specification = json.loads(inventory.read_text())
    expected = {(test, scene) for test, scenes in specification['screen_checkpoints'].items() for scene in scenes}
    expected_tests = set(specification['screen_checkpoints'])
    runs = sorted(folder.glob('run-*/index.jsonl'))
    assert len(runs) == 1, 'Expected evidence from exactly one instrumentation process'
    root = runs[0].parent
    assert not root.is_symlink() and not runs[0].is_symlink(), 'Evidence run or index is a symbolic link'
    files = list(root.iterdir())
    assert all(not path.is_symlink() and path.is_file() for path in files), 'Evidence run must contain only regular files'
    info = json.loads((root / 'run-info.json').read_text())
    assert info['schema_version'] == 1 and info['package'] == 'io.github.wcwac.yimu.reference.debug'
    assert info['sdk'] == 36 and 'x86_64' in info['abis']
    assert info['synthetic_cloud_data_only'] is True
    assert info['original_apk_executed'] is False and info['pixel_parity_verified'] is False
    assert info['max_images'] == 64 and info['max_total_bytes'] == 128 * 1024 * 1024
    assert len(files) <= MAX_FILES and sum(path.stat().st_size for path in files) <= info['max_total_bytes']
    seen, started, finished, unshown = set(), set(), set(), {}
    images, records = [], []
    events = [json.loads(line) for line in runs[0].read_text().splitlines() if line.strip()]
    for event in events:
        name, test = event['event'], event.get('test')
        if name == 'test_started':
            assert test in expected_tests and test not in started
            started.add(test)
        elif name == 'test_finished':
            assert test in started and test not in finished and event['outcome'] == 'PASSED', 'Screen case failed or was unfinished: ' + str(test)
            finished.add(test)
        elif name == 'coverage_not_displayed':
            assert test not in unshown and event['status'] == 'INFLATED_ONLY'
            unshown[test] = event['templates']
        elif name == 'CHECKPOINT':
            pair = (test, event['scene'])
            assert pair in expected and pair not in seen, 'Unexpected or duplicate screen checkpoint: ' + str(pair)
            assert test in started and test not in finished
            assert event['status'] == 'CAPTURED' and event['coverage'] == 'ENTRY_RENDER_ONLY'
            assert event['attached'] is True and event['focused'] is True
            assert event['synthetic_cloud_data_only'] is True and event['original_apk_executed'] is False and event['pixel_parity_verified'] is False
            screenshot, tree = event['screenshot'], event['tree']
            assert Path(screenshot).name == screenshot and Path(tree).name == tree
            assert screenshot.endswith('.png') and tree == screenshot[:-4] + '.tree.json'
            image = root / screenshot
            metadata = root / (screenshot[:-4] + '.json')
            assert json.loads(metadata.read_text()) == event, 'Saved screenshot metadata differs from its index'
            raw = image.read_bytes()
            width, height = png_dimensions(raw)
            assert width == event['width'] and height == event['height'] and 0 < min(width, height) <= max(width, height) <= 1600
            hierarchy = json.loads((root / tree).read_text())
            assert hierarchy['tree_kind'] == 'ACTUAL_ANDROID_VIEW_HIERARCHY' and hierarchy['activity'] == event['activity']
            assert hierarchy['visited'] == len(hierarchy['nodes']) == event['tree_nodes'] and 0 < hierarchy['visited'] <= 1500
            assert hierarchy['truncated'] == event['tree_truncated']
            for position, node in enumerate(hierarchy['nodes']):
                assert node['index'] == position and 0 <= node['depth'] <= 40
                if position == 0:
                    assert node['parent'] == -1 and node['depth'] == 0
                else:
                    assert 0 <= node['parent'] < position
                    assert hierarchy['nodes'][node['parent']]['depth'] + 1 == node['depth']
            assert event['uncovered_states'], 'Screen evidence must state its incomplete coverage'
            seen.add(pair)
            images.append(screenshot)
            records.append({'test': test, 'scene': pair[1], 'screenshot': screenshot, 'tree': tree, 'sha256': hashlib.sha256(raw).hexdigest(), 'tree_truncated': hierarchy['truncated']})
        else:
            raise AssertionError('Unsuccessful or unknown screen evidence event: ' + str(name))
    assert seen == expected, 'Missing screen checkpoints: ' + str(sorted(expected - seen))
    assert started == finished == expected_tests, 'Not all screen cases completed successfully'
    assert unshown == specification['screen_unshown_templates'], 'Unattached template coverage was not reported honestly'
    assert len(images) == len(set(images)) == len(expected)
    assert {path.name for path in root.glob('*.png')} == set(images), 'Unindexed screenshot artifact'
    result = {'schema_version': 1, 'verified_checkpoints': len(seen), 'verified_screen_cases': len(finished), 'original_apk_executed': False, 'pixel_parity_verified': False,
              'scope': 'Entry rendering only; no save/cancel, hidden dialogs, offscreen pixels, or original-app equivalence claim.', 'checkpoints': records}
    folder_fd = os.open(folder, os.O_RDONLY | os.O_DIRECTORY | os.O_NOFOLLOW)
    try:
        summary_name = 'verified-summary.json'
        try:
            previous = os.stat(summary_name, dir_fd=folder_fd, follow_symlinks=False)
        except FileNotFoundError:
            previous = None
        if previous is not None:
            assert stat.S_ISREG(previous.st_mode), 'Existing evidence summary is not a regular file'
            # Do not truncate an existing inode, which could be a hard link.
            os.unlink(summary_name, dir_fd=folder_fd)
        summary_fd = os.open(summary_name, os.O_WRONLY | os.O_CREAT | os.O_EXCL | os.O_NOFOLLOW, 0o600, dir_fd=folder_fd)
        with os.fdopen(summary_fd, 'w', encoding='utf-8') as output:
            output.write(json.dumps(result, ensure_ascii=False, indent=2) + '\n')
    finally:
        os.close(folder_fd)
    print('Verified real screen checkpoints:', len(seen), 'in', len(finished), 'screen cases')


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--extract', type=Path)
    parser.add_argument('--destination', type=Path, required=True)
    parser.add_argument('--inventory', type=Path)
    arguments = parser.parse_args()
    if arguments.extract:
        extract(arguments.extract, arguments.destination)
    else:
        assert arguments.inventory is not None
        verify(arguments.destination, arguments.inventory)
