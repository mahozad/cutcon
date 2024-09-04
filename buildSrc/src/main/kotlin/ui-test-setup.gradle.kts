import org.gradle.api.tasks.testing.logging.TestLogEvent

// See the section about tests in README.
// See https://discuss.gradle.org/t/how-do-i-add-directories-to-the-main-resources-sourceset-in-a-gradle-plugin/5953
val sourceSets = properties["sourceSets"] as SourceSetContainer;
sourceSets {
    create("uiTest") {
        // Adds files from the main source set to the compile classpath and runtime classpath of this new source set.
        // sourceSets.main.output is a collection of all the directories containing compiled main classes and resources
        val mainSourceSet = sourceSets.getByName("main")
        compileClasspath += mainSourceSet.output
        runtimeClasspath += mainSourceSet.output
    }
}

// Makes the uiTestImplementation configuration extend from testImplementation,
// which means that all the declared dependencies of the test code (and transitively the main as well)
// also become dependencies of this new configuration
val uiTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.named("testImplementation").get())
}

val uiTest = task<Test>("uiTest") {
    description = "Runs UI tests."
    group = "verification"

    testClassesDirs = sourceSets["uiTest"].output.classesDirs
    classpath = sourceSets["uiTest"].runtimeClasspath
    shouldRunAfter("test")

    testLogging { events(TestLogEvent.PASSED) }
}

tasks.named("check") { dependsOn(uiTest) }
