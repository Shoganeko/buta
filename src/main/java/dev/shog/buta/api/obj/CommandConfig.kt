package dev.shog.buta.api.obj

import dev.shog.buta.api.obj.perms.All
import dev.shog.buta.api.permission.Permable

data class CommandConfig(
        val name: String,
        val permable: Permable = All,
        val isPmAvailable: Boolean = true
)