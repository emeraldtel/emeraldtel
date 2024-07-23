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
package org.devsonics.activities.main.sidemenu.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.*
import org.devsonics.LinphoneApplication.Companion.coreContext
import org.devsonics.LinphoneApplication.Companion.corePreferences
import org.devsonics.R
import org.devsonics.activities.main.settings.SettingListenerStub
import org.devsonics.activities.main.settings.viewmodels.AccountSettingsViewModel
import org.devsonics.utils.LinphoneUtils
import org.linphone.core.*
import org.linphone.core.tools.Log

class SideMenuViewModel : ViewModel() {
    val registrationStatusText = MutableLiveData<Int>()
    val switchState = MutableLiveData<Boolean>()
    val registrationStatusDrawable = MutableLiveData<Int>()

    val voiceMailCount = MutableLiveData<Int>()

    val showAccounts: Boolean = corePreferences.showAccountsInSideMenu
    val showAssistant: Boolean = corePreferences.showAssistantInSideMenu
    val showSettings: Boolean = corePreferences.showSettingsInSideMenu
    val showRecordings: Boolean = corePreferences.showRecordingsInSideMenu
    val showScheduledConferences = MutableLiveData<Boolean>()
    val showAbout: Boolean = corePreferences.showAboutInSideMenu
    val showQuit: Boolean = corePreferences.showQuitInSideMenu

    val defaultAccountViewModel = MutableLiveData<AccountSettingsViewModel>()
    val defaultAccountFound = MutableLiveData<Boolean>()
    val defaultAccountAvatar = MutableLiveData<String>()

    val accounts = MutableLiveData<ArrayList<AccountSettingsViewModel>>()
    private var accountToDelete: Account? = null
    lateinit var accountsSettingsListener: SettingListenerStub

    private val listener: CoreListenerStub = object : CoreListenerStub() {
        override fun onAccountRegistrationStateChanged(
            core: Core,
            account: Account,
            state: RegistrationState,
            message: String,
        ) {
            accountToDelete = account
            // +1 is for the default account, otherwise this will trigger every time
            if (accounts.value.isNullOrEmpty() ||
                coreContext.core.accountList.size != accounts.value.orEmpty().size + 1
            ) {
                // Only refresh the list if an account has been added or removed
                updateAccountsList()
            }

            if (account == core.defaultAccount) {
                updateDefaultAccountRegistrationStatus(state)
            } else if (core.accountList.isEmpty()) {
                // Update registration status when default account is removed
                registrationStatusText.value = getStatusIconText(state)
                registrationStatusDrawable.value = getStatusIconResource(state)
                switchState.setValue(getStatusSwitch(state))
            }
        }

        override fun onNotifyReceived(
            core: Core,
            event: Event,
            notifiedEvent: String,
            body: Content,
        ) {
            if (body.type == "application" && body.subtype == "simple-message-summary" && body.size > 0) {
                val data = body.utf8Text?.lowercase(Locale.getDefault())
                val voiceMail = data?.split("voice-message: ")
                if ((voiceMail?.size ?: 0) >= 2) {
                    val toParse = voiceMail!![1].split("/", limit = 0)
                    try {
                        val unreadCount: Int = toParse[0].toInt()
                        voiceMailCount.value = unreadCount
                    } catch (nfe: NumberFormatException) {
                        Log.e("[Status Fragment] $nfe")
                    }
                }
            }
        }
    }

    init {
        val core = coreContext.core
        var state: RegistrationState = RegistrationState.None
        val defaultAccount = core.defaultAccount
        if (defaultAccount != null) {
            state = defaultAccount.state
        }
        updateDefaultAccountRegistrationStatus(state)

        defaultAccountFound.value = false
        defaultAccountAvatar.value = corePreferences.defaultAccountAvatarPath
        showScheduledConferences.value = corePreferences.showScheduledConferencesInSideMenu &&
            LinphoneUtils.isRemoteConferencingAvailable()
        coreContext.core.addListener(listener)
        updateAccountsList()
    }

