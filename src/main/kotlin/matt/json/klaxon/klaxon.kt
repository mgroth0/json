package matt.json.klaxon

/*slightly modified version of code I stole from klaxon library*/

import java.text.DecimalFormat

object Render {
  fun renderString(s: String) = StringBuilder().renderString(s).toString()

  fun escapeString(s: String): String {
	val result = StringBuilder().apply {
	  for (idx in 0 until s.length) {
		val ch = s[idx]
		when (ch) {
		  '"'      -> append("\\").append(ch)
		  //                    '\'' -> append("\\").append(ch)
		  '\\'     -> append(ch).append(ch)
		  '\n'     -> append("\\n")
		  '\r'     -> append("\\r")
		  '\t'     -> append("\\t")
		  '\b'     -> append("\\b")
		  '\u000c' -> append("\\f")
		  else     -> {
			if (isNotPrintableUnicode(ch)) {
			  append("\\u")
			  append(Integer.toHexString(ch.code).padStart(4, '0'))
			} else {
			  append(ch)
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

  private val decimalFormat = DecimalFormat("0.0####E0;-0.0####E0")
}
