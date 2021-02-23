import org.gradle.api.Project
import org.gradle.api.internal.project.DefaultProject
import org.gradle.util.Path

val Project.nexusUser: String?
	get() = System.getenv("SONATYPE_USER") ?: findProperty("nexus.user")?.toString()

val Project.nexusPassword: String?
	get() = System.getenv("SONATYPE_PASSOWRD") ?: findProperty("nexus.passowrd")?.toString()

val Project.globalProjects
    get() = rootProject.subprojects.filter { it.name !in arrayOf("bom", "all") && it.rootProject != it }

val Project.isSnapshot: Boolean
    get() = (rootProject.version as String).endsWith("-SNAPSHOT")

val Project.artifactId: String
    get() = (this as DefaultProject).identityPath.path.replace(Path.SEPARATOR, "-").let {
        if (rootProject.name.equals(it.substring(1), true)) rootProject.name else (rootProject.name + if (it.startsWith(
                "-"
            ) && it.length > 1
        ) it else "")
    }.toLowerCase()
