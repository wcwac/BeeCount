"""Run a CI command and expose its failure tail in GitHub check annotations."""

from collections import deque
from queue import Empty, Queue
from pathlib import Path
import os
import subprocess
import sys
import re
from threading import Thread


def escape(value):
    return value.replace("%", "%25").replace("\r", "%0D").replace("\n", "%0A")


def main():
    if len(sys.argv) < 2:
        raise SystemExit("Expected a command")
    tail = deque(maxlen=180)
    diagnostics = deque(maxlen=200)
    exception_head = None
    log_directory = Path(os.environ['RUNNER_TEMP']) / 'validation-logs'
    log_directory.mkdir(parents=True, exist_ok=True)
    log_name = re.sub(r'[^A-Za-z0-9_.-]', '_', os.environ.get('GITHUB_ACTION', 'command'))
    log = (log_directory / f'{log_name}.log').open('w', buffering=1)
    log.write('Command: ' + repr(sys.argv[1:]) + '\n')
    # Commands and their arguments come from the checked-in workflow, not user text.
    process = subprocess.Popen(
        sys.argv[1:], stdout=subprocess.PIPE, stderr=subprocess.STDOUT,
        text=True, errors="replace", bufsize=1,
    )
    output = Queue(maxsize=64)
    def read_output():
        for line in process.stdout:
            output.put(line)
        output.put(None)
    Thread(target=read_output, daemon=True).start()
    while True:
        try:
            line = output.get(timeout=60)
        except Empty:
            message = "No new output for 60 seconds. Last output:\n" + "\n".join(list(tail)[-8:])
            print(f"::notice title=Command progress::{escape(message)}", flush=True)
            continue
        if line is None:
            break
        print(line, end="", flush=True)
        log.write(line)
        tail.append(line.rstrip("\n"))
        if "EXCEPTION CAUGHT BY FLUTTER TEST FRAMEWORK" in line:
            exception_head = []
        if exception_head is not None and len(exception_head) < 30:
            exception_head.append(line.rstrip("\n"))
            if len(exception_head) == 25:
                message = "\n".join(exception_head)
                print(f"::notice title=Flutter exception diagnostic::{escape(message)}", flush=True)
        if re.search(r"\berror\s*(?:•|-)|Error:|^e: |\[E\]|FAILED|Exception:|\[(?:ERROR|SEVERE)\]", line):
            diagnostics.append(line.rstrip("\n"))
        if "[E]" in line:
            message = "\n".join((exception_head or []) + list(tail)[-15:])
            print(f"::notice title=Test diagnostic::{escape(message)}", flush=True)
    result = process.wait()
    log.close()
    if result:
        lines = list(diagnostics) + ["--- Last output ---"] + list(tail)[-60:]
        for start in range(0, len(lines), 20):
            message = "\n".join(lines[start:start + 20])
            print(f"::error title=Command failed (exit {result})::{escape(message)}")
    else:
        message = "\n".join(list(tail)[-8:])
        print(f"::notice title=Command succeeded::{escape(message)}")
    return result


if __name__ == "__main__":
    sys.exit(main())
