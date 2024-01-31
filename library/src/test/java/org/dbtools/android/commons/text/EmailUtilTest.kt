package org.dbtools.android.commons.text

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test

class EmailUtilTest {
    @Test
    fun testValidEmails() {
        assertThat(EmailUtil.isValidEmailAddress("joe@somedomain.org")).isTrue()
        assertThat(EmailUtil.isValidEmailAddress("joe@somedomain.org")).isTrue()
        assertThat(EmailUtil.isValidEmailAddress("j@s.g")).isTrue()
        assertThat(EmailUtil.isValidEmailAddress("loooooooooooooooooooooooooonnnnnnnnnnnnnnnnnnnnnnnnnnggggggggggggggggggggggggggggg@somedomain.org")).isTrue()
    }

    @Test
    fun testInvalidValidEmails() {
        assertThat(EmailUtil.isValidEmailAddress("")).isFalse()
        assertThat(EmailUtil.isValidEmailAddress("j")).isFalse()
        assertThat(EmailUtil.isValidEmailAddress("@somedomain.org")).isFalse()
        assertThat(EmailUtil.isValidEmailAddress("joe")).isFalse()
        assertThat(EmailUtil.isValidEmailAddress("joe@somedomain")).isFalse()
        assertThat(EmailUtil.isValidEmailAddress("joe@.org")).isFalse()
    }
}