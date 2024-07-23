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
package org.devsonics.activities.main.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.devsonics.LinphoneApplication.Companion.corePreferences
import org.devsonics.R
import org.devsonics.activities.GenericFragment
import org.devsonics.activities.main.viewmodels.TabsViewModel
import org.devsonics.activities.navigateToCallHistory
import org.devsonics.activities.navigateToChatRooms
import org.devsonics.activities.navigateToContacts
import org.devsonics.activities.navigateToDialer
import org.devsonics.databinding.TabsFragmentBinding
import org.devsonics.utils.Event

class TabsFragment :
    GenericFragment<TabsFragmentBinding>(),
    NavController.OnDestinationChangedListener {
    private lateinit var viewModel: TabsViewModel
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navController: NavController
    override fun getLayoutId(): Int = R.layout.tabs_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        useMaterialSharedAxisXForwardAnimation = false

        viewModel = requireActivity().run {
            ViewModelProvider(this)[TabsViewModel::class.java]
        }
        binding.viewModel = viewModel

        binding.setHistoryClickListener {
            when (findNavController().currentDestination?.id) {
                R.id.masterContactsFragment ->
                    sharedViewModel.updateContactsAnimationsBasedOnDestination.value =
                        Event(R.id.masterCallLogsFragment)
                R.id.dialerFragment ->
                    sharedViewModel.updateDialerAnimationsBasedOnDestination.value =
                        Event(R.id.masterCallLogsFragment)
            }
            navigateToCallHistory()
            setHistorySelected()
        }

        binding.setContactsClickListener {
            when (findNavController().currentDestination?.id) {
                R.id.dialerFragment ->
                    sharedViewModel.updateDialerAnimationsBasedOnDestination.value =
                        Event(R.id.masterContactsFragment)
            }
            sharedViewModel.updateContactsAnimationsBasedOnDestination.value =
                Event(findNavController().currentDestination?.id ?: -1)
            navigateToContacts()
            setContactsSelected()
        }

        binding.setDialerClickListener {
            when (findNavController().currentDestination?.id) {
                R.id.masterContactsFragment ->
                    sharedViewModel.updateContactsAnimationsBasedOnDestination.value =
                        Event(R.id.dialerFragment)
            }
            sharedViewModel.updateDialerAnimationsBasedOnDestination.value =
                Event(findNavController().currentDestination?.id ?: -1)
            navigateToDialer()
            setDialSelected()
        }

        binding.setChatClickListener {
            when (findNavController().currentDestination?.id) {
                R.id.masterContactsFragment ->
                    sharedViewModel.updateContactsAnimationsBasedOnDestination.value =
                        Event(R.id.masterChatRoomsFragment)
                R.id.dialerFragment ->
                    sharedViewModel.updateDialerAnimationsBasedOnDestination.value =
                        Event(R.id.masterChatRoomsFragment)
            }
            navigateToChatRooms()
            setMessageSelected()
        }
    }

    override fun onStart() {
        super.onStart()
        findNavController().addOnDestinationChangedListener(this)
    }

    override fun onStop() {
        findNavController().removeOnDestinationChangedListener(this)
        super.onStop()
    }

    fun setMessageSelected() {
        ImageViewCompat.setImageTintList(
            binding.chat,
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.icon_tint))
        )
        binding.tvMessage!!.setTextColor(resources.getColor(R.color.white))
        binding.tvContacts!!.setTextColor(resources.getColor(R.color.white_32))
        binding.tvDial!!.setTextColor(resources.getColor(R.color.white_32))
        binding.tvHistory!!.setTextColor(resources.getColor(R.color.white_32))
        ImageViewCompat.setImageTintList(
            binding.ivDial!!,
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.icon_tint_unselected
                )
            )
        )
        ImageViewCompat.setImageTintList(
            binding.ivContacts!!,
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.icon_tint_unselected
                )
            )
        )
        ImageViewCompat.setImageTintList(
            binding.history,
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.icon_tint_unselected
                )
            )
        )
    }

    fun setDialSelected() {
        ImageViewCompat.setImageTintList(
            binding.ivDial!!,
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.icon_tint))
        )
        binding.tvDial!!.setTextColor(resources.getColor(R.color.white))
        binding.tvContacts!!.setTextColor(resources.getColor(R.color.white_32))
        binding.tvMessage!!.setTextColor(resources.getColor(R.color.white_32))
        binding.tvHistory!!.setTextColor(resources.getColor(R.color.white_32))
        ImageViewCompat.setImageTintList(
            binding.chat,
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.icon_tint_unselected
                )
            )
        )
        ImageViewCompat.setImageTintList(
            binding.ivContacts!!,
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.icon_tint_unselected
                )
            )
        )
        ImageViewCompat.setImageTintList(
            binding.history,
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.icon_tint_unselected
                )
            )
        )
    }

    fun setContactsSelected() {
        ImageViewCompat.setImageTintList(
            binding.ivContacts!!,
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.icon_tint))
        )
        binding.tvContacts!!.setTextColor(resources.getColor(R.color.white))
        binding.tvDial!!.setTextColor(resources.getColor(R.color.white_32))
        binding.tvMessage!!.setTextColor(resources.getColor(R.color.white_32))
        binding.tvHistory!!.setTextColor(resources.getColor(R.color.white_32))
        ImageViewCompat.setImageTintList(
            binding.chat,
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.icon_tint_unselected
                )
            )
        )
        ImageViewCompat.setImageTintList(
            binding.ivDial!!,
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.icon_tint_unselected
                )
            )
        )
        ImageViewCompat.setImageTintList(
            binding.history,
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.icon_tint_unselected
                )
            )
        )
    }

    fun setHistorySelected() {
        ImageViewCompat.setImageTintList(
            binding.history,
            ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.icon_tint))
        )
        binding.tvHistory!!.setTextColor(resources.getColor(R.color.white))
        binding.tvDial!!.setTextColor(resources.getColor(R.color.white_32))
        binding.tvMessage!!.setTextColor(resources.getColor(R.color.white_32))
        binding.tvContacts!!.setTextColor(resources.getColor(R.color.white_32))
        ImageViewCompat.setImageTintList(
            binding.chat,
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.icon_tint_unselected
                )
            )
        )
        ImageViewCompat.setImageTintList(
            binding.ivDial!!,
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.icon_tint_unselected
                )
            )
        )
        ImageViewCompat.setImageTintList(
            binding.ivContacts!!,
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.icon_tint_unselected
                )
            )
        )
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?,
    ) {
        if (corePreferences.enableAnimations) {
            when (destination.id) {
                R.id.masterCallLogsFragment -> {
                    binding.motionLayout.transitionToState(R.id.call_history)
                    setHistorySelected()
                }
                R.id.masterContactsFragment -> {
                    binding.motionLayout.transitionToState(R.id.contacts)
                    setContactsSelected()
                }
                R.id.dialerFragment -> {
                    binding.motionLayout.transitionToState(R.id.dialer)
                    setDialSelected()
                }
                R.id.masterChatRoomsFragment -> {
                    binding.motionLayout.transitionToState(R.id.chat_rooms)
                    setMessageSelected()
                }
            }
        } else {
            when (destination.id) {
                R.id.masterCallLogsFragment -> {
                    binding.motionLayout.setTransition(
                        R.id.call_history,
                        R.id.call_history
                    )
                    setHistorySelected()
                }
                R.id.masterContactsFragment -> {
                    binding.motionLayout.setTransition(
                        R.id.contacts,
                        R.id.contacts
                    )
                    setContactsSelected()
                }
                R.id.dialerFragment -> {
                    binding.motionLayout.setTransition(R.id.dialer, R.id.dialer)
                    setDialSelected()
                }
                R.id.masterChatRoomsFragment -> {
                    binding.motionLayout.setTransition(
                        R.id.chat_rooms,
                        R.id.chat_rooms
                    )
                    setMessageSelected()
                }
            }
        }
    }
}
