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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Utility class to provide networking and file operations.
 */
public class CodelabUtil {

    public static final String TASK_ID = "taskId";
    public static final String TASK_STATUS = "status";
    public static final String TASK_UPDATE_FILTER = "task-update";
    private static final String TAG = "CodelabUtil";
    private static final String FILE_NAME = "taskfile.dat";
    private static final String ONLINE_LOCATION = "https://google.com";

    public static final String PENDING_STATUS = "Pending";
    public static final String EXECUTED_STATUS = "Executed";
    public static final String FAILED_STATUS = "Failed";
    public static final String ONEOFF_TASK = "Oneoff Task";
    public static final String NOW_TASK = "Now Task";

    static final String PAGE = "page";
    static final String BEER = "beer";
    static final String FILTER = "filter";
    static final String ORDER = "order";
    static final String FAV = "fav";



    /**
    * Some utils
    */

    static void saveIntPreference(String key, int value, Context mContext) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    static int getIntPreference(String key, Context mContext) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        return pref.getInt(key, 0);
    }

    static int getIntPreference(String key, Context mContext, int def) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
        return pref.getInt(key, def);
    }

    /**
     * Make a network request form ONLINE_LOCATION.
     */
    public static boolean makeNetworkCall() {
        try {
            URL url = new URL(ONLINE_LOCATION);
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.getInputStream();
            Log.d(TAG, "Network call completed.");
            return true;
        } catch (IOException e) {
            Log.e(TAG, "IOException " + e.getMessage());
            return false;
        }
    }

   public static List<BeerItem> getBeerItemsFromFile(Context context) {
        List<BeerItem> beerItems = new ArrayList<>();
        File beerFile = new File(context.getFilesDir(), FILE_NAME);
        if (!beerFile.exists()) {
            return beerItems;
        }
        try {
            String beerStr = IOUtils.toString(new FileInputStream(beerFile));
            beerItems.addAll(beerItemsFromString(beerStr));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return beerItems;
    }

    public static List<BeerItem> beerItemsFromString(String beerStr) {
        Gson gson = new Gson();
        Type beerItemType = new TypeToken<ArrayList<BeerItem>>(){}.getType();
        List<BeerItem> beerItems = gson.fromJson(beerStr, beerItemType);
        return beerItems;
    }


      public static void saveBeerItemsToFile(Context context, List<BeerItem> beerItems) {
        String beerStr = beerItemsToString(beerItems);
        File file = new File(context.getFilesDir(), FILE_NAME);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            IOUtils.write(beerStr, fileOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String beerItemsToString(List<BeerItem> beerItems) {
        return new Gson().toJson(beerItems);
    }



    public static void addBeerItemsToFile(Context context, List<BeerItem> newItems) {

        for(BeerItem beerItem: newItems){
            addBeerItemToFile(context, beerItem);
        }

    }


    public static void saveBeerItemToFile(Context context, BeerItem beerItem) {
        List<BeerItem> beerItems = getBeerItemsFromFile(context);
        for (int i = 0; i < beerItems.size(); i++) {
            BeerItem ti = beerItems.get(i);
            if (ti.getId().equals(beerItem.getId())) {
                beerItems.set(i, beerItem);
                break;
            }
        }
        saveBeerItemsToFile(context, beerItems);
    }



    public static void addBeerItemToFile(Context context, BeerItem beerItem) {
        List<BeerItem> beerItems = getBeerItemsFromFile(context);

        boolean hasDup = false;
        String id = beerItem.getId();

        for(BeerItem item: beerItems){
            if(id.equals(item.getId())){
                hasDup = true;
            }
        }

        if(!hasDup){
            beerItems.add(beerItem);
            saveBeerItemsToFile(context, beerItems);
        }


    }


}
