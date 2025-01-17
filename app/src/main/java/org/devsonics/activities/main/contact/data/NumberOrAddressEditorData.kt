/*
 * Copyright (c) 2010-2020 Emerald SARL.
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
package org.devsonics.activities.main.contact.data

import androidx.lifecycle.MutableLiveData

class NumberOrAddressEditorData(
    val currentValue: String,
    val isSipAddress: Boolean,
    val contactEditorData: ContactEditorData,
) {
    val newValue = MutableLiveData<String>()

    val toRemove = MutableLiveData<Boolean>()

    init {
        newValue.value = currentValue
        toRemove.value = false
    }

    fun addEmptySipAddress() {
        contactEditorData.addEmptySipAddress()
    }

    fun addEmptyNumber() {
        contactEditorData.addEmptyPhoneNumber()
    }

    fun remove() {
        println("value of data" + contactEditorData.getAddressSize())
        if (isSipAddress) {
            if (contactEditorData.getAddressSize()) {
                toRemove.value = true
                contactEditorData.sipAddressesCount--
            }
        } else {
            if (contactEditorData.getNumberSize()) {
                toRemove.value = true
                contactEditorData.phoneNumberCount--
            }
        }
    }
}
