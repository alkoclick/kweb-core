package io.kweb.routing

import io.kotlintest.specs.FreeSpec

/**
 * Created by @@jmdesprez, some modifications by @sanity
 */

data class Nullables(val x: Int, val y: Int?)

sealed class Entity {
    data class Users(val id: Int, val userEntity: UserEntity) : Entity()
    data class Spaceships(val id: Int) : Entity()
    data class Squares(val x: Int, val y: Int, val z: Int = 42) : Entity()
    class Root : Entity() {
        // Necessary for tests, shouldn't be necessary if the user is using when/is construct, but perhaps
        // should recommend it anyway
        override fun equals(other: Any?) = other is Root
        override fun hashCode() = this::class.hashCode()
    }
}

data class Project(val id: Int) : UserEntity()

sealed class UserEntity {
    class Root : UserEntity() {
        // Necessary for tests, shouldn't be necessary if the user is using when/is construct, but perhaps
        // should recommend it anyway
        override fun equals(other: Any?) = other is UserEntity.Root
        override fun hashCode() = this::class.hashCode()
    }

    data class Friend(val id: Int) : UserEntity()
    class Settings : UserEntity()
}

class UrlPathParserSpec : FreeSpec() {
    init {
        // TODO choose one solution
        // val contextProvider = KClass<*>::meAndNested
        val urlPathParser = UrlPathParser(ClasspathScanner("io.kweb.routing")::getContext)

        "test URL path parsing" - {
            val parseableUrls = table(
                    headers("url", "parsedUrl"),
                    row<String, Entity>("/", Entity.Root()),
                    row<String, Entity>("/users/152", Entity.Users(152, UserEntity.Root())),
                    row<String, Entity>("/users/152/", Entity.Users(152, UserEntity.Root())),
                    row<String, Entity>("/spaceships/5235/", Entity.Spaceships(5235)),
                    row<String, Entity>("/squares/152/22/44", Entity.Squares(152, 22, 44)),
                    row<String, Entity>("/squares/142/23", Entity.Squares(142, 23)),
                    row<String, Entity>("/users/152/friend/51", Entity.Users(152, UserEntity.Friend(51)))
                    )
            forAll(parseableUrls) { url, parsedUrl ->
                "verify that $url is parsed correctly" {
                    urlPathParser.parse<Entity>(url) shouldEqual parsedUrl
                }
            }
        }

        "test URL path generation" - {
            val parseableUrls = table(
                    headers("url", "parsedUrl"),
                    row<String, Entity>("/", Entity.Root()),
                    row<String, Entity>("/users/152", Entity.Users(152, UserEntity.Root())),
                    row<String, Entity>("/squares/152/22/44", Entity.Squares(152, 22, 44)),
                    row<String, Entity>("/squares/142/23", Entity.Squares(142, 23)),
                    row<String, Entity>("/users/152/friend/51", Entity.Users(152, UserEntity.Friend(51)))
            )
            forAll(parseableUrls) { url, parsedUrl ->
                "verify that $parsedUrl is translated back to $url correctly" {
                    parsedUrl.toPath() shouldEqual url
                }
            }
        }

        "should handle null value when property has no default" {
            urlPathParser.parse<Nullables>("/nullables/42") shouldBe Nullables(42, null)
        }

        "should throw an exception for malformed URLs" {
            val parseableUrls = table(
                    headers("url"),
                    row<String>("asdf"),
                    row<String>("/userps/152"),
                    row<String>("/users/152/frieb/51")
            )
            forAll(parseableUrls) { url ->
                shouldThrow<UrlParseException> {
                    urlPathParser.parse<Entity>(url)
                    Unit
                }
            }
        }
    }
}