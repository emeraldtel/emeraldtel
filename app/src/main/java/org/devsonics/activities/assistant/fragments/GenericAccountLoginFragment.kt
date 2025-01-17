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
package org.devsonics.activities.assistant.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import org.devsonics.LinphoneApplication.Companion.coreContext
import org.devsonics.LinphoneApplication.Companion.corePreferences
import org.devsonics.R
import org.devsonics.activities.GenericFragment
import org.devsonics.activities.assistant.AssistantActivity
import org.devsonics.activities.assistant.viewmodels.GenericLoginViewModel
import org.devsonics.activities.assistant.viewmodels.GenericLoginViewModelFactory
import org.devsonics.activities.assistant.viewmodels.SharedAssistantViewModel
import org.devsonics.activities.main.viewmodels.DialogViewModel
import org.devsonics.activities.navigateToEchoCancellerCalibration
import org.devsonics.databinding.AssistantGenericAccountLoginFragmentBinding
import org.devsonics.utils.DialogUtils

class GenericAccountLoginFragment : GenericFragment<AssistantGenericAccountLoginFragmentBinding>() {
    private lateinit var sharedAssistantViewModel: SharedAssistantViewModel
    private lateinit var viewModel: GenericLoginViewModel

    override fun getLayoutId(): Int = R.layout.assistant_generic_account_login_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner

        sharedAssistantViewModel = requireActivity().run {
            ViewModelProvider(this)[SharedAssistantViewModel::class.java]
        }

        viewModel = ViewModelProvider(this, GenericLoginViewModelFactory(sharedAssistantViewModel.getAccountCreator(true)))[GenericLoginViewModel::class.java]
        binding.viewModel = viewModel

        viewModel.leaveAssistantEvent.observe(
            viewLifecycleOwner
        ) {
            it.consume {
                val isLinphoneAccount = viewModel.domain.value.orEmpty() == corePreferences.defaultDomain
                coreContext.newAccountConfigured(isLinphoneAccount)

                if (coreContext.core.isEchoCancellerCalibrationRequired) {
                    navigateToEchoCancellerCalibration()
                } else {
                    requireActivity().finish()
                }
            }
        }
        viewModel.dismiss.observe(
            viewLifecycleOwner
        ) {
            activity?.finish()
        }
        viewModel.invalidCredentialsEvent.observe(
            viewLifecycleOwner
        ) {
            it.consume {
                val dialogViewModel =
                    DialogViewModel(getString(R.string.assistant_error_invalid_credentials))
                val dialog: Dialog = DialogUtils.getDialog(requireContext(), dialogViewModel)

                dialogViewModel.showCancelButton {
                    viewModel.removeInvalidProxyConfig()
                    dialog.dismiss()
                }

                dialogViewModel.showDeleteButton(
                    {
                        viewModel.continueEvenIfInvalidCredentials()
                        dialog.dismiss()
                    },
                    getString(R.string.assistant_continue_even_if_credentials_invalid)
                )

                dialog.show()
            }
        }

        viewModel.onErrorEvent.observe(
            viewLifecycleOwner
        ) {
            it.consume { message ->
                (requireActivity() as AssistantActivity).showSnackBar(message)
            }
        }
    }
}
