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
package org.devsonics.activities.main.settings.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import org.devsonics.R
import org.devsonics.activities.main.settings.viewmodels.TunnelSettingsViewModel
import org.devsonics.databinding.SettingsTunnelFragmentBinding

class TunnelSettingsFragment : GenericSettingFragment<SettingsTunnelFragmentBinding>() {
    private lateinit var viewModel: TunnelSettingsViewModel

    override fun getLayoutId(): Int = R.layout.settings_tunnel_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.sharedMainViewModel = sharedViewModel

        viewModel = ViewModelProvider(this)[TunnelSettingsViewModel::class.java]
        binding.viewModel = viewModel
    }
}
