/*
 ~ Copyright (c) 2014, WSO2 Inc. (http://wso2.com/) All Rights Reserved.
 ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");
 ~ you may not use this file except in compliance with the License.
 ~ You may obtain a copy of the License at
 ~
 ~      http://www.apache.org/licenses/LICENSE-2.0
 ~
 ~ Unless required by applicable law or agreed to in writing, software
 ~ distributed under the License is distributed on an "AS IS" BASIS,
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ~ See the License for the specific language governing permissions and
 ~ limitations under the License.
*/
package org.wso2.emm.agent.models;

import android.util.Log;

public class PInfo {
    public String appname = "";
    public String pname = "";
    public String versionName = "";
    public int versionCode = 0;
    public String icon;
    public void prettyPrint() {
        Log.v("APPLICATION INFO : ", appname + "\t" + pname + "\t" + versionName + "\t" + versionCode);
    }
}

