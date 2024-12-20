package io.getstream.android.xr.video

import android.app.Application
import io.getstream.video.android.core.GEO
import io.getstream.video.android.core.StreamVideo
import io.getstream.video.android.core.StreamVideoBuilder
import io.getstream.video.android.model.User

class XRApp: Application() {

    override fun onCreate() {
        super.onCreate()

        val apiKey = "wt9xvrvup9nq"
        val userId = "stream"
        val token = StreamVideo.devToken(userId)

        // Create a user.
        val user = User(
            id = userId,
            name = "Tutorial",
            image = "http://placekitten.com/200/300",
        )

        // Initialize StreamVideo. For a production app.
        StreamVideoBuilder(
            context = applicationContext,
            apiKey = apiKey,
            geo = GEO.GlobalEdgeNetwork,
            user = user,
            token = token,
        ).build()
    }
}