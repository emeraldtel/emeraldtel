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
package org.devsonics.activities.main.sidemenu.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import java.io.File
import kotlinx.coroutines.launch
import org.devsonics.LinphoneApplication.Companion.coreContext
import org.devsonics.R
import org.devsonics.activities.*
import org.devsonics.activities.assistant.AssistantActivity
import org.devsonics.activities.main.settings.SettingListenerStub
import org.devsonics.activities.main.sidemenu.viewmodels.SideMenuViewModel
import org.devsonics.databinding.SideMenuFragmentBinding
import org.devsonics.utils.Event
import org.devsonics.utils.FileUtils
import org.devsonics.utils.PermissionHelper
import org.linphone.core.Account
import org.linphone.core.RegistrationState
import org.linphone.core.tools.Log

class SideMenuFragment : GenericFragment<SideMenuFragmentBinding>() {
    private lateinit var viewModel: SideMenuViewModel
    private var temporaryPicturePath: File? = null
    private var accountToDelete: Account? = null

    override fun getLayoutId(): Int = R.layout.side_menu_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = viewLifecycleOwner
        // Get the value of the margin in sdp
        val marginInSdp = 0f
        val marginInPixels = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, marginInSdp, resources.displayMetrics
        ).toInt()

        // Get the layout params of the view you want to set the margin for
        val layoutParams = view.layoutParams as MarginLayoutParams

//        // Set the bottom margin
//        layoutParams.bottomMargin = marginInPixels

        sharedViewModel.isDestroyFragment.observe(
            requireActivity()
        ) {
            it.consume {
                println("why this kola ")
                val marginInSdpd = 97f
                val marginInPixelsd = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, marginInSdpd, resources.displayMetrics
                ).toInt()
                val layoutParamsd = view.layoutParams as MarginLayoutParams
                layoutParamsd.bottomMargin = marginInPixelsd
                binding.rlSideMenuMain.layoutParams = layoutParamsd
            }
        }

        viewModel = ViewModelProvider(this)[SideMenuViewModel::class.java]
        binding.viewModel = viewModel

        sharedViewModel.accountRemoved.observe(
            viewLifecycleOwner
        ) {
            Log.i("[Side Menu] Account removed, update accounts list")
            viewModel.updateAccountsList()
        }

        sharedViewModel.defaultAccountChanged.observe(
            viewLifecycleOwner
        ) {
            Log.i("[Side Menu] Default account changed, update accounts list")
            viewModel.updateAccountsList()
        }

        viewModel.accountsSettingsListener = object : SettingListenerStub() {
            override fun onAccountClicked(identity: String) {
                val args = Bundle()
                args.putString("Identity", identity)
                Log.i("[Side Menu] Navigation to settings for account with identity: $identity")

                sharedViewModel.toggleDrawerEvent.value = Event(true)
                navigateToAccountSettings(identity)
            }
        }

        binding.setSelfPictureClickListener {
            pickFile()
        }
        binding.setRefreshClickListener {
            viewModel.refreshRegister()
        }
        binding.setAssistantClickListener {

            sharedViewModel.toggleDrawerEvent.value = Event(true)
            startActivity(Intent(context, AssistantActivity::class.java))
        }

        binding.setSettingsClickListener {
            layoutParams.bottomMargin = marginInPixels
            binding.rlSideMenuMain.layoutParams = layoutParams
            sharedViewModel.toggleDrawerEvent.value = Event(true)
            navigateToSettings()
        }

        binding.setRecordingsClickListener {
            layoutParams.bottomMargin = marginInPixels
            binding.rlSideMenuMain.layoutParams = layoutParams
            sharedViewModel.toggleDrawerEvent.value = Event(true)
            navigateToRecordings()
        }

        binding.setAboutClickListener {
            layoutParams.bottomMargin = marginInPixels
            binding.rlSideMenuMain.layoutParams = layoutParams

            sharedViewModel.toggleDrawerEvent.value = Event(true)
            navigateToAbout()
        }

        binding.setLogoutClickListener {
            layoutParams.bottomMargin = marginInPixels
            binding.rlSideMenuMain.layoutParams = layoutParams

            sharedViewModel.toggleDrawerEvent.value = Event(true)
            val account: Account? = coreContext.core.defaultAccount
            val core = coreContext.core
            accountToDelete = account

            val registered = account?.state == RegistrationState.Ok

            if (core.defaultAccount == account) {
                Log.i("[Account Settings] Account was default, let's look for a replacement")
                for (accountIterator in core.accountList) {
                    if (account != accountIterator) {
                        core.defaultAccount = accountIterator
                        Log.i("[Account Settings] New default account is $accountIterator")
                        break
                    }
                }
            }

            val params = account?.params?.clone()
            params?.isRegisterEnabled = false
            if (params != null) {
                account?.params = params
            }

            if (!registered) {
                Log.w("[Account Settings] Account isn't registered, don't unregister before removing it")
                account?.let { it1 -> deleteAccount(it1) }
            }
        }

        binding.setConferencesClickListener {
            binding.rlSideMenuMain.layoutParams = layoutParams
            sharedViewModel.toggleDrawerEvent.value = Event(true)
            navigateToScheduledConferences()
        }

        binding.setQuitClickListener {
            Log.i("[Side Menu] Quitting app")
            requireActivity().finishAndRemoveTask()

            Log.i("[Side Menu] Stopping Core Context")
            coreContext.notificationsManager.stopForegroundNotification()
            coreContext.stop()
        }
    }
    private fun deleteAccount(account: Account) {
        val core = coreContext.core
        val authInfo = account.findAuthInfo()
        if (authInfo != null) {
            Log.i("[Account Settings] Found auth info $authInfo, removing it.")
            core.removeAuthInfo(authInfo)
        } else {
            Log.w("[Account Settings] Couldn't find matching auth info...")
        }

        core.removeAccount(account)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            lifecycleScope.launch {
                val contactImageFilePath =
                    FileUtils.getFilePathFromPickerIntent(data, temporaryPicturePath)
                if (contactImageFilePath != null) {
                    viewModel.setPictureFromPath(contactImageFilePath)
                }
            }
        }
    }

    private fun pickFile() {
        val cameraIntents = ArrayList<Intent>()

        // Handles image picking
        val galleryIntent = Intent(Intent.ACTION_PICK)
        galleryIntent.type = "image/*"

        if (PermissionHelper.get().hasCameraPermission()) {
            // Allows to capture directly from the camera
            val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            val tempFileName = System.currentTimeMillis().toString() + ".jpeg"
            val file = FileUtils.getFileStoragePath(tempFileName)
            temporaryPicturePath = file
            val publicUri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().getString(R.string.file_provider),
                file
            )
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, publicUri)
            captureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            cameraIntents.add(captureIntent)
        }

        val chooserIntent =
            Intent.createChooser(galleryIntent, getString(R.string.chat_message_pick_file_dialog))
        chooserIntent.putExtra(
            Intent.EXTRA_INITIAL_INTENTS,
            cameraIntents.toArray(arrayOf<Parcelable>())
        )

        startActivityForResult(chooserIntent, 0)
    }
}
