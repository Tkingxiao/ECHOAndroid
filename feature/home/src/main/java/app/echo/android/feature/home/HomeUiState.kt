package app.echo.android.feature.home

import app.echo.android.model.library.EchoTrack
import app.echo.android.model.playback.EchoPlaybackStatus

data class HomeUiState(
    val playbackStatus: EchoPlaybackStatus,
    val trackCount: Int,
    val albumCount: Int,
    val artistCount: Int,
    val recommendedTracks: List<EchoTrack>,
)
