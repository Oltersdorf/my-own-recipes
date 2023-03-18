package com.olt.mor.common.database

import com.badoo.reaktive.base.setCancellable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.observable
import com.squareup.sqldelight.Query

internal fun <T: Any> Query<T>.observed(): Observable<Query<T>> =
    observable { emitter ->
        val listener =
            object : Query.Listener {
                override fun queryResultsChanged() {
                    emitter.onNext(this@observed)
                }
            }

        emitter.onNext(this@observed)
        addListener(listener)
        emitter.setCancellable { removeListener(listener) }
    }