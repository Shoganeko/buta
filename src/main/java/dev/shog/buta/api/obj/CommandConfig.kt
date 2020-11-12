package dev.shog.buta.api.obj

import dev.shog.buta.api.obj.perms.All
import dev.shog.buta.api.permission.Permable

data class CommandConfig(
        val name: String,
        val description: String,
        val category: Category,
        val help: HashMap<String, String> = hashMapOf(),
        val aliases: List<String> = listOf(),
        val permable: Permable = All,
        val isPmAvailable: Boolean = true
)