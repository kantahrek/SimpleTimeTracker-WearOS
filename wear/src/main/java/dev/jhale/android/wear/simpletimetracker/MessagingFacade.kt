/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package dev.jhale.android.wear.simpletimetracker

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.CapabilityInfo
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import dev.jhale.android.wear.simpletimetracker.presentation.LOG_TAG
import javax.inject.Inject

class MessagingFacade @Inject constructor()
    : MessageClient.OnMessageReceivedListener {

    // This the right way to implement collections of constants in Kotlin?
        companion object KnownMessages {
            const val START_TIME_TRACKING_ACTIVITY_CAPABILITY_NAME = "start_time_tracking_activity";
            const val REQUEST_CATEGORIES = "request_categories"
        const val REQUEST_RUNNING_RECORDS = "request_running_records"
        const val REQUEST_PREFS = "request_prefs"
        const val RECEIVE_CATEGORIES = "receive_categories"
        const val RECEIVE_RUNNING_RECORDS = "receive_running_records"
        const val RECEIVE_PREFS = "receive_prefs"
        }

    fun startTimeTracking(context: Context, activity: String, tag: String) {
        Thread(Runnable {
            sendMessage(
                context,
                START_TIME_TRACKING_ACTIVITY_CAPABILITY_NAME,
                "$activity|$tag"
            )
        }).start()
    }

    private fun sendMessage(
        context: Context,
        capability: String,
        message: String
    ) {
        // Find all nodes which support the time tracking message
        val capabilityInfo: CapabilityInfo = Tasks.await(
            Wearable.getCapabilityClient(context)
                .getCapability(
                    capability,
                    CapabilityClient.FILTER_REACHABLE
                )
        )

        // Choose the best node (the closest one connected to the watch
        val nodes = capabilityInfo.nodes
        val bestNode = nodes.firstOrNull { it.isNearby }?.id ?: nodes.firstOrNull()?.id

        // Send the message
        bestNode?.also { nodeId ->
            val sendTask: Task<*> = Wearable.getMessageClient(context).sendMessage(
                nodeId,
                "/$capability",
                message.toByteArray()
            ).apply {
                addOnSuccessListener {
                    Log.i(
                        LOG_TAG,
                        "Sent $capability message: $message"
                    )
                }
                addOnFailureListener {
                    Log.e(
                        LOG_TAG,
                        "Failed to send $capability message: $message"
                    )
                }
            }
        } ?: run {
            Log.e(
                LOG_TAG,
                "No nodes found with the capability $capability"
            )
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        when (messageEvent.path) {
            RECEIVE_CATEGORIES -> {

            }

            RECEIVE_RUNNING_RECORDS -> {

            }

            else -> {}
        }
    }
}