//KT-1718 compiler error when not using temporary variable
package n

import java.util.ArrayList

fun test() {
    val list = arrayList("foo", "bar") + arrayList("cheese", "wine")
    list: List<String>
    //check it's not an error type
    <!TYPE_MISMATCH!>list<!>: Int
}

//from library
fun arrayList<T>(vararg <!UNUSED_PARAMETER!>values<!>: T) : ArrayList<T> {<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>
fun <in T> Iterable<T>.plus(<!UNUSED_PARAMETER!>elements<!>: Iterable<T>): List<T> {<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>