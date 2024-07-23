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
package org.devsonics.activities.main.adapters

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.devsonics.activities.main.viewmodels.ListTopBarViewModel

abstract class SelectionListAdapter<T, VH : RecyclerView.ViewHolder>(
    selectionVM: ListTopBarViewModel,
    diff: DiffUtil.ItemCallback<T>
) :
    ListAdapter<T, VH>(diff) {

    private var _selectionViewModel: ListTopBarViewModel? = selectionVM
    protected val selectionViewModel get() = _selectionViewModel!!

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        _selectionViewModel = null
    }
}
