package com.example.softwareupdate.ui.fragments.appUsage

import CustomSpinnerAdapter
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.softwareupdate.R
import com.example.softwareupdate.databinding.FragmentAppUsageBinding
import com.example.softwareupdate.utils.Logs


class AppUsageFragment : Fragment() {
    private var _binding:FragmentAppUsageBinding?=null
    private val binding get() = _binding
    private var strMsg = ""
    private lateinit var smallInfoList: ArrayList<AppUsageInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAppUsageBinding.inflate(inflater,container,false)
        val items = listOf("Daily", "Weekly")
        val adapter = CustomSpinnerAdapter(requireContext(), items)
        binding?.spinner?.adapter = adapter
        binding?.spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = items[position]
                // Handle selected item here
                Toast.makeText(requireContext(), "Selected: $selectedItem", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val hourInMillis: Long = 1000 * 60 * 60
        var endTime = System.currentTimeMillis()
        var startTime = endTime - hourInMillis

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getUsageStatistics(startTime, endTime)

            endTime = startTime
            startTime = startTime - hourInMillis

            getUsageStatistics(startTime, endTime)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getUsageStatistics(startTime: Long, endTime: Long) {

        val map: HashMap<String, AppUsageInfo> = HashMap()
        val sameEvents: HashMap<String, MutableList<UsageEvents.Event>> = HashMap()

        val mUsageStatsManager =
            requireContext().getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager

        val packageManager = requireContext().packageManager

        mUsageStatsManager.let { usageStatsManager ->
            val usageEvents = usageStatsManager?.queryEvents(startTime, endTime)
            Logs().createLog("mUsageStatsManager $mUsageStatsManager")
            Logs().createLog("usageStatsManager $usageStatsManager")
            Logs().createLog("usageEvents $usageEvents")

            while (usageEvents?.hasNextEvent() == true) {
                val currentEvent = UsageEvents.Event()
                usageEvents.getNextEvent(currentEvent)

                if (currentEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED ||
                    currentEvent.eventType == UsageEvents.Event.ACTIVITY_PAUSED
                ) {
                    val key = currentEvent.packageName
                    if (!map.containsKey(key)) {
                        val appUsageInfo = AppUsageInfo(key)
                        Logs().createLog("appUsageInfo $appUsageInfo")

                        try {
                            val applicationInfo = packageManager.getApplicationInfo(key, 0)
                            appUsageInfo.appName = packageManager.getApplicationLabel(applicationInfo).toString()

                            Logs().createLog("applicationInfo $applicationInfo")

                        } catch (e: PackageManager.NameNotFoundException) {
                            appUsageInfo.appName = key  // Fallback to package name if app name is not found
                        }
                        map[key] = appUsageInfo
                        sameEvents[key] = ArrayList()
                    }
                    sameEvents[key]?.add(currentEvent)
                }
            }

            for ((key, events) in sameEvents) {
                val totalEvents = events.size
                if (totalEvents > 1) {
                    for (i in 0 until totalEvents - 1) {
                        val e0 = events[i]
                        val e1 = events[i + 1]

                        if (e1.eventType == UsageEvents.Event.ACTIVITY_RESUMED || e0.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                            map[e1.packageName]?.launchCount = (map[e1.packageName]?.launchCount ?: 0) + 1
                        }

                        if (e0.eventType == UsageEvents.Event.ACTIVITY_RESUMED && e1.eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                            val diff = e1.timeStamp - e0.timeStamp
                            map[e0.packageName]?.timeInForeground = (map[e0.packageName]?.timeInForeground ?: 0) + diff
                        }
                    }
                }

                if (events[0].eventType == UsageEvents.Event.ACTIVITY_PAUSED) {
                    val diff = events[0].timeStamp - startTime
                    map[events[0].packageName]?.timeInForeground = (map[events[0].packageName]?.timeInForeground ?: 0) + diff
                }

                if (events[totalEvents - 1].eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                    val diff = endTime - events[totalEvents - 1].timeStamp
                    map[events[totalEvents - 1].packageName]?.timeInForeground = (map[events[totalEvents - 1].packageName]?.timeInForeground ?: 0) + diff
                }
            }

            smallInfoList = ArrayList(map.values)
            Logs().createLog("smallInfoList $smallInfoList")

            smallInfoList.forEach { appUsageInfo ->
                val formattedTime = formatTime(appUsageInfo.timeInForeground)
                strMsg = "$strMsg${appUsageInfo.appName} : ${appUsageInfo.launchCount} launches, $formattedTime\n\n"
            }

            val tvMsg = view?.findViewById<TextView>(R.id.MA_TvMsg)
            tvMsg?.text = strMsg
        } ?: run {
            Toast.makeText(context, "Sorry...", Toast.LENGTH_SHORT).show()
        }
    }



    // Utility function to format time in milliseconds to a readable format
    private fun formatTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60)) % 24

        return when {
            hours > 0 -> String.format("%02d hr %02d min %02d sec", hours, minutes, seconds)
            minutes > 0 -> String.format("%02d min %02d sec", minutes, seconds)
            else -> String.format("%02d sec", seconds)
        }
    }

}