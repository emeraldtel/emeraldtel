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

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import org.devsonics.LinphoneApplication.Companion.coreContext
import org.devsonics.LinphoneApplication.Companion.corePreferences
import org.devsonics.R
import org.devsonics.activities.GenericFragment
import org.devsonics.activities.main.conference.viewmodels.ConferenceSchedulingViewModel
import org.devsonics.activities.navigateToSummary
import org.devsonics.contact.ContactsSelectionAdapter
import org.devsonics.databinding.ConferenceSchedulingParticipantsListFragmentBinding
import org.devsonics.utils.AppUtils
import org.devsonics.utils.PermissionHelper
import org.linphone.core.tools.Log

class ConferenceSchedulingParticipantsListFragment : GenericFragment<ConferenceSchedulingParticipantsListFragmentBinding>() {
    private val viewModel: ConferenceSchedulingViewModel by navGraphViewModels(R.id.conference_scheduling_nav_graph)
    private lateinit var adapter: ContactsSelectionAdapter

    override fun getLayoutId(): Int = R.layout.conference_scheduling_participants_list_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.viewModel = viewModel

        adapter = ContactsSelectionAdapter(viewLifecycleOwner)
        adapter.setLimeCapabilityRequired(viewModel.isEncrypted.value == true)
        binding.contactsList.adapter = adapter

        val layoutManager = LinearLayoutManager(requireContext())
        binding.contactsList.layoutManager = layoutManager

        // Divider between items
        binding.contactsList.addItemDecoration(AppUtils.getDividerDecoration(requireContext(), layoutManager))

        binding.setNextClickListener {
            navigateToSummary()
        }

        viewModel.contactsList.observe(
            viewLifecycleOwner
        ) {
            adapter.submitList(it)
        }
        viewModel.sipContactsSelected.observe(
            viewLifecycleOwner
        ) {
            viewModel.applyFilter()
        }

        viewModel.selectedAddresses.observe(
            viewLifecycleOwner
        ) {
            adapter.updateSelectedAddresses(it)
        }
        viewModel.filter.observe(
            viewLifecycleOwner
        ) {
            viewModel.applyFilter()
        }

        adapter.selectedContact.observe(
            viewLifecycleOwner
        ) {
            it.consume { searchResult ->
                viewModel.toggleSelectionForSearchResult(searchResult)
            }
        }

        if (corePreferences.enableNativeAddressBookIntegration) {
            if (!PermissionHelper.get().hasReadContactsPermission()) {
                Log.i("[Conference Creation] Asking for READ_CONTACTS permission")
                requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS), 0)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 0) {
            val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (granted) {
                Log.i("[Conference Creation] READ_CONTACTS permission granted")
                coreContext.fetchContacts()
            } else {
                Log.w("[Conference Creation] READ_CONTACTS permission denied")
            }
        }
    }
}
