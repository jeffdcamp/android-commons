package org.dbtools.android.commons.text

object EmailUtil {
    fun isValidEmailAddress(email: String): Boolean = EMAIL_ADDRESS.matches(email)

    private val EMAIL_ADDRESS: Regex = """[a-zA-Z0-9+._%\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\-]{0,64}(\.[a-zA-Z0-9][a-zA-Z0-9\-]{0,25})+""".toRegex()
}