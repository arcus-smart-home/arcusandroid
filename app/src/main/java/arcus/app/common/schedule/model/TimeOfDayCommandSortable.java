/*
 *  Copyright 2019 Arcus Project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package arcus.app.common.schedule.model;

import com.iris.client.bean.TimeOfDayCommand;

import java.util.Map;


public class TimeOfDayCommandSortable extends TimeOfDayCommand implements Comparable<TimeOfDayCommandSortable> {

    //constructors
    public TimeOfDayCommandSortable(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public int compareTo(TimeOfDayCommandSortable timeOfDayCommand) {
       return(this.getTime().compareTo(timeOfDayCommand.getTime()) );
    }

    @Override
    public String getTime() {
        //the getTime method in the jar-lib is not working properly. Currently, it drops the leading zero. This override fixes that.
         String strTime = super.getTime();
        if (strTime.indexOf(":",0) == 1){
            return "0"+strTime;
        } else {
            return strTime;
        }
    }

}
