package matt.json.test


import matt.json.custom.int
import matt.json.prim.parseJsonObj
import matt.test.JupiterTestAssertions.assertRunsInOneMinute
import kotlin.test.Test
import kotlin.test.assertEquals

class JsonTests {
    @Test
    fun readJson() = assertRunsInOneMinute {
        assertEquals(
            "{\"number\": 1}".parseJsonObj()["number"]!!.int,
            1
        )

    }
}