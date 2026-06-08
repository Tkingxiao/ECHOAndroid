package app.echo.android

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import app.echo.android.connect.EchoRemoteClient
import app.echo.android.design.EchoContentMaxWidth
import app.echo.android.design.EchoGlassBackground
import app.echo.android.design.EchoMobileTheme
import app.echo.android.feature.connect.ConnectScreen
import app.echo.android.feature.library.LibraryScreen
import app.echo.android.feature.player.MiniPlayer
import app.echo.android.feature.player.NowPlayingScreen
import app.echo.android.feature.settings.DiagnosticsScreen
import app.echo.android.model.connect.EchoRemoteCommand
import app.echo.android.model.connect.EchoRemoteEndpoint
import app.echo.android.model.connect.EchoRemotePlaybackState
import app.echo.android.model.library.LibraryStats

@Composable
fun EchoAppRoot(viewModel: EchoAndroidViewModel) {
    val context = LocalContext.current
    val permission = remember { audioPermissionName() }
    var hasAudioPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasAudioPermission = granted
        if (granted) viewModel.refreshLibrary()
    }

    val remoteClient = remember { EchoRemoteClient() }
    val remoteStatus by remoteClient.status.collectAsStateWithLifecycle()
    val playbackStatus by viewModel.playbackStatus.collectAsStateWithLifecycle()
    val scanState by viewModel.scanState.collectAsStateWithLifecycle()
    val libraryStats by viewModel.libraryStats.collectAsStateWithLifecycle(LibraryStats())
    val homeRecommendedTracks by viewModel.recommendedTracks.collectAsStateWithLifecycle(emptyList())
    val tracks = viewModel.tracks.collectAsLazyPagingItems()
    val albums = viewModel.albums.collectAsLazyPagingItems()
    val artists = viewModel.artists.collectAsLazyPagingItems()
    var selectedTab by remember { mutableIntStateOf(EchoTab.Now.ordinal) }

    LaunchedEffect(hasAudioPermission) {
        if (hasAudioPermission && scanState.lastScanCount == null && !scanState.isScanning) {
            viewModel.refreshLibrary()
        }
    }

    EchoMobileTheme(darkTheme = isSystemInDarkTheme()) {
        Box(Modifier.fillMaxSize()) {
            EchoGlassBackground(Modifier.fillMaxSize())
            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(start = 14.dp, top = 8.dp, end = 14.dp, bottom = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(7.dp),
                    ) {
                        MiniPlayer(
                            status = playbackStatus,
                            onPlayPause = viewModel::playPause,
                            modifier = Modifier
                                .widthIn(max = EchoContentMaxWidth)
                                .fillMaxWidth(),
                        )
                        BottomDock(
                            selectedTab = selectedTab,
                            onLightSurface = true,
                            onSelectTab = { selectedTab = it },
                            modifier = Modifier
                                .widthIn(max = EchoContentMaxWidth)
                                .fillMaxWidth(),
                        )
                    }
                },
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                ) {
                    key(selectedTab) {
                        when (EchoTab.entries[selectedTab]) {
                            EchoTab.Library -> LibraryScreen(
                                hasPermission = hasAudioPermission,
                                scanState = scanState,
                                tracks = tracks,
                                albums = albums,
                                artists = artists,
                                onRequestPermission = { permissionLauncher.launch(permission) },
                                onScan = viewModel::refreshLibrary,
                                onCancelScan = viewModel::cancelScan,
                                onPlayQueue = viewModel::playQueue,
                                onPlayAlbum = { album -> viewModel.playAlbum(album.albumKey) },
                                onPlayArtist = { artist -> viewModel.playArtist(artist.artistKey) },
                            )

                            EchoTab.Now -> NowPlayingScreen(
                                status = playbackStatus,
                                trackCount = libraryStats.trackCount,
                                albumCount = libraryStats.albumCount,
                                artistCount = libraryStats.artistCount,
                                recommendedTracks = homeRecommendedTracks,
                                onPlayPause = viewModel::playPause,
                                onNext = viewModel::skipNext,
                                onPrevious = viewModel::skipPrevious,
                                onCycleRepeatMode = viewModel::cycleRepeatMode,
                                onToggleShuffle = viewModel::toggleShuffle,
                                onRefreshRecommendations = viewModel::refreshLibrary,
                                onPlayRecommendation = viewModel::playQueue,
                                onOpenLibrary = { selectedTab = EchoTab.Library.ordinal },
                                onOpenConnect = { selectedTab = EchoTab.Connect.ordinal },
                            )

                            EchoTab.Connect -> ConnectScreen(
                                remoteState = remoteStatus.connectionState,
                                pcTitle = remoteStatus.endpoint?.name ?: "PC ECHO",
                                trackTitle = remoteStatus.playback.track?.title ?: "未连接",
                                trackArtist = remoteStatus.playback.track?.artist ?: "点按配对",
                                isPlaying = remoteStatus.playback.state == EchoRemotePlaybackState.Playing,
                                onPairDemo = {
                                    remoteClient.pair(
                                        EchoRemoteEndpoint(
                                            id = "echo-pc-demo",
                                            name = "PC ECHO 演示",
                                            host = "192.168.1.12",
                                            port = 26789,
                                            token = "demo-token-echo-remote",
                                        ),
                                    )
                                },
                                onPlayPause = { remoteClient.send(EchoRemoteCommand.PlayPause) },
                                onNext = { remoteClient.send(EchoRemoteCommand.Next) },
                                onDisconnect = remoteClient::disconnect,
                            )

                            EchoTab.Diagnostics -> DiagnosticsScreen(status = playbackStatus)
                        }
                    }
                }
            }
        }
    }
}
