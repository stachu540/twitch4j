dependencies {
	api(project(":core"))
}

publishing.publications.withType<MavenPublication> {
	pom {
		name.set("Twitch4J EventSub")
		description.set("Webhook Events.")
	}
}
