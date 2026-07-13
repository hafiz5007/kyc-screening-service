rootProject.name = "kyc-screening-service"

// Pure-domain sub-module. Zero Spring / MongoDB dependencies — the compiler
// enforces the dependency rule: this sub-module has no way to reach into the
// Spring layer, so it can't drift back into being framework-coupled.
include(":kyc-domain")
