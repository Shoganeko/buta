package dev.shog.buta.commands.commands

import dev.shog.buta.commands.obj.Categories
import dev.shog.buta.commands.obj.Command
import dev.shog.buta.util.applyEmbed
import dev.shog.buta.util.ar
import dev.shog.buta.util.form
import dev.shog.buta.util.sendMessage
import kong.unirest.Unirest
import reactor.core.publisher.toMono

/**
 * Dog Fact
 */
val DOG_FACT = Command("dogfact", Categories.FUN) { e, _, lang ->
    Unirest.get("https://dog-api.kinduff.com/api/facts")
            .asJsonAsync()
            .toMono()
            .map { js -> js.body.`object` }
            .map { obj -> obj.getJSONArray("facts").first() }
            .flatMap { fact -> e.sendMessage(lang.getString("fact").form(fact as String)) }
            .then()
}.build().add()

/**
 * Cat Fact
 */
val CAT_FACT = Command("catfact", Categories.FUN) { e, _, lang ->
    Unirest.get("https://catfact.ninja/fact")
            .asJsonAsync()
            .toMono()
            .map { js -> js.body.`object` }
            .map { obj -> obj.getString("fact") }
            .flatMap { fact -> e.sendMessage(lang.getString("fact").form(fact as String)) }
            .then()
}.build().add()

/**
 * Cat gallery
 */
val CAT_GALLERY = Command("catgallery", Categories.FUN) { e, _, lang ->
    Unirest.get("https://api.thecatapi.com/v1/images/search?size=full")
            .asJsonAsync()
            .toMono()
            .map { js -> js.body.array }
            .map { obj -> obj.getJSONObject(0).getString("url") }
            .flatMap { url ->
                e.message.channel
                        .flatMap { ch ->
                            ch.createEmbed { spec ->
                                lang.getJSONObject("embed").applyEmbed(spec, e.message.author.get(), hashMapOf("thumb" to url.ar()))
                            }
                        }
            }
            .then()
}.build().add()

/**
 * Dog gallery
 */
val DOG_GALLERY = Command("doggallery", Categories.FUN) { e, _, lang ->
    Unirest.get("https://api.thedogapi.com/v1/images/search?size=full")
            .asJsonAsync()
            .toMono()
            .map { js -> js.body.array }
            .map { obj -> obj.getJSONObject(0).getString("url") }
            .flatMap { url ->
                e.message.channel
                        .flatMap { ch ->
                            ch.createEmbed { spec ->
                                lang.getJSONObject("embed").applyEmbed(spec, e.message.author.get(), hashMapOf("thumb" to url.ar()))
                            }
                        }
            }
            .then()
}.build().add()

/**
 * Get a random word
 */
val RANDOM_WORD = Command("randomword", Categories.FUN) { e, args, lang ->
    Unirest.get("https://random-word-api.herokuapp.com/word?key=LN5TCZP1")
            .asJsonAsync()
            .toMono()
            .map { js -> js.body.array }
            .map { ar -> ar.getString(0) }
            .flatMap { word -> e.sendMessage(lang.getString("word").form(word)) }
            .then()
}.build().add()

/**
 * Reverse a word.
 */
val WORD_REVERSE = Command("wordreverse", Categories.FUN) { e, args, lang ->
    if (args.isEmpty())
        e.sendMessage(lang.getString("include-word")).then()
    else e.sendMessage(lang.getString("success").form(args[0].reversed())).then()
}.build().add()
