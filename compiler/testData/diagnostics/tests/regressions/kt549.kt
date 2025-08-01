// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
//KT-549 type inference failed

package demo

  fun <T> filter(list : Array<T>, filter :  (T) -> Boolean) : List<T> {
    val answer = java.util.ArrayList<T>();
    for (l in list) {
      if (filter(l)) answer.add(l)
    }
    return answer;
  }

fun main(args : Array<String>) {
  for (a in filter(args, {it.length > 1})) {
    System.out.println("Hello, ${a}!")
  }
}

/* GENERATED_FIR_TAGS: comparisonExpression, flexibleType, forLoop, functionDeclaration, functionalType, ifExpression,
integerLiteral, javaFunction, javaProperty, lambdaLiteral, localProperty, nullableType, propertyDeclaration,
stringLiteral, typeParameter */
