/*
 *             Twidere - Twitter client for Android
 *
 *  Copyright (C) 2012-2017 Mariotaku Lee <mariotaku.lee@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mariotaku.twidere.data.fetcher

import org.mariotaku.microblog.library.MicroBlog
import org.mariotaku.microblog.library.MicroBlogException
import org.mariotaku.microblog.library.mastodon.Mastodon
import org.mariotaku.microblog.library.twitter.model.Paging
import org.mariotaku.microblog.library.twitter.model.Status
import org.mariotaku.microblog.library.twitter.model.TimelineOption
import org.mariotaku.twidere.alias.MastodonStatus
import org.mariotaku.twidere.alias.MastodonTimelineOption
import org.mariotaku.twidere.exception.RequiredFieldNotFoundException
import org.mariotaku.twidere.model.AccountDetails
import org.mariotaku.twidere.model.UserKey
import org.mariotaku.twidere.model.timeline.TimelineFilter
import org.mariotaku.twidere.model.timeline.UserTimelineFilter

class UserTimelineFetcher(
        private val userKey: UserKey?,
        private val userScreenName: String?
) : StatusesFetcher {

    override fun forTwitter(account: AccountDetails, twitter: MicroBlog, paging: Paging, filter: TimelineFilter?): List<Status> {
        return getMicroBlogUserFavorites(twitter, paging, filter)
    }

    override fun forStatusNet(account: AccountDetails, statusNet: MicroBlog, paging: Paging, filter: TimelineFilter?): List<Status> {
        return getMicroBlogUserFavorites(statusNet, paging, filter)
    }

    override fun forFanfou(account: AccountDetails, fanfou: MicroBlog, paging: Paging, filter: TimelineFilter?): List<Status> {
        return getMicroBlogUserFavorites(fanfou, paging, filter)
    }

    override fun forMastodon(account: AccountDetails, mastodon: Mastodon, paging: Paging, filter: TimelineFilter?): List<MastodonStatus> {
        val id = userKey?.id ?: throw MicroBlogException("Only ID are supported at this moment")
        val option = (filter as? UserTimelineFilter)?.toMastodonTimelineOption()
        return mastodon.getStatuses(id, paging, option)
    }

    private fun getMicroBlogUserFavorites(microBlog: MicroBlog, paging: Paging, filter: TimelineFilter?): List<Status> {
        val option = (filter as? UserTimelineFilter)?.toTwitterTimelineOption()
        return when {
            userKey != null -> microBlog.getUserTimeline(userKey.id, paging, option)
            userScreenName != null -> microBlog.getUserTimelineByScreenName(userScreenName, paging, option)
            else -> throw RequiredFieldNotFoundException("user_id", "screen_name")
        }
    }

    private fun UserTimelineFilter.toTwitterTimelineOption() = TimelineOption().apply {
        setExcludeReplies(!isIncludeReplies)
        setIncludeRetweets(isIncludeRetweets)
    }

    private fun UserTimelineFilter.toMastodonTimelineOption() = MastodonTimelineOption().apply {
        excludeReplies(!isIncludeReplies)
    }
}