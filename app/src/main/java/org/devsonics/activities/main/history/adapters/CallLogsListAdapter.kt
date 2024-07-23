package org.devsonics.activities.main.history.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import org.devsonics.R
import org.devsonics.activities.main.adapters.SelectionListAdapter
import org.devsonics.activities.main.history.*
import org.devsonics.activities.main.history.data.GroupedCallLogData
import org.devsonics.activities.main.viewmodels.ListTopBarViewModel
import org.devsonics.databinding.GenericListHeaderBinding
import org.devsonics.databinding.HistoryListCellBinding
import org.devsonics.utils.Event
import org.devsonics.utils.HeaderAdapter
import org.devsonics.utils.TimestampUtils

class CallLogsListAdapter(
    selectionVM: ListTopBarViewModel,
    private val viewLifecycleOwner: LifecycleOwner
) : SelectionListAdapter<GroupedCallLogData, RecyclerView.ViewHolder>(
    selectionVM,
    CallLogDiffCallbackes()
),
    HeaderAdapter {
    val selectedCallLogEvent: MutableLiveData<Event<GroupedCallLogData>> by lazy {
        MutableLiveData<Event<GroupedCallLogData>>()
    }

    val startCallToEvent: MutableLiveData<Event<GroupedCallLogData>> by lazy {
        MutableLiveData<Event<GroupedCallLogData>>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding: HistoryListCellBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.history_list_cell, parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).bind(getItem(position))
    }

    inner class ViewHolder(
        val binding: HistoryListCellBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(callLogGroup: GroupedCallLogData) {
            with(binding) {
                val callLogViewModel = callLogGroup.lastCallLogViewModel
                viewModel = callLogViewModel

                lifecycleOwner = viewLifecycleOwner

                // This is for item selection through ListTopBarFragment
                selectionListViewModel = selectionViewModel
                selectionViewModel.isEditionEnabled.observe(
                    viewLifecycleOwner
                ) {
                    position = bindingAdapterPosition
                }

                setClickListener {
                    if (selectionViewModel.isEditionEnabled.value == true) {
                        selectionViewModel.onToggleSelect(bindingAdapterPosition)
                    } else {
                        startCallToEvent.value = Event(callLogGroup)
                    }
                }

                setLongClickListener {
                    if (selectionViewModel.isEditionEnabled.value == false) {
                        selectionViewModel.isEditionEnabled.value = true
                        // Selection will be handled by click listener
                        true
                    }
                    false
                }

                // This listener is disabled when in edition mode
                setDetailsClickListener {
                    selectedCallLogEvent.value = Event(callLogGroup)
                }

                groupCount = callLogGroup.callLogs.size

                executePendingBindings()
            }
        }
    }

    override fun displayHeaderForPosition(position: Int): Boolean {
        if (position >= itemCount) return false
        val callLogGroup = getItem(position)
        val date = callLogGroup.lastCallLog.startDate
        val previousPosition = position - 1
        return if (previousPosition >= 0) {
            val previousItemDate = getItem(previousPosition).lastCallLog.startDate
            !TimestampUtils.isSameDay(date, previousItemDate)
        } else true
    }

    override fun getHeaderViewForPosition(context: Context, position: Int): View {
        val callLog = getItem(position)
        val date = formatDate(context, callLog.lastCallLog.startDate)
        val binding: GenericListHeaderBinding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.generic_list_header, null, false
        )
        binding.title = date
        binding.executePendingBindings()
        return binding.root
    }

    private fun formatDate(context: Context, date: Long): String {
        if (TimestampUtils.isToday(date)) {
            return context.getString(R.string.today)
        } else if (TimestampUtils.isYesterday(date)) {
            return context.getString(R.string.yesterday)
        }
        return TimestampUtils.toString(date, onlyDate = true, shortDate = false, hideYear = false)
    }
}
private class CallLogDiffCallbackes : DiffUtil.ItemCallback<org.devsonics.activities.main.history.data.GroupedCallLogData>() {
    override fun areItemsTheSame(
        oldItem: org.devsonics.activities.main.history.data.GroupedCallLogData,
        newItem: org.devsonics.activities.main.history.data.GroupedCallLogData
    ): Boolean {
        return oldItem.lastCallLog.callId == newItem.lastCallLog.callId
    }

    override fun areContentsTheSame(
        oldItem: org.devsonics.activities.main.history.data.GroupedCallLogData,
        newItem: org.devsonics.activities.main.history.data.GroupedCallLogData
    ): Boolean {
        return oldItem.callLogs.size == newItem.callLogs.size
    }
}
