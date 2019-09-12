package dev.shog.buta.commands.api.obj

import java.util.concurrent.ConcurrentHashMap

open class DataObject {
    val data = ConcurrentHashMap<String, Any>()
}