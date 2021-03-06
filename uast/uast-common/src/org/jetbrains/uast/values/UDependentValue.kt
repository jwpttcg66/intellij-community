/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.uast.values

open class UDependentValue protected constructor(
        val value: UValue,
        override val dependencies: Set<UDependency> = emptySet()
) : UValueBase() {

    private fun UValue.unwrap() = (this as? UDependentValue)?.unwrap() ?: this

    private fun unwrap(): UValue = value.unwrap()

    private val dependenciesWithThis: Set<UDependency>
        get() = (this as? UDependency)?.let { dependencies + it } ?: dependencies

    private fun wrapBinary(result: UValue, arg: UValue): UValue {
        val wrappedDependencies = (arg as? UDependentValue)?.dependenciesWithThis ?: emptySet()
        val resultDependencies = dependenciesWithThis + wrappedDependencies
        return create(result, resultDependencies)
    }

    private fun wrapUnary(result: UValue) = create(result, dependenciesWithThis)

    override fun plus(other: UValue) = wrapBinary(unwrap() + other.unwrap(), other)

    override fun minus(other: UValue) = wrapBinary(unwrap() - other.unwrap(), other)

    override fun times(other: UValue) = wrapBinary(unwrap() * other.unwrap(), other)

    override fun div(other: UValue) = wrapBinary(unwrap() / other.unwrap(), other)

    internal fun inverseDiv(other: UValue) = wrapBinary(other.unwrap() / unwrap(), other)

    override fun mod(other: UValue) = wrapBinary(unwrap() % other.unwrap(), other)

    internal fun inverseMod(other: UValue) = wrapBinary(other.unwrap() % unwrap(), other)

    override fun unaryMinus() = wrapUnary(-unwrap())

    override fun valueEquals(other: UValue) = wrapBinary(unwrap() valueEquals other.unwrap(), other)

    override fun valueNotEquals(other: UValue) = wrapBinary(unwrap() valueNotEquals other.unwrap(), other)

    override fun not() = wrapUnary(!unwrap())

    override fun greater(other: UValue) = wrapBinary(unwrap() greater other.unwrap(), other)

    override fun less(other: UValue) = wrapBinary(other.unwrap() greater unwrap(), other)

    override fun inc() = wrapUnary(unwrap().inc())

    override fun dec() = wrapUnary(unwrap().dec())

    override fun and(other: UValue) = wrapBinary(unwrap() and other.unwrap(), other)

    override fun or(other: UValue) = wrapBinary(unwrap() or other.unwrap(), other)

    override fun bitwiseAnd(other: UValue) = wrapBinary(unwrap() bitwiseAnd other.unwrap(), other)

    override fun bitwiseOr(other: UValue) = wrapBinary(unwrap() bitwiseOr other.unwrap(), other)

    override fun bitwiseXor(other: UValue) = wrapBinary(unwrap() bitwiseXor other.unwrap(), other)

    override fun shl(other: UValue) = wrapBinary(unwrap() shl other.unwrap(), other)

    internal fun inverseShiftLeft(other: UValue) = wrapBinary(other.unwrap() shl unwrap(), other)

    override fun shr(other: UValue) = wrapBinary(unwrap() shr other.unwrap(), other)

    internal fun inverseShiftRight(other: UValue) = wrapBinary(other.unwrap() shr unwrap(), other)

    override fun ushr(other: UValue) = wrapBinary(unwrap() ushr other.unwrap(), other)

    internal fun inverseShiftRightUnsigned(other: UValue) =
            wrapBinary(other.unwrap() ushr unwrap(), other)

    override fun merge(other: UValue) = when (other) {
        this -> this
        value -> this
        is UDependentValue -> {
            if (value != other.value) UPhiValue.create(this, other)
            else UDependentValue(value, dependencies + other.dependencies)
        }
        else -> UPhiValue.create(this, other)
    }

    override fun toConstant() = value.toConstant()

    open internal fun copy(dependencies: Set<UDependency>) =
            if (dependencies == this.dependencies) this else create(value, dependencies)

    override fun coerceConstant(constant: UConstant): UValue =
            if (toConstant() == constant) this
            else create(value.coerceConstant(constant), dependencies)

    override fun equals(other: Any?) =
            other is UDependentValue
            && javaClass == other.javaClass
            && value == other.value
            && dependencies == other.dependencies

    override fun hashCode(): Int {
        var result = 31
        result = result * 19 + value.hashCode()
        result = result * 19 + dependencies.hashCode()
        return result
    }

    override fun toString() =
            if (dependencies.isNotEmpty())
                "$value" + dependencies.joinToString(prefix = " (depending on: ", postfix = ")", separator = ", ")
            else
                "$value"

    companion object {
        fun create(value: UValue, dependencies: Set<UDependency>): UValue =
                if (dependencies.isNotEmpty()) UDependentValue(value, dependencies)
                else value

        internal fun UValue.coerceConstant(constant: UConstant): UValue =
                (this as? UValueBase)?.coerceConstant(constant) ?: constant
    }
}
