package com.olt.mor.common.database

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.observeOn
import com.badoo.reaktive.scheduler.ioScheduler
import com.badoo.reaktive.single.Single
import com.badoo.reaktive.single.flatMapObservable
import com.squareup.sqldelight.Query

internal fun <T: Any, R> Single<Query<T>>.observe(get: (Query<T>) -> R): Observable<R> =
    flatMapObservable { it.observed() }
        .observeOn(ioScheduler)
        .map(get)