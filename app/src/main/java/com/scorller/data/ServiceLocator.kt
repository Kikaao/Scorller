package com.scorller.data

import android.content.Context
import com.scorller.data.linkpreview.LinkPreviewFetcher
import com.scorller.data.local.LocalIdentityManager
import com.scorller.data.repository.SessionRepository

/** Simple singleton holder for app-wide dependencies, used instead of a DI framework. */
object ServiceLocator {
    val sessionRepository: SessionRepository by lazy { SessionRepository() }
    val linkPreviewFetcher: LinkPreviewFetcher by lazy { LinkPreviewFetcher() }

    private var identityManager: LocalIdentityManager? = null

    fun identityManager(context: Context): LocalIdentityManager =
        identityManager ?: LocalIdentityManager(context.applicationContext).also { identityManager = it }
}
