package matt.json.klaxon

/*slightly modified version of code I stole from klaxon library

https://www.baeldung.com/kotlin/int-to-hex-string*/
fun Int.toHexString() = toString(16)

object Render {
    fun renderString(s: String) = StringBuilder().renderString(s).toString()

    fun escapeString(s: String): String {
        val result =
            StringBuilder().apply {
                for (element in s) {
                    when (element) {
                        '"'      -> append("\\").append(element)
                        '\\'     -> append(element).append(element)
                        '\n'     -> append("\\n")
                        '\r'     -> append("\\r")
                        '\t'     -> append("\\t")
                        '\b'     -> append("\\b")
                        '\u000c' -> append("\\f")
                        else     -> {
                            if (isNotPrintableUnicode(element)) {
                                append("\\u")
                                append(element.code.toHexString().padStart(4, '0'))
                            } else {
                                append(element)
                            }
                        }
                    }
                }
            }
        return result.toString()
    }

    private fun <A: Appendable> A.renderString(s: String): A {
        append("\"")
        append(escapeString(s))
        append("\"")
        return this
    }

    private fun isNotPrintableUnicode(c: Char): Boolean =
        c in '\u0000'..'\u001F' ||
            c in '\u007F'..'\u009F' ||
            c in '\u2000'..'\u20FF'
}


/*test edit*/
