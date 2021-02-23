dependencies {
	api(project(":core"))
}

publishing.publications.withType<MavenPublication> {
	pom {
		name.set("Twitch4J PubSub")
		description.set("PubSub companion.")
	}
}
