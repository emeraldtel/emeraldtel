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
package org.devsonics.activities.main.settings.fragments

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import org.devsonics.activities.GenericFragment

abstract class GenericSettingFragment<T : ViewDataBinding> : GenericFragment<T>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        useMaterialSharedAxisXForwardAnimation = sharedViewModel.isSlidingPaneSlideable.value == false

        super.onViewCreated(view, savedInstanceState)
    }
}
