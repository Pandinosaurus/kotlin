// RUN_PIPELINE_TILL: BACKEND
// FILE: Producer.java

import java.util.*;
import org.jetbrains.annotations.*;

public class Producer {
    @NotNull
    public static ArrayList foo() { return null; }
}

// FILE: test.kt

interface StringSet : MutableSet<String>

fun foo(arg: Boolean) {
    val x = Producer.foo()
    if (x is Set<*>) {
        val y = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.collections.Set<*> & (java.util.ArrayList<(kotlin.Any..kotlin.Any?)>..java.util.ArrayList<*>)")!>x<!>
    }
    if (x is MutableSet<*>) {
        val y = <!DEBUG_INFO_EXPRESSION_TYPE("kotlin.collections.MutableSet<*> & (java.util.ArrayList<(kotlin.Any..kotlin.Any?)>..java.util.ArrayList<*>)")!>x<!>
    }
    if (x is StringSet) {
        x.add("")
        x.add(1)
        x.add(null)
        x.iterator().next().length
    }
}

/* GENERATED_FIR_TAGS: flexibleType, functionDeclaration, ifExpression, integerLiteral, interfaceDeclaration,
intersectionType, isExpression, javaFunction, localProperty, nullableType, propertyDeclaration, smartcast,
starProjection, stringLiteral */
