package dev.shog.buta.commands.obj

import dev.shog.buta.commands.permission.Permable

data class CommandConfig(
        val name: String,
        val desc: String,
        val category: Category,
        val permable: Permable,
        val isPmAvailable: Boolean = true
) {
    companion object {
        /**
         * Get a command config.
         */
        fun configure(cfg: () -> CommandConfig): CommandConfig =
                cfg.invoke()
    }
}