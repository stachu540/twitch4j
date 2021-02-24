import okhttp3.OkHttpClient
import org.eclipse.jgit.api.Git
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.withType
import org.kohsuke.github.GHPerson
import org.kohsuke.github.GHUser
import org.kohsuke.github.GitHubBuilder
import org.kohsuke.github.extras.okhttp3.OkHttpConnector
import java.util.*

open class Github : Plugin<Project> {
	override fun apply(project: Project) {
		if (project == project.rootProject) {
			val git = Git.open(project.rootDir)
			val slug = try {
				git.remoteList().call().flatMap {
					it.urIs.filter { it.host == "github.com" }.map { it.path.substring(1, it.path.indexOf(".git")) }
				}.first()
			} finally {
				git.close()
				Git.shutdown()
			}

			val github = GitHubBuilder.fromEnvironment()
				.withConnector(OkHttpConnector(OkHttpClient()))
				.build()
			var repository = github.getRepository(slug)
			while (repository.isFork) {
				repository = repository.parent
			}
			val owner = repository.owner

			val members: Set<GHUser> = if (owner.isOrganization) github.getOrganization(owner.name).listMembers()
				.toSet() else setOf(owner as GHUser)

			val contributors: Set<GHUser> = repository.listContributors().toSet()
				.filter {
					it.login.toLowerCase() !in members.map { it.login.toLowerCase() } && !it.login.startsWith(
						"dependabot",
						true
					)
				}.toSet()

			project.allprojects {
				pluginManager.withPlugin("maven-publish") {
					val publishing: PublishingExtension by extensions
					publishing.publications.withType<MavenPublication> {
						pom {
							name.set(repository.name)
							description.set(repository.description)
							url.set(repository.homepage)
							inceptionYear.set("${Calendar.getInstance().get(Calendar.YEAR)}")
							scm {
								connection.set(repository.httpTransportUrl)
								developerConnection.set(repository.sshUrl)
								url.set("${repository.htmlUrl}")
								tag.set("v${project.version}")
							}
							issueManagement {
								system.set("GitHub")
								url.set("${repository.htmlUrl}/issues")
							}
							ciManagement {
								system.set("GitHub")
								url.set("${repository.htmlUrl}/actions")
							}
							licenses {
								license {
									name.set(repository.license.name)
									url.set("${repository.license.htmlUrl}")
									distribution.set("repo")
								}
							}
							distributionManagement {
								downloadUrl.set("${repository.htmlUrl}/releases/tag/v${project.version}")
							}
							if (owner.isOrganization) {
								organization {
									name.set(owner.name)
									url.set(owner.htmlUrl.toString())
								}
							}
							developers {
								members.forEach {
									developer {
										id.set(it.login)
										name.set(it.name)
										email.set(it.email)
										url.set("${it.htmlUrl}")
									}
								}
							}
							contributors {
								contributors.forEach {
									contributor {
										name.set(it.login)
										url.set("${it.htmlUrl}")
									}
								}
							}
						}
					}
				}
			}
		}
	}
}

val GHPerson.isOrganization
	get() = type == "Organization"

val GHPerson.isUser
	get() = type == "User"

