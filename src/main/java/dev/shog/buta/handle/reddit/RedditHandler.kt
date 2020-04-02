package dev.shog.buta.handle.reddit

import dev.shog.buta.APP
import dev.shog.lib.util.logDiscord
import kong.unirest.Unirest
import org.json.JSONObject

/**
 * This class works by caching previously requested posts, and then using them when they're requested.
 *
 * When a subreddit is requested, the contents is saved and that will be used whenever it's requested again. This is very helpful to limit requests to Reddit.
 */
object RedditHandler {
    /**
     * The subreddit's posts
     */
    private val subreddits = hashMapOf<String, MutableList<Post>>()

    /**
     * Gets a post from multiple subreddits
     */
    fun getPost(subs: ArrayList<String>, postType: PostType): Post? =
            getPost(subs.random(), postType)

    /**
     * Get [Post]s by their [PostType] from a sub-reddit.
     */
    private fun getPostsByType(subreddit: String, postType: PostType): List<Post> {
        val posts = subreddits[subreddit]

        if (posts.isNullOrEmpty())
            return listOf()

        return posts
                .filter { post -> post.type == postType }
                .toList()
    }

    /**
     * Clear [Post]s by their [PostType]
     */
    private fun clearPostsByType(subreddit: String, postType: PostType) {
        val posts = subreddits[subreddit]

        if (!posts.isNullOrEmpty()) {
            posts
                    .filter { post -> post.type == postType }
                    .onEach { post -> subreddits[subreddit]?.remove(post) }
        }
    }

    /**
     * Gets a post from a single subreddit
     */
    fun getPost(subredditName: String, postType: PostType): Post? {
        val subreddit = subredditName.toLowerCase()

        if (!subreddits.containsKey(subreddit) || getPostsByType(subreddit, postType).isEmpty())
            refreshSubreddit(subreddit, postType)

        val posts = getPostsByType(subreddit, postType)

        if (posts.isNullOrEmpty()) {
            val ret = refreshSubreddit(subreddit, postType)

            if (ret)
                return null
        }

        val post = posts.random()

        subreddits[subreddit]?.remove(post)

        return post
    }

    /**
     * Refreshes a subreddit's posts
     */
    private fun refreshSubreddit(subreddit: String, postType: PostType): Boolean {
        if (!subreddits.containsKey(subreddit))
            subreddits[subreddit] = mutableListOf()

        try {
            val json = when (postType) {
                PostType.HOT -> Unirest.get("https://reddit.com/r/$subreddit/hot.json?limit=100").asJsonAsync()
                PostType.NEW -> Unirest.get("https://reddit.com/r/$subreddit/new.json?limit=100").asJsonAsync()
                PostType.TOP -> Unirest.get("https://reddit.com/r/$subreddit/top.json?limit=100").asJsonAsync()
            }.get().body.`object` ?: return false

            if (json.getJSONObject("data").getJSONArray("children").isEmpty)
                return false

            clearPostsByType(subreddit.toLowerCase(), postType)

            val children = json.getJSONObject("data").getJSONArray("children")

            (0..children.length())
                    .forEach { i ->
                        val child = children.getJSONObject(i)

                        if (!child.getJSONObject("data").getBoolean("stickied")) {
                            subreddits[subreddit]?.add(Post(postType, JSONObject(child.toString()).getJSONObject("data")))
                        }
                    }

            return true
        } catch (ex: Exception) {
            ex.logDiscord(APP)
            return false
        }
    }
}