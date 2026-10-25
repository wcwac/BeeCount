package io.github.wcwac.beecount.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface CaptureInbox {
    suspend fun upsert(proposal: CaptureProposal): CaptureUpsertResult
    fun observePending(): Flow<List<CaptureCandidate>>
    suspend fun candidate(id: String): CaptureCandidate?
    suspend fun dismiss(id: String, revision: Long)
    suspend fun enrichRemark(id: String, remark: String, expectedDescription: String? = null)
}

/** Implemented by the application container; never exposes an Activity to capture. */
interface CaptureHost {
    suspend fun awaitReady()
    val captureConfig: StateFlow<CaptureConfig>
    val captureInbox: CaptureInbox
    val captureGeneration: Long
    fun onCaptureStatus(status: CaptureStatus)
    fun onCandidateAvailable(candidate: CaptureCandidate)
}

interface CaptureHostProvider { val captureHost: CaptureHost }
