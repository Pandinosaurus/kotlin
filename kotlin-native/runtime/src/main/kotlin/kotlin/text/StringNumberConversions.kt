/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package kotlin.text

import kotlin.native.internal.FloatingPointParser
import kotlin.native.internal.GCUnsafeCall
import kotlin.native.internal.escapeAnalysis.Escapes

/**
 * Returns a string representation of this [Byte] value in the specified [radix].
 *
 * @throws IllegalArgumentException when [radix] is not a valid radix for number to string conversion.
 */
@SinceKotlin("1.1")
@kotlin.internal.InlineOnly
public actual inline fun Byte.toString(radix: Int): String = this.toInt().toString(checkRadix(radix))

/**
 * Returns a string representation of this [Short] value in the specified [radix].
 *
 * @throws IllegalArgumentException when [radix] is not a valid radix for number to string conversion.
 */
@SinceKotlin("1.1")
@kotlin.internal.InlineOnly
public actual inline fun Short.toString(radix: Int): String = this.toInt().toString(checkRadix(radix))

@GCUnsafeCall("Kotlin_Int_toStringRadix")
@PublishedApi
@Escapes.Nothing
external internal fun intToString(value: Int, radix: Int): String

/**
 * Returns a string representation of this [Int] value in the specified [radix].
 *
 * @throws IllegalArgumentException when [radix] is not a valid radix for number to string conversion.
 */
@SinceKotlin("1.1")
@kotlin.internal.InlineOnly
public actual inline fun Int.toString(radix: Int): String = intToString(this, checkRadix(radix))

@GCUnsafeCall("Kotlin_Long_toStringRadix")
@PublishedApi
@Escapes.Nothing
external internal fun longToString(value: Long, radix: Int): String

/**
 * Returns a string representation of this [Long] value in the specified [radix].
 *
 * @throws IllegalArgumentException when [radix] is not a valid radix for number to string conversion.
 */
@SinceKotlin("1.1")
@kotlin.internal.InlineOnly
public actual inline fun Long.toString(radix: Int): String = longToString(this, checkRadix(radix))

/**
 * Returns `true` if this string is not `null` and its content is equal to the word "true", ignoring case, and `false` otherwise.
 *
 * There are also strict versions of the function available on non-nullable String, [toBooleanStrict] and [toBooleanStrictOrNull].
 */
@SinceKotlin("1.4")
@kotlin.internal.InlineOnly
public actual inline fun String?.toBoolean(): Boolean = this.equals("true", ignoreCase = true)

/**
 * Parses the string to a [Byte] number.
 *
 * The string must consist of an optional leading `+` or `-` sign and decimal digits (`0-9`),
 * and fit the valid [Byte] value range (within `Byte.MIN_VALUE..Byte.MAX_VALUE`),
 * otherwise a [NumberFormatException] will be thrown.
 *
 * @throws NumberFormatException if the string is not a valid representation of a [Byte].
 * @sample samples.text.Numbers.toByte
 */
@kotlin.internal.InlineOnly
public actual inline fun String.toByte(): Byte = toByteOrNull() ?: throw NumberFormatException()

/**
 * Parses the string as a signed [Byte] number and returns the result.
 * @throws NumberFormatException if the string is not a valid representation of a number.
 * @throws IllegalArgumentException when [radix] is not a valid radix for string to number conversion.
 */
@SinceKotlin("1.1")
@kotlin.internal.InlineOnly
public actual inline fun String.toByte(radix: Int): Byte = toByteOrNull(radix) ?: throw NumberFormatException()

/**
 * Parses the string to a [Short] number.
 *
 * The string must consist of an optional leading `+` or `-` sign and decimal digits (`0-9`),
 * and fit the valid [Short] value range (within `Short.MIN_VALUE..Short.MAX_VALUE`),
 * otherwise a [NumberFormatException] will be thrown.
 *
 * @throws NumberFormatException if the string is not a valid representation of a [Short].
 * @sample samples.text.Numbers.toShort
 */
@kotlin.internal.InlineOnly
public actual inline fun String.toShort(): Short = toShortOrNull() ?: throw NumberFormatException()

/**
 * Parses the string as a [Short] number and returns the result.
 * @throws NumberFormatException if the string is not a valid representation of a number.
 * @throws IllegalArgumentException when [radix] is not a valid radix for string to number conversion.
 */
@SinceKotlin("1.1")
@kotlin.internal.InlineOnly
public actual inline fun String.toShort(radix: Int): Short = toShortOrNull(radix) ?: throw NumberFormatException()

/**
 * Parses the string to an [Int] number.
 *
 * The string must consist of an optional leading `+` or `-` sign and decimal digits (`0-9`),
 * and fit the valid [Int] value range (within `Int.MIN_VALUE..Int.MAX_VALUE`),
 * otherwise a [NumberFormatException] will be thrown.
 *
 * @throws NumberFormatException if the string is not a valid representation of an [Int].
 * @sample samples.text.Numbers.toInt
 */
@kotlin.internal.InlineOnly
public actual inline fun String.toInt(): Int = toIntOrNull() ?: throw NumberFormatException()

/**
 * Parses the string as an [Int] number and returns the result.
 * @throws NumberFormatException if the string is not a valid representation of a number.
 * @throws IllegalArgumentException when [radix] is not a valid radix for string to number conversion.
 */
@SinceKotlin("1.1")
@kotlin.internal.InlineOnly
public actual inline fun String.toInt(radix: Int): Int = toIntOrNull(radix) ?: throw NumberFormatException()

/**
 * Parses the string to a [Long] number.
 *
 * The string must consist of an optional leading `+` or `-` sign and decimal digits (`0-9`),
 * and fit the valid [Long] value range (within `Long.MIN_VALUE..Long.MAX_VALUE`),
 * otherwise a [NumberFormatException] will be thrown.
 *
 * @throws NumberFormatException if the string is not a valid representation of a [Long].
 * @sample samples.text.Numbers.toLong
 */
@kotlin.internal.InlineOnly
public actual inline fun String.toLong(): Long = toLongOrNull() ?: throw NumberFormatException()

/**
 * Parses the string as a [Long] number and returns the result.
 * @throws NumberFormatException if the string is not a valid representation of a number.
 * @throws IllegalArgumentException when [radix] is not a valid radix for string to number conversion.
 */
@SinceKotlin("1.1")
@kotlin.internal.InlineOnly
public actual inline fun String.toLong(radix: Int): Long = toLongOrNull(radix) ?: throw NumberFormatException()

/**
 * Parses the string as a [Float] number and returns the result.
 * @throws NumberFormatException if the string is not a valid representation of a number.
 */
public actual fun String.toFloat(): Float = FloatingPointParser.parseFloat(this)


/**
 * Parses the string as a [Double] number and returns the result.
 * @throws NumberFormatException if the string is not a valid representation of a number.
 */
public actual fun String.toDouble(): Double = FloatingPointParser.parseDouble(this)

/**
 * Parses the string as a [Float] number and returns the result
 * or `null` if the string is not a valid representation of a number.
 */
@SinceKotlin("1.1")
public actual fun String.toFloatOrNull(): Float? {
    try {
        return toFloat()
    } catch (e: NumberFormatException) {
        return null
    }
}

/**
 * Parses the string as a [Double] number and returns the result
 * or `null` if the string is not a valid representation of a number.
 */
@SinceKotlin("1.1")
public actual fun String.toDoubleOrNull(): Double? {
    try {
        return toDouble()
    } catch (e: NumberFormatException) {
        return null
    }
}
