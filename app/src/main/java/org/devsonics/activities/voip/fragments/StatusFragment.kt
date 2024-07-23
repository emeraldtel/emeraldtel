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
package org.devsonics.activities.voip.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.navGraphViewModels
import java.util.*
import org.devsonics.R
import org.devsonics.activities.GenericFragment
import org.devsonics.activities.main.viewmodels.DialogViewModel
import org.devsonics.activities.voip.viewmodels.ControlsViewModel
import org.devsonics.activities.voip.viewmodels.StatusViewModel
import org.devsonics.databinding.VoipStatusFragmentBinding
import org.devsonics.utils.DialogUtils
import org.linphone.core.Call
import org.linphone.core.tools.Log

class StatusFragment : GenericFragment<VoipStatusFragmentBinding>() {
    private lateinit var viewModel: StatusViewModel
    private val controlsViewModel: ControlsViewModel by navGraphViewModels(R.id.call_nav_graph)

    private var zrtpDialog: Dialog? = null

    override fun getLayoutId(): Int = R.layout.voip_status_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        useMaterialSharedAxisXForwardAnimation = false

        viewModel = ViewModelProvider(this)[StatusViewModel::class.java]
        binding.viewModel = viewModel

        binding.setRefreshClickListener {
            viewModel.refreshRegister()
        }

        viewModel.showZrtpDialogEvent.observe(
            viewLifecycleOwner
        ) {
            it.consume { call ->
                if (call.state == Call.State.Connected || call.state == Call.State.StreamsRunning) {
                    showZrtpDialog(call)
                }
            }
        }

        viewModel.showCallStatsEvent.observe(
            viewLifecycleOwner
        ) {
            it.consume {
                controlsViewModel.showCallStats(skipAnimation = true)
            }
        }
    }

    override fun onDestroy() {
        if (zrtpDialog != null) {
            zrtpDialog?.dismiss()
        }
        super.onDestroy()
    }

    private fun showZrtpDialog(call: Call) {
        if (zrtpDialog != null && zrtpDialog?.isShowing == true) {
            Log.e("[Status Fragment] ZRTP dialog already visible")
            return
        }

        val token = call.authenticationToken
        if (token == null || token.length < 4) {
            Log.e("[Status Fragment] ZRTP token is invalid: $token")
            return
        }

        val toRead: String
        val toListen: String
        when (call.dir) {
            Call.Dir.Incoming -> {
                toRead = token.substring(0, 2)
                toListen = token.substring(2)
            }
            else -> {
                toRead = token.substring(2)
                toListen = token.substring(0, 2)
            }
        }

        val viewModel = DialogViewModel(getString(R.string.zrtp_dialog_explanation), getString(R.string.zrtp_dialog_title))
        viewModel.showZrtp = true
        viewModel.zrtpReadSas = toRead.uppercase(Locale.getDefault())
        viewModel.zrtpListenSas = toListen.uppercase(Locale.getDefault())
        viewModel.showIcon = true
        viewModel.iconResource = if (call.audioStats?.isZrtpKeyAgreementAlgoPostQuantum == true) {
            R.drawable.security_post_quantum
        } else {
            R.drawable.security_2_indicator
        }

        val dialog: Dialog = DialogUtils.getVoipDialog(requireContext(), viewModel)

        viewModel.showCancelButton(
            {
                call.authenticationTokenVerified = false
                this@StatusFragment.viewModel.updateEncryptionInfo(call)
                dialog.dismiss()
                zrtpDialog = null
            },
            getString(R.string.zrtp_dialog_later_button_label)
        )

        viewModel.showOkButton(
            {
                call.authenticationTokenVerified = true
                this@StatusFragment.viewModel.updateEncryptionInfo(call)
                dialog.dismiss()
                zrtpDialog = null
            },
            getString(R.string.zrtp_dialog_correct_button_label)
        )

        viewModel.dismissEvent.observe(viewLifecycleOwner) {
            it.consume {
                dialog.dismiss()
            }
        }

        zrtpDialog = dialog
        dialog.show()
    }
}
