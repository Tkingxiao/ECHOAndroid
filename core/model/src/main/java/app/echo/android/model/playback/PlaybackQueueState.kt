package app.echo.android.model.playback

data class PlaybackQueueState(
    val items: List<EchoTrackRef> = emptyList(),
    val currentIndex: Int = -1,
) {
    val isEmpty: Boolean
        get() = items.isEmpty()

    val currentItem: EchoTrackRef?
        get() = items.getOrNull(currentIndex)
}
