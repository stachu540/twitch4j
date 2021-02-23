plugins {
	id("com.apollographql.apollo") version "2.5.3"
}

dependencies {
	api("com.apollographql.apollo:apollo-runtime:2.5.3")

	api(project(":core"))
}

tasks.withType<Javadoc> {
	// Ignore auto-generated files from apollo graphql
	exclude("com/github/twitch4j/graphql/internal/**")
}

publishing.publications.withType<MavenPublication> {
	pom {
		name.set("Twitch4J GraphQL (Experimental)")
		description.set("Experimental GraphQL API.")
	}
}
