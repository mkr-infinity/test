package com.mkrinfinity.autooptimiser.storage

import java.io.IOException
import java.io.InputStream
import java.security.MessageDigest
import java.util.concurrent.CancellationException

/** Includes bytes traversed by skip: providers need not implement seeking efficiently. */
class HashBudget(val maximumBytes: Long) {
    init { require(maximumBytes >= 0) }
    var consumedBytes: Long = 0
        private set
    val remainingBytes: Long get() = maximumBytes - consumedBytes

    internal fun allowance(requested: Int): Int {
        if (remainingBytes == 0L) throw HashBudgetExceededException()
        return minOf(requested.toLong(), remainingBytes).toInt()
    }

    internal fun consume(count: Long) { consumedBytes += count }
}

class HashBudgetExceededException : IOException("Hash byte budget exhausted; duplicates are not fully checked.")
data class FullContentHash(val sha256: String)
data class SampleContentHash(val sha256: String)

/** Streaming only; callers own the stream. Samples must NEVER become duplicate evidence. */
object ContentHasher {
    private const val BUFFER_SIZE = 8192
    const val SAMPLE_BYTES = 64 * 1024

    fun full(input: InputStream, expectedSize: Long, budget: HashBudget, checkCancelled: () -> Unit = {}): FullContentHash {
        require(expectedSize >= 0)
        val digest = MessageDigest.getInstance("SHA-256")
        readExactly(input, expectedSize, digest, budget, checkCancelled)
        checkCancelled()
        budget.allowance(1)
        val extra = input.read()
        if (extra != -1) {
            budget.consume(1)
            throw IOException("File grew while hashing")
        }
        checkCancelled()
        return FullContentHash(digest.hex())
    }

    /** Exact, non-overlapping head and tail, handling short reads and short/zero skips. */
    fun sample(input: InputStream, expectedSize: Long, budget: HashBudget, checkCancelled: () -> Unit = {}): SampleContentHash {
        require(expectedSize >= 0)
        val digest = MessageDigest.getInstance("SHA-256")
        val head = minOf(expectedSize, SAMPLE_BYTES.toLong())
        val tail = minOf(expectedSize - head, SAMPLE_BYTES.toLong())
        readExactly(input, head, digest, budget, checkCancelled)
        var gap = expectedSize - head - tail
        while (gap > 0) {
            checkCancelled()
            val requested = minOf(gap, budget.allowance(BUFFER_SIZE).toLong())
            val skipped = input.skip(requested)
            if (skipped < 0 || skipped > requested) throw IOException("Invalid stream skip result")
            if (skipped > 0) {
                budget.consume(skipped)
                gap -= skipped
            } else {
                checkCancelled()
                if (input.read() == -1) throw IOException("File ended while sampling")
                budget.consume(1)
                gap--
            }
        }
        readExactly(input, tail, digest, budget, checkCancelled)
        checkCancelled()
        return SampleContentHash(digest.hex())
    }

    private fun readExactly(input: InputStream, size: Long, digest: MessageDigest, budget: HashBudget, checkCancelled: () -> Unit) {
        val buffer = ByteArray(BUFFER_SIZE)
        var remaining = size
        while (remaining > 0) {
            checkCancelled()
            val count = input.read(buffer, 0, budget.allowance(minOf(remaining, buffer.size.toLong()).toInt()))
            when {
                count < 0 -> throw IOException("File ended before its reported size")
                count == 0 -> {
                    checkCancelled()
                    val byte = input.read()
                    if (byte == -1) throw IOException("File ended before its reported size")
                    digest.update(byte.toByte())
                    budget.consume(1)
                    remaining--
                }
                else -> {
                    digest.update(buffer, 0, count)
                    budget.consume(count.toLong())
                    remaining -= count
                }
            }
        }
        checkCancelled()
    }

    private fun MessageDigest.hex(): String = digest().joinToString("") { "%02x".format(it.toInt() and 0xff) }
}

interface HashSource {
    val id: String
    val sizeBytes: Long
    fun open(): InputStream
    fun isUnchanged(): Boolean
}

data class DuplicateVerification(val fullHashes: Map<String, String>, val failures: Map<String, String>)

/** Only successful full reads of unchanged sources are returned, never sample digests. */
object DuplicateVerifier {
    fun verify(
        sources: List<HashSource>,
        budget: HashBudget,
        fullHashThreshold: Long = 1024 * 1024,
        checkCancelled: () -> Unit = {}
    ): DuplicateVerification {
        require(fullHashThreshold >= 0)
        val full = linkedMapOf<String, String>()
        val failures = linkedMapOf<String, String>()
        fun <T> attempt(source: HashSource, hash: (InputStream) -> T): T? {
            checkCancelled()
            return try {
                if (!source.isUnchanged()) throw IOException("File metadata changed")
                val value = source.open().use(hash)
                checkCancelled()
                if (!source.isUnchanged()) throw IOException("File metadata changed")
                value
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: IOException) {
                failures[source.id] = failure.message ?: "Could not read file"
                null
            } catch (failure: SecurityException) {
                failures[source.id] = "Read permission denied"
                null
            }
        }
        val bySize = linkedMapOf<Long, MutableList<HashSource>>()
        val seen = hashSetOf<String>()
        sources.forEach { source ->
            checkCancelled()
            require(source.sizeBytes >= 0) { "Source size must be non-negative" }
            if (seen.add(source.id)) bySize.getOrPut(source.sizeBytes) { arrayListOf() }.add(source)
        }
        bySize.values.forEach { matchingSize ->
            checkCancelled()
            if (matchingSize.size < 2) return@forEach
            if (matchingSize.first().sizeBytes <= fullHashThreshold) {
                matchingSize.forEach { source ->
                    attempt(source) { ContentHasher.full(it, source.sizeBytes, budget, checkCancelled) }
                        ?.let { full[source.id] = it.sha256 }
                }
            } else {
                val samples = linkedMapOf<SampleContentHash, MutableList<HashSource>>()
                matchingSize.forEach { source ->
                    attempt(source) { ContentHasher.sample(it, source.sizeBytes, budget, checkCancelled) }
                        ?.let { samples.getOrPut(it) { arrayListOf() }.add(source) }
                }
                samples.values.forEach { matchingSample ->
                    checkCancelled()
                    if (matchingSample.size > 1) matchingSample.forEach { source ->
                        attempt(source) { ContentHasher.full(it, source.sizeBytes, budget, checkCancelled) }
                            ?.let { full[source.id] = it.sha256 }
                    }
                }
            }
        }
        return DuplicateVerification(full, failures)
    }
}
