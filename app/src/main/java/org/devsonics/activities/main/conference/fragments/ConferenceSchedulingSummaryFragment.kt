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
package org.devsonics.activities.main.conference.fragments

import android.os.Bundle
import android.view.View
import androidx.navigation.navGraphViewModels
import org.devsonics.R
import org.devsonics.activities.GenericFragment
import org.devsonics.activities.main.MainActivity
import org.devsonics.activities.main.conference.viewmodels.ConferenceSchedulingViewModel
import org.devsonics.activities.navigateToDialer
import org.devsonics.activities.navigateToScheduledConferences
import org.devsonics.databinding.ConferenceSchedulingSummaryFragmentBinding

class ConferenceSchedulingSummaryFragment : GenericFragment<ConferenceSchedulingSummaryFragmentBinding>() {
    private val viewModel: ConferenceSchedulingViewModel by navGraphViewModels(R.id.conference_scheduling_nav_graph)

    override fun getLayoutId(): Int = R.layout.conference_scheduling_summary_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        viewModel.conferenceCreationCompletedEvent.observe(
            viewLifecycleOwner
        ) {
            it.consume {
                if (viewModel.scheduleForLater.value == true) {
                    (requireActivity() as MainActivity).showSnackBar(R.string.conference_schedule_info_created)
                    navigateToScheduledConferences()
                } else {
                    navigateToDialer()
                }
            }
        }

        viewModel.onMessageToNotifyEvent.observe(
            viewLifecycleOwner
        ) {
            it.consume { messageId ->
                (activity as MainActivity).showSnackBar(messageId)
            }
        }

        viewModel.computeParticipantsData()
    }
}
