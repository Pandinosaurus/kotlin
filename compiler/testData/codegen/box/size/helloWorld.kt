// TARGET_BACKEND: WASM

// RUN_THIRD_PARTY_OPTIMIZER
// WASM_DCE_EXPECTED_OUTPUT_SIZE: wasm  55_043
// WASM_DCE_EXPECTED_OUTPUT_SIZE: mjs    5_890
// WASM_OPT_EXPECTED_OUTPUT_SIZE:        6_381

fun box(): String {
    println("Hello, World!")
    return "OK"
}
