package eu.corvus.essentials.core

import timber.log.Timber

/**
 * Created by Vlad Cazacu on 17.03.2017.
 */

fun debug(message: String, vararg anys: Any) = Timber.d(message, *anys)
fun info(message: String, vararg anys: Any) = Timber.i(message, *anys)
fun error(message: String, throwable: Throwable? = null, vararg anys: Any) = Timber.e(throwable, message, *anys)
fun warn(message: String, throwable: Throwable? = null, vararg anys: Any) = Timber.i(throwable, message, *anys)