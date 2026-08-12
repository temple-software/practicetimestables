package com.templesoftware.practicetimestables.ui.repeat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class RepeatCarouselTest {
    @Test
    fun derivesInstructionWindow() {
        val window = repeatCarouselWindow(0)
        assertNull(window.previous)
        assertEquals(RepeatCarouselItem.Instruction, window.current)
        assertEquals(RepeatCarouselItem.Fact(1), window.next)
    }

    @Test
    fun derivesFirstFactWindow() {
        assertEquals(
            RepeatCarouselWindow(
                RepeatCarouselItem.Instruction,
                RepeatCarouselItem.Fact(1),
                RepeatCarouselItem.Fact(2),
            ),
            repeatCarouselWindow(1),
        )
    }

    @Test
    fun derivesMiddleFactWindow() {
        assertEquals(
            RepeatCarouselWindow(
                RepeatCarouselItem.Fact(6),
                RepeatCarouselItem.Fact(7),
                RepeatCarouselItem.Fact(8),
            ),
            repeatCarouselWindow(7),
        )
    }

    @Test
    fun derivesFinalFactWindowWithoutNextItem() {
        val window = repeatCarouselWindow(12)
        assertEquals(RepeatCarouselItem.Fact(11), window.previous)
        assertEquals(RepeatCarouselItem.Fact(12), window.current)
        assertNull(window.next)
    }

    @Test
    fun rejectsEveryIndexOutsideFiniteSequence() {
        assertThrows(IllegalArgumentException::class.java) { repeatCarouselWindow(-1) }
        assertThrows(IllegalArgumentException::class.java) { repeatCarouselWindow(13) }
    }
}
