package com.company.app.common.rbac

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class RequiresPermission(
    val module: String,
    val action: Action
) {
    enum class Action { READ, WRITE, DELETE }
}
