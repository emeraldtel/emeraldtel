/*
 * Copyright (c) 2010-2021 Emerald SARL.
 *
 * This file is part of linphone-android
 * (see https://www.linphone.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.devsonics.activities.main.chat.data

import org.devsonics.contact.GenericContactData
import org.linphone.core.EventLog

class EventLogData(val eventLog: EventLog) {
    val type: EventLog.Type = eventLog.type

    val notifyId = eventLog.notifyId

    val data: GenericContactData = if (type == EventLog.Type.ConferenceChatMessage) {
        ChatMessageData(eventLog.chatMessage!!)
    } else {
        EventData(eventLog)
    }

    fun destroy() {
        data.destroy()
    }
}
