package kt43176

import kt43176.KotlinScriptRunner.eval
import kotlin.reflect.KClass
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

data class ProvidedProperty(val name: String, val type: KClass<*>, val value: Any?) {
    constructor(name: String, type: Class<*>, value: Any?) : this(name, type.kotlin, value)
}

object KotlinScriptRunner  {

    fun eval(sourceCode: SourceCode, props: List<ProvidedProperty>): ResultWithDiagnostics<EvaluationResult> {

        val compileConfig = ScriptCompilationConfiguration {
            jvm {
                defaultImports("kt43176.*")
                dependenciesFromCurrentContext(wholeClasspath = true)
            }
            providedProperties(*(props.map { it.name to KotlinType(it.type) }.toTypedArray()))
        }
        val evaluationConfig = ScriptEvaluationConfiguration {
            providedProperties(*(props.map { it.name to it.value }.toTypedArray()))
        }

        return BasicJvmScriptingHost().eval(sourceCode, compileConfig, evaluationConfig)
    }
}

fun fun1(x: Int, y: Int, aFunction: (Int,Int) -> Int) {
    println(aFunction.invoke(x, y))
}

inline fun fun2(x: Int, y: Int, aFunction: (Int,Int) -> Int) {
    println(aFunction.invoke(x, y))
}

const val script1 = """
    fun1(1,2) { x, y -> 
        (x+y) % aValue
    }
"""

const val script2 = """
    fun2(1,2) { x, y ->
        (x+y) % aValue
    }
"""

fun main() {

    val props = listOf(
        ProvidedProperty("aValue", Int::class, 3)
    )

    // Should return Success(value=EvaluationResult(returnValue=java.lang.NoSuchMethodError: 'int Script.access$getAValue$p(Script)' ...
    println(eval(script1.toScriptSource(), props))

    // Should return Success(value=EvaluationResult(returnValue=Unit ...
    println(eval(script2.toScriptSource(), props))
}