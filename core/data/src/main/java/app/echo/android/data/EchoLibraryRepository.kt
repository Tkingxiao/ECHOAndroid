package app.echo.android.data

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import app.echo.android.model.library.AlbumSortMode
import app.echo.android.model.library.AlbumSummary
import app.echo.android.model.library.ArtistSortMode
import app.echo.android.model.library.ArtistSummary
import app.echo.android.model.library.LibraryScanPhase
import app.echo.android.model.library.LibraryScanProgress
import app.echo.android.model.library.LibrarySource
import app.echo.android.model.library.LibraryStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.coroutineContext

class EchoLibraryRepository(
    private val database: EchoLibraryDatabase,
    private val scanner: MediaStoreTrackScanner,
) {
    fun pagedTracks(query: String? = null): Flow<PagingData<LibraryTrackEntity>> =
        flow {
            val dao = database.trackDao()
            val trimmedQuery = query?.trim().orEmpty()
            val matchQuery = sanitizeFtsQuery(trimmedQuery)
            val rankQuery = ftsRankQuery(trimmedQuery)
            val pagingSourceFactory = when {
                trimmedQuery.isBlank() -> {
                    { dao.pageTracks() }
                }
                matchQuery == null -> {
                    { dao.pageTracksByLike(trimmedQuery, rankQuery) }
                }
                canUseFts(dao, matchQuery) -> {
                    { dao.pageTracksByFts(matchQuery, rankQuery) }
                }
                else -> {
                    { dao.pageTracksByLike(trimmedQuery, rankQuery) }
                }
            }

            emitAll(
                Pager(
                    config = PagingConfig(
                        pageSize = 60,
                        prefetchDistance = 20,
                        enablePlaceholders = false,
                    ),
                    pagingSourceFactory = pagingSourceFactory,
                ).flow,
            )
        }.flowOn(Dispatchers.IO)

    fun observeLibraryStats(): Flow<LibraryStats> =
        database.trackDao().observeLibraryStats()
            .flowOn(Dispatchers.IO)

    fun observeRecommendedTracks(limit: Int = RecommendedTrackLimit): Flow<List<LibraryTrackEntity>> =
        database.trackDao().observeRecommendedTracks(limit)
            .flowOn(Dispatchers.IO)

    fun pagedAlbums(
        query: String? = null,
        sort: AlbumSortMode = AlbumSortMode.Title,
    ): Flow<PagingData<AlbumSummary>> =
        Pager(
            config = defaultPagingConfig(),
            pagingSourceFactory = {
                database.trackDao().pageAlbums(query?.trim()?.takeIf { it.isNotBlank() }, sort.name)
            },
        ).flow

    fun pagedArtists(
        query: String? = null,
        sort: ArtistSortMode = ArtistSortMode.Name,
    ): Flow<PagingData<ArtistSummary>> =
        Pager(
            config = defaultPagingConfig(),
            pagingSourceFactory = {
                database.trackDao().pageArtists(query?.trim()?.takeIf { it.isNotBlank() }, sort.name)
            },
        ).flow

    fun pagedAlbumTracks(albumKey: String): Flow<PagingData<LibraryTrackEntity>> =
        Pager(
            config = defaultPagingConfig(),
            pagingSourceFactory = { database.trackDao().pageTracksByAlbum(albumKey) },
        ).flow

    fun pagedArtistTracks(artistKey: String): Flow<PagingData<LibraryTrackEntity>> =
        Pager(
            config = defaultPagingConfig(),
            pagingSourceFactory = { database.trackDao().pageTracksByArtist(artistKey) },
        ).flow

    suspend fun albumTracks(albumKey: String): List<LibraryTrackEntity> =
        database.trackDao().getTracksByAlbum(albumKey)

    suspend fun artistTracks(artistKey: String): List<LibraryTrackEntity> =
        database.trackDao().getTracksByArtist(artistKey)

    fun refreshMediaStoreSnapshot(batchSize: Int = ScanBatchSize): Flow<LibraryScanProgress> = flow {
        val dao = database.trackDao()
        val source = LibrarySource.MediaStore.id
        val scanRunId = System.currentTimeMillis()
        var progress = LibraryScanProgress(phase = LibraryScanPhase.Preparing)
        var insertedCount = 0
        var updatedCount = 0
        var scannedCount = 0
        var lastProgressEmitCount = 0

        suspend fun emitProgress(
            phase: LibraryScanPhase = progress.phase,
            currentTitle: String? = progress.currentTitle,
            deletedCount: Int = progress.deletedCount,
            error: String? = null,
            isCompleted: Boolean = false,
        ) {
            progress = LibraryScanProgress(
                phase = phase,
                scannedCount = scannedCount,
                insertedCount = insertedCount,
                updatedCount = updatedCount,
                deletedCount = deletedCount,
                totalCount = null,
                currentTitle = currentTitle,
                error = error,
                isCompleted = isCompleted,
            )
            emit(progress)
        }

        try {
            emitProgress()
            coroutineContext.ensureActive()

            emitProgress(phase = LibraryScanPhase.Diffing)
            val existingFingerprints = dao.getExistingMediaStoreFingerprints(source)
                .associateBy(TrackFingerprint::id)

            emitProgress(phase = LibraryScanPhase.QueryingMediaStore)
            scanner.scanAudio(
                batchSize = batchSize,
                onProgress = { count, currentTrack ->
                    scannedCount = count
                    if (count == 0 || count - lastProgressEmitCount >= ProgressEmitStride) {
                        lastProgressEmitCount = count
                        emitProgress(
                            phase = LibraryScanPhase.QueryingMediaStore,
                            currentTitle = currentTrack?.title,
                        )
                    }
                },
                onBatch = { batch ->
                    coroutineContext.ensureActive()
                    val inserts = ArrayList<LibraryTrackEntity>(batch.size)
                    val updates = ArrayList<LibraryTrackEntity>(batch.size)
                    val unchangedIds = ArrayList<String>(batch.size)

                    batch.forEach { rawTrack ->
                        val track = rawTrack.withScanMetadata(scanRunId)
                        val existing = existingFingerprints[track.id]
                        when {
                            existing == null -> inserts += track
                            existing.fingerprint != track.fingerprint -> updates += track
                            else -> unchangedIds += track.id
                        }
                    }

                    emitProgress(phase = LibraryScanPhase.WritingDatabase)
                    (inserts + updates).chunked(DatabaseBatchSize).forEach { chunk ->
                        dao.upsertBatchWithFts(chunk)
                    }
                    unchangedIds.chunked(DatabaseBatchSize).forEach { ids ->
                        dao.markSeen(ids, scanRunId)
                    }
                    insertedCount += inserts.size
                    updatedCount += updates.size
                    lastProgressEmitCount = scannedCount
                    emitProgress(phase = LibraryScanPhase.QueryingMediaStore)
                },
            )

            coroutineContext.ensureActive()
            emitProgress(phase = LibraryScanPhase.CleaningRemoved)
            val missingTrackIds = dao.getMissingTrackIdsFromSource(source, scanRunId)
            val deletedCount = dao.deleteMissingFromSource(source, scanRunId)
            missingTrackIds.chunked(DatabaseBatchSize).forEach { trackIds ->
                dao.deleteFtsByTrackIds(trackIds)
            }
            emitProgress(
                phase = LibraryScanPhase.Completed,
                currentTitle = null,
                deletedCount = deletedCount,
                isCompleted = true,
            )
        } catch (error: CancellationException) {
            emitProgress(
                phase = LibraryScanPhase.Cancelled,
                currentTitle = null,
                isCompleted = true,
            )
            throw error
        } catch (error: Throwable) {
            emitProgress(
                phase = LibraryScanPhase.Error,
                currentTitle = null,
                error = error.message ?: "曲库扫描失败",
                isCompleted = true,
            )
        }
    }.flowOn(Dispatchers.IO)

    suspend fun countTracks(): Int = database.trackDao().countTracks()

    private suspend fun canUseFts(dao: LibraryTrackDao, matchQuery: String): Boolean =
        runCatching {
            dao.validateFtsQuery(matchQuery)
            true
        }.onFailure { error ->
            Log.w(TAG, "FTS search failed; falling back to LIKE query.", error)
        }.getOrDefault(false)

    private fun defaultPagingConfig(): PagingConfig =
        PagingConfig(
            pageSize = 60,
            prefetchDistance = 20,
            enablePlaceholders = false,
        )

    private companion object {
        const val TAG = "EchoLibraryRepository"
        const val ScanBatchSize = 500
        const val DatabaseBatchSize = 500
        const val ProgressEmitStride = 100
        const val RecommendedTrackLimit = 8
    }
}
