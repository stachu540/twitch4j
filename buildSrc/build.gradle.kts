plugins {
	`kotlin-dsl`
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.kohsuke:github-api:1.122") // github API
	implementation("org.eclipse.jgit:org.eclipse.jgit:5.10.0.202012080955-r") // JGit
	implementation("com.squareup.okhttp3:logging-interceptor:4.9.0")
	implementation("com.squareup.okhttp3:okhttp:4.9.0")
}
