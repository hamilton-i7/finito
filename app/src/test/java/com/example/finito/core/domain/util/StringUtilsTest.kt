package com.example.finito.core.domain.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StringUtilsTest {

    @Test
    fun `should return string without accents`() {
        assertThat("BóäRd".normalize()).isEqualTo("board")
        assertThat("åé".normalize()).isEqualTo("ae")
        assertThat("São Paulo".normalize()).isEqualTo("sao paulo")
        assertThat("español".normalize()).isEqualTo("espanol")
        assertThat("nueva película😎".normalize()).isEqualTo("nueva pelicula😎")
        assertThat("çereza".normalize()).isEqualTo("cereza")
        assertThat("ALL CAPS NAME".normalize()).isEqualTo("all caps name")
    }
}