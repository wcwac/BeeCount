package io.github.wcwac.beecount.capture.rules

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class RuleTextCompatibilityRegressionTest {
    @Test fun javaStringOperationsPreserveLiteralAndRegexBoundaries() {
        assertEquals(1.25, Amounts.S("1,25"), 0.0)
        // CharSequence arguments force the compatibility overload; the same-package
        // String arguments below also follow the generated rules' original call shape.
        val currency: CharSequence = StringBuilder("¥")
        val empty: CharSequence = StringBuilder()
        assertEquals("12.30", "¥12.30".replace(currency, empty))
        assertEquals("a\$1\\b\$1\\", "a.*b.*".replace(StringBuilder(".*"), StringBuilder("\$1\\")))
        assertEquals("|a|b|", "ab".replace("", "|"))
        assertEquals("ba", "aaa".replace("aa", "b"))

        // String.split(regex) drops trailing empty fields; a zero-width match at
        // the beginning does not introduce an empty first field in Java.
        assertArrayEquals(arrayOf("", "a", "", "b"), javaSplit(":a::b::", ":"))
        assertArrayEquals(arrayOf("abc"), javaSplit("abc", "(?=a)"))
        assertArrayEquals(arrayOf(""), javaSplit("", ":"))

        // Unlike the literal overload, Java replaceFirst interprets groups and escapes.
        assertEquals("fee=12; fee=¥34", replaceFirstRegex("fee=¥12; fee=¥34", "¥(\\d+)", "\$1"))
        assertEquals("fee=\$1", replaceFirstRegex("fee=12", "(\\d+)", "\\\$1"))
    }
}
