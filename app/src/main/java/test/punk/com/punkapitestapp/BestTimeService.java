/**
 * Copyright Google Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test.punk.com.punkapitestapp;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;


public class BestTimeService extends GcmTaskService {

    private static final String TAG = "BestTimeService";

    @Override
    public int onRunTask(TaskParams taskParams) {
   //     String taskId = taskParams.getExtras().getString(CodelabUtil.TASK_ID);
        boolean completed = CodelabUtil.makeNetworkCall();

   //     Log.d(TAG, "Oneoff scheduled call executed. Task ID: " + taskId);

        // Prepare Intent to send with broadcast.
        Intent taskUpdateIntent = new Intent(CodelabUtil.TASK_UPDATE_FILTER);
        taskUpdateIntent.putExtra(CodelabUtil.TASK_STATUS, completed ? CodelabUtil.EXECUTED_STATUS: CodelabUtil.FAILED_STATUS);

        // Notify listeners (MainActivity) that task was completed successfully.
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(taskUpdateIntent);
        return GcmNetworkManager.RESULT_SUCCESS;
    }
}
