package com.wangc.bill.reference;

import android.app.UiAutomation;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import androidx.test.platform.app.InstrumentationRegistry;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

/** Test-only command transport on the disposable CI emulator, never application code. */
public final class ReferenceShell {
    private ReferenceShell() {}

    public static String run(UiAutomation automation, String command) throws IOException {
        if (!"true".equals(InstrumentationRegistry.getArguments().getString("reference_cloud_only"))
                || Build.VERSION.SDK_INT != 36
                || !(Build.HARDWARE.equals("ranchu") || Build.HARDWARE.equals("goldfish"))
                || !Arrays.asList(Build.SUPPORTED_ABIS).contains("x86_64")) {
            throw new IllegalStateException("Shell fixtures require the disposable cloud emulator");
        }
        if (command == null || command.length() > 16384 || command.indexOf('\0') >= 0)
            throw new IllegalArgumentException("Invalid fixture command");
        String marker = "REFERENCE_COMMAND_EXIT_" + UUID.randomUUID().toString().replace("-", "") + ":";
        String script = "exec 2>&1\n{\n" + command + "\n} 2>&1\nreference_command_exit=$?\n"
                + "printf '\\n" + marker + "%s\\n' \"$reference_command_exit\"\n"
                + "exit \"$reference_command_exit\"\n";
        // UiAutomation's command transport need not parse shell quoting. Start
        // only the fixed platform shell, then send our own script through stdin.
        // RW result order is stdout-read, stdin-write; stderr is redirected above.
        ParcelFileDescriptor[] channels = automation.executeShellCommandRw("/system/bin/sh");
        try (InputStream input = new ParcelFileDescriptor.AutoCloseInputStream(channels[0]);
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            // Close stdin before reading: the shell must receive EOF and cannot
            // wait indefinitely for another line from the instrumentation owner.
            try (OutputStream sink = new ParcelFileDescriptor.AutoCloseOutputStream(channels[1])) {
                sink.write(script.getBytes(StandardCharsets.UTF_8));
            }
            byte[] buffer = new byte[4096]; int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
                if (output.size() > 1048576) throw new IOException("Fixture shell output exceeded its bound");
            }
            String text = output.toString(StandardCharsets.UTF_8.name());
            int boundary = text.lastIndexOf("\n" + marker);
            if (boundary < 0) throw new IOException("Fixture shell did not report an exit status");
            String status = text.substring(boundary + marker.length() + 1).trim();
            if (!status.matches("[0-9]{1,3}")) throw new IOException("Fixture shell returned an invalid exit status");
            String result = text.substring(0, boundary);
            int code = Integer.parseInt(status);
            if (code != 0) throw new IOException("Fixture shell exit " + code + ": " + result);
            return result;
        }
    }
}
