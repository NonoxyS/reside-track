plugins {
    alias(libs.plugins.conventionPlugin.kmpLibrary)
}

iosConfig {
    xcFrameworkName = "common"
}

android {
    namespace = "dev.nonoxy.common"
}
