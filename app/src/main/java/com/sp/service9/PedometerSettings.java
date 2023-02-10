/*
 *  Pedometer - Android App
 *  Copyright (C) 2009 Levente Bagi
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.sp.service9;

import android.content.SharedPreferences;

/**
 * Wrapper for {@link SharedPreferences}, handles preferences-related tasks.
 * @author Levente Bagi
 */
public class PedometerSettings {

    SharedPreferences mSettings;

    public PedometerSettings(SharedPreferences settings) {
        mSettings = settings;
    }

    // Internal

    public void clearServiceRunning(Boolean status) {
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean("service_running", status);
        editor.commit();
    }

    public boolean isServiceRunning() {
        return mSettings.getBoolean("service_running", false);
    }

}
