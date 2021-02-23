dependencies {
	api(project(":core"))
}

publishing.publications.withType<MavenPublication> {
	pom {
		name.set("Twitch4J WebSub")
		description.set("WebSub implementation.")
	}
}
