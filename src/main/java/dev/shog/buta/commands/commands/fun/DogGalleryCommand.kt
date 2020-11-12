package dev.shog.buta.commands.commands.`fun`

import com.gitlab.kordlib.core.behavior.channel.createEmbed
import dev.shog.buta.api.obj.Category
import dev.shog.buta.api.obj.Command
import dev.shog.buta.api.obj.CommandConfig
import dev.shog.buta.util.addFooter
import kong.unirest.Unirest

val DOG_GALLERY_COMMAND = Command(CommandConfig(
        name = "doggallery",
        category = Category.FUN,
        help = hashMapOf("doggallery" to "Get a random picture of a dog."),
        description = "Get pictures of dogs!"
)) {
    val array = Unirest.get("https://api.thedogapi.com/v1/images/search?size=full")
            .asJson()
            .body
            .array

    val url = array.getJSONObject(0).getString("url")

    event.message.channel.createEmbed {
        addFooter(event)

        description = "Retrieved from thedogapi.com"
        image = url
    }
}