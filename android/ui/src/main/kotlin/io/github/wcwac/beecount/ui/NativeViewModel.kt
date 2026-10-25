package io.github.wcwac.beecount.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import io.github.wcwac.beecount.data.NativeRepository
import io.github.wcwac.beecount.domain.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
internal class NativeViewModel(val repository: NativeRepository, var platform: NativePlatformActions) : ViewModel() {
    val books = repository.observeBooks().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val accounts = repository.observeAccounts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val categories = repository.observeCategories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val tags = repository.observeTags().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val pending = repository.observePending().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val filter = MutableStateFlow(HistoryFilter())
    val history = filter.flatMapLatest(repository::history).cachedIn(viewModelScope)
    val busy = MutableStateFlow(false)
    val message = MutableStateFlow<String?>(null)

    fun run(success: String? = "已保存", after: () -> Unit = {}, operation: suspend () -> Unit) {
        if (busy.value) return
        val generation = repository.currentGeneration
        busy.value = true
        viewModelScope.launch {
            try {
                withContext(MutationGeneration(generation)) {
                    require(generation == repository.currentGeneration) { "数据已恢复，请重新打开当前页面" }
                    operation()
                    require(generation == repository.currentGeneration) { "数据已恢复，请重新打开当前页面" }
                }
                if (success != null) message.value = success
                after()
            }
            catch (error: Exception) { if (error is kotlinx.coroutines.CancellationException) throw error; message.value = error.message ?: "操作失败，请重试" }
            finally { busy.value = false }
        }
    }
    fun commit(command: FinancialCommand, after: () -> Unit = {}) = run(after = after) {
        when (val result = repository.execute(command)) {
            is CommitResult.Success -> Unit
            is CommitResult.Rejected -> throw IllegalArgumentException(result.reason)
        }
    }
    fun preferences(transform: (UserPreferences) -> UserPreferences) = run(success = null) { platform.updatePreferences(transform) }

    companion object {
        fun factory(repository: NativeRepository, platform: NativePlatformActions): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST") override fun <T : ViewModel> create(modelClass: Class<T>): T = NativeViewModel(repository, platform) as T
        }
    }
}
