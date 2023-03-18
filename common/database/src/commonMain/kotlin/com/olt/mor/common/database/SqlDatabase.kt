package com.olt.mor.common.database

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.maybe.Maybe
import com.badoo.reaktive.observable.*
import com.badoo.reaktive.scheduler.ioScheduler
import com.badoo.reaktive.single.*
import com.squareup.sqldelight.Query
import com.squareup.sqldelight.Transacter

abstract class SqlDatabase<Database: Transacter>(private val database: Single<Database>) {

    constructor(database: Database) : this(singleOf(database))

    protected fun <Query: Transacter> databaseQuery(mapper: (Database) -> Query): Single<Query> =
        database
            .map(mapper)
            .asObservable()
            .replay()
            .autoConnect()
            .firstOrError()

    protected fun <T: Any, Q: Transacter> queryObservable(query: Single<Q>, queryMapper: (Q) -> Query<T>): Observable<List<T>> =
        query
            .observeOn(ioScheduler)
            .map(queryMapper)
            .observe { it.executeAsList() }

    protected fun <T: Any, Q: Transacter> queryMaybe(query: Single<Q>, queryMapper: (Q) -> T?): Maybe<T> =
        query
            .observeOn(ioScheduler)
            .mapNotNull(queryMapper)

    protected fun <Q: Transacter> execute(query: Single<Q>, queryExecution: (Q) -> Unit): Completable =
        query
            .observeOn(ioScheduler)
            .doOnBeforeSuccess(queryExecution)
            .asCompletable()
}