package com.mkrinfinity.autooptimiser

import com.mkrinfinity.autooptimiser.storage.ContentHasher
import com.mkrinfinity.autooptimiser.storage.DuplicateDetector
import com.mkrinfinity.autooptimiser.storage.DuplicateVerifier
import com.mkrinfinity.autooptimiser.storage.HashBudget
import com.mkrinfinity.autooptimiser.storage.HashBudgetExceededException
import com.mkrinfinity.autooptimiser.storage.HashSource
import com.mkrinfinity.autooptimiser.storage.ScannedFile
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest
import java.util.concurrent.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/** No Android mocks, disk files, clocks, or provider assumptions. */
class ContentHasherTest {
    @Test
    fun fullHashMatchesKnownSha256AndCountsBytes() {
        val budget = HashBudget(4)
        val result = ContentHasher.full(shortStream("abc".toByteArray()), 3, budget)
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad", result.sha256)
        assertEquals(3L, budget.consumedBytes)
    }

    @Test
    fun emptyFilesAreFullyReadAndHashable() {
        val result = ContentHasher.full(ByteArrayInputStream(byteArrayOf()), 0, HashBudget(1))
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855", result.sha256)
    }

    @Test
    fun truncatedAndGrowingStreamsAreRejected() {
        assertFailsWith<IOException> { ContentHasher.full(shortStream(byteArrayOf(1, 2)), 3, HashBudget(100)) }
        assertFailsWith<IOException> { ContentHasher.full(shortStream(byteArrayOf(1, 2, 3)), 2, HashBudget(100)) }
    }

    @Test
    fun zeroLengthBulkReadsMakeProgressWithoutMistakingThemForEof() {
        val input = object : ByteArrayInputStream("abc".toByteArray()) {
            override fun read(buffer: ByteArray, offset: Int, length: Int): Int = 0
        }
        assertEquals(sha("abc".toByteArray()), ContentHasher.full(input, 3, HashBudget(4)).sha256)
    }

    @Test
    fun sampleUsesExactHeadAndTailWithShortReadsAndShortOrZeroSkips() {
        val bytes = data(ContentHasher.SAMPLE_BYTES * 2 + 29)
        val expected = bytes.copyOfRange(0, ContentHasher.SAMPLE_BYTES) + bytes.copyOfRange(bytes.size - ContentHasher.SAMPLE_BYTES, bytes.size)
        val budget = HashBudget(bytes.size.toLong())
        val result = ContentHasher.sample(shortStream(bytes), bytes.size.toLong(), budget)
        assertEquals(sha(expected), result.sha256)
        assertEquals(bytes.size.toLong(), budget.consumedBytes)
    }

    @Test
    fun smallSamplesNeverOverlapHeadAndTail() {
        listOf(0, 3, ContentHasher.SAMPLE_BYTES, ContentHasher.SAMPLE_BYTES + 7).forEach { size ->
            val bytes = data(size)
            assertEquals(sha(bytes), ContentHasher.sample(shortStream(bytes), size.toLong(), HashBudget(size.toLong())).sha256)
        }
    }

    @Test
    fun budgetBoundsReadsAndSkipTraversal() {
        val budget = HashBudget(7)
        assertFailsWith<HashBudgetExceededException> {
            ContentHasher.full(shortStream(data(50)), 50, budget)
        }
        assertEquals(7L, budget.consumedBytes)
        val sampleBudget = HashBudget(ContentHasher.SAMPLE_BYTES + 4L)
        assertFailsWith<HashBudgetExceededException> {
            ContentHasher.sample(shortStream(data(ContentHasher.SAMPLE_BYTES * 3)), ContentHasher.SAMPLE_BYTES * 3L, sampleBudget)
        }
        assertEquals(ContentHasher.SAMPLE_BYTES + 4L, sampleBudget.consumedBytes)
    }

    @Test
    fun fullReadChecksCancellationInsideLoop() {
        var checks = 0
        val budget = HashBudget(1000)
        assertFailsWith<CancellationException> {
            ContentHasher.full(shortStream(data(100)), 100, budget) {
                if (++checks == 3) throw CancellationException("cancel")
            }
        }
        assertTrue(budget.consumedBytes < 100)
    }

    @Test
    fun sampleChecksCancellationInsideSkipLoop() {
        val size = ContentHasher.SAMPLE_BYTES * 3
        val budget = HashBudget(size.toLong())
        assertFailsWith<CancellationException> {
            ContentHasher.sample(shortStream(data(size)), size.toLong(), budget) {
                if (budget.consumedBytes > ContentHasher.SAMPLE_BYTES) throw CancellationException("cancel")
            }
        }
        assertTrue(budget.consumedBytes < size)
    }

    @Test
    fun matchingSamplesWithDifferentMiddlesAreNotDuplicates() {
        val a = data(ContentHasher.SAMPLE_BYTES * 2 + 15)
        val b = a.copyOf().apply { this[ContentHasher.SAMPLE_BYTES + 2] = (this[ContentHasher.SAMPLE_BYTES + 2].toInt() xor 127).toByte() }
        assertEquals(
            ContentHasher.sample(shortStream(a), a.size.toLong(), HashBudget(a.size.toLong())),
            ContentHasher.sample(shortStream(b), b.size.toLong(), HashBudget(b.size.toLong()))
        )
        val sources = listOf(MemorySource("a", a), MemorySource("b", b))
        val result = DuplicateVerifier.verify(sources, HashBudget(a.size * 4L + 1), fullHashThreshold = 0)
        assertNotEquals(result.fullHashes["a"], result.fullHashes["b"])
        assertTrue(DuplicateDetector.findGroups(sources.map { ScannedFile(it.id, it.sizeBytes, 1, result.fullHashes[it.id]) }).isEmpty())
        assertTrue(result.failures.isEmpty())
        assertTrue(sources.all { it.openCount == 2 && it.closeCount == 2 })
    }

