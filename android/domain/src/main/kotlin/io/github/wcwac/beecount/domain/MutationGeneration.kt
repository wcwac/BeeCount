package io.github.wcwac.beecount.domain

import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/** Captures the data generation when a user action begins, before any suspending reads. */
class MutationGeneration(val expectedGeneration: Long) : AbstractCoroutineContextElement(Key) {
    companion object Key : CoroutineContext.Key<MutationGeneration>
}
