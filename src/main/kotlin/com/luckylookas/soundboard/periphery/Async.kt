package com.luckylookas.soundboard.periphery

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component

@Component
class Async(val dispatcher: CoroutineDispatcher = Dispatchers.IO) {
    fun dispatch(run: Runnable) {
        CoroutineScope(dispatcher).launch {
            run.run()
        }
    }
}