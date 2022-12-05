package com.journiapp.stampapplication

import okhttp3.Headers
import org.junit.Assert
import org.junit.Test

class SessionCookieUnitTest {

    @Test
    fun `Session cookie is returned`() {
        val sessionCookie = "session=ABCDE"
        val headers = Headers.headersOf("Set-Cookie", sessionCookie)
        val actualSessionCookie = NetworkUtil.getCookieString(headers)
        Assert.assertEquals(sessionCookie, actualSessionCookie)
    }

    /**
     * TODO: Out network layer should only use `session` cookies, but there is currently a bug where
     *  any kind of cookie is accepted. Fix the `NetworkUtil.getCookieString` to only return
     *  `session` cookies.
     */
    @Test
    fun `Other non session cookies are not returned`() {
        val nonSessionCookie = "NonSessionCookie=XYZ"
        val headers = Headers.headersOf("Set-Cookie", nonSessionCookie)
        val actualSessionCookie = NetworkUtil.getCookieString(headers)
        Assert.assertNull(actualSessionCookie)
    }
}