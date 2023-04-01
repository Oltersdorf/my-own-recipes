package com.olt.mor.common.utils.store

import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class CoroutineExecutor <in Intent: Any, in State: Any, Message: Any>
    : CoroutineExecutor<Intent, Unit, State, Message, Nothing>()
{
    fun <T> listenToFlow(flow: Flow<T>, mapper: (T) -> Message) {
        scope.launch {
            flow.collectLatest { dispatch(mapper(it)) }
        }
    }

    fun ioTask(block: suspend CoroutineScope.() -> Unit) {
        scope.launch(Dispatchers.IO) {
            block()
        }
    }
}