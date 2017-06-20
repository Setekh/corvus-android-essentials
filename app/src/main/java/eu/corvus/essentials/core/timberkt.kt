package eu.corvus.essentials.core

import timber.log.Timber

/**
 * Created by Vlad Cazacu on 17.03.2017.
 */

fun Any.debug(message: String, vararg anys: Any) = Timber.tag(javaClass.simpleName).d(message, *anys)
fun Any.info(message: String, vararg anys: Any) = Timber.tag(javaClass.simpleName).i(message, *anys)
fun Any.error(message: String, throwable: Throwable? = null, vararg anys: Any) = Timber.tag(javaClass.simpleName).e(throwable, message, *anys)
fun Any.warn(message: String, throwable: Throwable? = null, vararg anys: Any) = Timber.tag(javaClass.simpleName).w(throwable, message, *anys)