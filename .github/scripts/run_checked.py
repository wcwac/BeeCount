"""Run a CI command and expose its failure tail in GitHub check annotations."""

from collections import deque
from queue import Empty, Queue
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
        tail.append(line.rstrip("\n"))
        if re.search(r"error\s*•|Error:|^e: |\[E\]|FAILED|Exception:", line):
            diagnostics.append(line.rstrip("\n"))
        if "[E]" in line:
            message = "\n".join(list(tail)[-35:])
            print(f"::notice title=Test diagnostic::{escape(message)}", flush=True)
    result = process.wait()
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