    @Test
    fun matchingSamplesAreNeverPublishedWhenFullVerificationRunsOutOfBudget() {
        val bytes = data(ContentHasher.SAMPLE_BYTES * 2 + 15)
        val sources = listOf(MemorySource("a", bytes), MemorySource("b", bytes))
        val result = DuplicateVerifier.verify(sources, HashBudget(bytes.size * 2L + 1), fullHashThreshold = 0)
        assertTrue(result.fullHashes.isEmpty())
        assertEquals(setOf("a", "b"), result.failures.keys)
        assertTrue(sources.all { it.openCount == it.closeCount })
    }

    @Test
    fun unmatchedSamplesAreNotPublishedAsContentHashes() {
        val sources = listOf(MemorySource("a", byteArrayOf(1)), MemorySource("b", byteArrayOf(2)))
        val result = DuplicateVerifier.verify(sources, HashBudget(100), fullHashThreshold = 0)
        assertTrue(result.fullHashes.isEmpty())
        assertTrue(result.failures.isEmpty())
        assertTrue(sources.all { it.openCount == 1 })
    }

    @Test
    fun fullVerifiedCopiesPreserveOneCanonicalAndCountOnlyExtraBytes() {
        val bytes = data(23)
        val sources = listOf(MemorySource("b", bytes), MemorySource("a", bytes), MemorySource("c", bytes))
        val result = DuplicateVerifier.verify(sources, HashBudget(1000), fullHashThreshold = 0)
        val group = DuplicateDetector.findGroups(sources.map { ScannedFile(it.id, it.sizeBytes, 1, result.fullHashes[it.id]) }).single()
        assertEquals("a", group.canonicalFile.path)
        assertEquals(listOf("b", "c"), group.duplicateFiles.map { it.path })
        assertEquals(46L, group.reclaimableBytes)
    }

    @Test
    fun metadataChangesAfterReadDiscardHash() {
        var metadataChecks = 0
        val changed = MemorySource("a", data(3), unchanged = { ++metadataChecks == 1 })
        val result = DuplicateVerifier.verify(listOf(changed, MemorySource("b", data(3))), HashBudget(100))
        assertTrue("a" !in result.fullHashes)
        assertTrue("a" in result.failures)
        assertEquals(1, changed.closeCount)
    }

    @Test
    fun cancelledSourceClosesStreamAndCancellationEscapesVerifier() {
        val cancelled = MemorySource("a", data(100))
        var checks = 0
        assertFailsWith<CancellationException> {
            DuplicateVerifier.verify(listOf(cancelled, MemorySource("b", data(100))), HashBudget(1000)) {
                if (cancelled.openCount > 0 && ++checks > 2) throw CancellationException("cancel")
            }
        }
        assertEquals(1, cancelled.openCount)
        assertEquals(1, cancelled.closeCount)
    }

    @Test
    fun unreadableSourceDoesNotFabricateHashOrStopOtherCandidates() {
        val denied = object : HashSource {
            override val id = "denied"
            override val sizeBytes = 3L
            override fun isUnchanged() = true
            override fun open(): InputStream = throw SecurityException("revoked")
        }
        val result = DuplicateVerifier.verify(listOf(denied, MemorySource("b", data(3))), HashBudget(100))
        assertEquals(setOf("denied"), result.failures.keys)
        assertEquals(setOf("b"), result.fullHashes.keys)
    }

    @Test
    fun uniqueSizesAndRepeatedIdentitiesAreNotHashed() {
        val a = MemorySource("a", data(1))
        val b = MemorySource("b", data(2))
        val result = DuplicateVerifier.verify(listOf(a, a, b), HashBudget(100))
        assertTrue(result.fullHashes.isEmpty())
        assertEquals(0, a.openCount)
        assertEquals(0, b.openCount)
    }

    private class MemorySource(override val id: String, val bytes: ByteArray, val unchanged: () -> Boolean = { true }) : HashSource {
        override val sizeBytes = bytes.size.toLong()
        var openCount = 0
        var closeCount = 0
        override fun isUnchanged() = unchanged()
        override fun open(): InputStream {
            openCount++
            return object : ByteArrayInputStream(bytes) {
                override fun read(buffer: ByteArray, offset: Int, length: Int): Int = super.read(buffer, offset, minOf(length, 3))
                override fun close() { closeCount++; super.close() }
            }
        }
    }

    private fun shortStream(bytes: ByteArray): InputStream = object : ByteArrayInputStream(bytes) {
        var skips = 0
        override fun read(buffer: ByteArray, offset: Int, length: Int): Int = super.read(buffer, offset, minOf(length, 3))
        override fun skip(count: Long): Long = if (++skips % 2 == 0) 0 else super.skip(minOf(count, 2))
    }

    private fun data(size: Int): ByteArray = ByteArray(size) { (it * 31 + it / 251).toByte() }
    private fun sha(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it.toInt() and 255) }
}
