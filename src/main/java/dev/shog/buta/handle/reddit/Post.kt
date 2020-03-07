package dev.shog.buta.handle.reddit

import org.json.JSONObject

/**
 * A post
 *
 * @param type The type of post.
 * @param data The post's data.
 */
data class Post(val type: PostType, val data: JSONObject)