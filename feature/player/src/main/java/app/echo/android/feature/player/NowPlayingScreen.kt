package app.echo.android.feature.player

import androidx.compose.runtime.Composable
import app.echo.android.feature.home.HomeScreen
import app.echo.android.model.library.EchoTrack
import app.echo.android.model.playback.EchoPlaybackStatus

@Composable
fun NowPlayingScreen(
    status: EchoPlaybackStatus,
    trackCount: Int,
    albumCount: Int,
    artistCount: Int,
    recommendedTracks: List<EchoTrack>,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onCycleRepeatMode: () -> Unit,
    onToggleShuffle: () -> Unit,
    onRefreshRecommendations: () -> Unit,
    onPlayRecommendation: (List<EchoTrack>, Int) -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenConnect: () -> Unit,
) {
    HomeScreen(
        status = status,
        trackCount = trackCount,
        albumCount = albumCount,
        artistCount = artistCount,
        recommendedTracks = recommendedTracks,
        onPlayPause = onPlayPause,
        onNext = onNext,
        onPrevious = onPrevious,
        onCycleRepeatMode = onCycleRepeatMode,
        onToggleShuffle = onToggleShuffle,
        onRefreshRecommendations = onRefreshRecommendations,
        onPlayRecommendation = onPlayRecommendation,
        onOpenLibrary = onOpenLibrary,
        onOpenConnect = onOpenConnect,
    )
}
