/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("NOTHING_TO_INLINE")

package kotlin

import kotlin.js.*

/**
 * Returns a string representation of the object. Can be called with a null receiver, in which case
 * it returns the string "null".
 */
public actual fun Any?.toString(): String = this?.toString() ?: "null"


/**
 * Concatenates this string with the string representation of the given [other] object. If either the receiver
 * or the [other] object are null, they are represented as the string "null".
 */
public actual operator fun String?.plus(other: Any?): String =
    (this?.toString() ?: "null").plus(other?.toString() ?: "null")

/**
 * Returns an array of objects of the given type with the given [size], initialized with null values.
 */
public actual inline fun <T> arrayOfNulls(size: Int): Array<T?> = Array<T?>(size)

/**
 * Returns an array containing the specified elements.
 */
public actual inline fun <T> arrayOf(vararg elements: T): Array<T> = elements.unsafeCast<Array<T>>()

/**
 * Returns an array containing the specified [Double] numbers.
 */
public actual inline fun doubleArrayOf(vararg elements: Double): DoubleArray = elements

/**
 * Returns an array containing the specified [Float] numbers.
 */
public actual inline fun floatArrayOf(vararg elements: Float): FloatArray = elements

/**
 * Returns an array containing the specified [Long] numbers.
 */
public actual inline fun longArrayOf(vararg elements: Long): LongArray = elements

/**
 * Returns an array containing the specified [Int] numbers.
 */
public actual inline fun intArrayOf(vararg elements: Int): IntArray = elements

/**
 * Returns an array containing the specified characters.
 */
public actual inline fun charArrayOf(vararg elements: Char): CharArray = elements

/**
 * Returns an array containing the specified [Short] numbers.
 */
public actual inline fun shortArrayOf(vararg elements: Short): ShortArray = elements

/**
 * Returns an array containing the specified [Byte] numbers.
 */
public actual inline fun byteArrayOf(vararg elements: Byte): ByteArray = elements

/**
 * Returns an array containing the specified boolean values.
 */
public actual inline fun booleanArrayOf(vararg elements: Boolean): BooleanArray = elements

// Use non-inline calls to enumValuesIntrinsic and enumValueOfIntrinsic calls in order
// for compiler to replace them with method calls of concrete enum classes after inlining.
// TODO: Figure out better solution (Inline hacks? Dynamic calls to stable mangled names?)

/**
 * Returns an array containing enum T entries.
 */
@SinceKotlin("1.1")
public actual inline fun <reified T : Enum<T>> enumValues(): Array<T> = enumValuesIntrinsic<T>()

/**
 * Returns an enum entry with specified name.
 */
@SinceKotlin("1.1")
public actual inline fun <reified T : Enum<T>> enumValueOf(name: String): T = enumValueOfIntrinsic<T>(name)