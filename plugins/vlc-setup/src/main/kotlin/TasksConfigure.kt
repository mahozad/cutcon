import org.gradle.api.tasks.TaskProvider

abstract class TasksConfigure {

    protected abstract fun isApplicable(): Boolean

    protected abstract fun apply(): TaskProvider<*>

    fun configure() = if (isApplicable()) apply() else null
}