    override fun onCleared() {
        defaultAccountViewModel.value?.destroy()
        accounts.value.orEmpty().forEach(AccountSettingsViewModel::destroy)
        coreContext.core.removeListener(listener)
        super.onCleared()
    }

    fun updateAccountsList() {
        defaultAccountFound.value = false // Do not assume a default account will still be found
        defaultAccountViewModel.value?.destroy()
        accounts.value.orEmpty().forEach(AccountSettingsViewModel::destroy)

        val list = arrayListOf<AccountSettingsViewModel>()
        val defaultAccount = coreContext.core.defaultAccount
        if (defaultAccount != null) {
            val defaultViewModel = AccountSettingsViewModel(defaultAccount)
            defaultViewModel.accountsSettingsListener = object : SettingListenerStub() {
                override fun onAccountClicked(identity: String) {
                    accountsSettingsListener.onAccountClicked(identity)
                }
            }
            defaultAccountViewModel.value = defaultViewModel
            defaultAccountFound.value = true
        }

        for (account in LinphoneUtils.getAccountsNotHidden()) {
            if (account != coreContext.core.defaultAccount) {
                val viewModel = AccountSettingsViewModel(account)
                viewModel.accountsSettingsListener = object : SettingListenerStub() {
                    override fun onAccountClicked(identity: String) {
                        accountsSettingsListener.onAccountClicked(identity)
                    }
                }
                list.add(viewModel)
            }
        }
        accounts.value = list

        showScheduledConferences.value = corePreferences.showScheduledConferencesInSideMenu &&
            LinphoneUtils.isRemoteConferencingAvailable()
    }

    fun setPictureFromPath(picturePath: String) {
        corePreferences.defaultAccountAvatarPath = picturePath
        defaultAccountAvatar.value = corePreferences.defaultAccountAvatarPath
        coreContext.contactsManager.updateLocalContacts()
    }

    fun refreshRegister() {
        coreContext.core.refreshRegisters()
    }

    fun updateDefaultAccountRegistrationStatus(state: RegistrationState) {
        registrationStatusText.value = getStatusIconText(state)
        registrationStatusDrawable.value = getStatusIconResource(state)
        switchState.value = getStatusSwitch(state)
    }

    fun onSwitchCheckedChanged(checked: Boolean) {
        switchState.value = checked

        deleteAccount(checked)
    }

    private fun getStatusIconText(state: RegistrationState): Int {
        return when (state) {
            RegistrationState.Ok -> R.string.status_connected
            RegistrationState.Progress -> R.string.status_in_progress
            RegistrationState.Failed -> R.string.status_error
            else -> R.string.status_not_connected
        }
    }

    private fun getStatusSwitch(state: RegistrationState): Boolean {
        return when (state) {
            RegistrationState.Ok -> true
            RegistrationState.Progress -> false
            RegistrationState.Failed -> false
            else -> false
        }
    }

    private fun getStatusIconResource(state: RegistrationState): Int {
        return when (state) {
            RegistrationState.Ok -> R.drawable.led_registered
            RegistrationState.Progress -> R.drawable.led_registration_in_progress
            RegistrationState.Failed -> R.drawable.led_error
            else -> R.drawable.led_not_registered
        }
    }

    private fun deleteAccount(checked: Boolean) {
        val core = coreContext.core

        if (checked) {
            var state: RegistrationState = RegistrationState.None
            val defaultAccount = core.defaultAccount
            if (defaultAccount != null) {
                state = defaultAccount.state
            }
            updateDefaultAccountRegistrationStatus(state)
        } else {
            var state: RegistrationState = RegistrationState.Cleared
            updateDefaultAccountRegistrationStatus(state)
        }

//        val core = coreContext.core
//        val authInfo = core.defaultAccount?.findAuthInfo()
//        if (authInfo != null) {
//            Log.i("[Account Settings] Found auth info $authInfo, removing it.")
//            coreContext.core.removeAuthInfo(authInfo)
//        } else {
//            Log.w("[Account Settings] Couldn't find matching auth info...")
//        }
//        if (core.defaultAccount != null)
//            coreContext.core.removeAccount(core.defaultAccount!!)
    }
}
