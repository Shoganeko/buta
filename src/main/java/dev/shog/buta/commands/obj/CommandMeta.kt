package dev.shog.buta.commands.obj

/**
 * The meta for a [BuiltCommand].
 */
data class CommandMeta(
        val commandName: String,
        val commandDesc: String,
        val commands: HashMap<String, String>,
        val isPmAvailable: Boolean,
        val alias: ArrayList<String>
)