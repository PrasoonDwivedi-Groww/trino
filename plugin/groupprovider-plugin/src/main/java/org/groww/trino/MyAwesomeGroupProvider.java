/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.groww.trino;

import com.google.common.collect.ImmutableSet;
import io.trino.spi.security.GroupProvider;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.*;

public class MyAwesomeGroupProvider
        implements GroupProvider
{
    String fs_url;
    Map<String, String> config;

    public MyAwesomeGroupProvider(Map<String, String> config) {
        if (config.containsKey("fs_url")) {
            this.fs_url = config.get("fs_url");
        } else {
            throw new RuntimeException("NO FS URL FOUND");
        }
        this.config = config;
    }

    @Override
    public Set<String> getGroups(String user, String catalogName) {
        if (catalogName == null)
            return ImmutableSet.of();
        String realmName = null;
        if(config.containsKey(catalogName))
            realmName = config.get(catalogName);
        else
            realmName = catalogName.split("_")[0];
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();

        Request request = new Request.Builder()
                .url("https://" + fs_url + "/api/v0/orgs/" + realmName + "/keycloak/user/" + user + "/groups")
                .get().build();

        Response response = null;
        try {
            if(realmName != null)
                response = client.newCall(request).execute();
            else
                throw new RuntimeException("RealmName can not be null");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assert response.body() != null;
        String cleanString = null;
        try {
            cleanString = response.body().string().replaceAll("\\[|\\]|\"", "");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String[] result = cleanString.split(",");
        response.close();
        return new HashSet<>(Arrays.asList(result));
    }

//    public static void main(String[] args) {
//        fs_url = "dp-feature-store-server.growwinfra.in";
//        config.put("fs_url", fs_url);
//        MyAwesomeGroupProvider s = new MyAwesomeGroupProvider(config);
//
//        System.out.println(s.getGroups("prasoon.dwivedi@groww.in", "invest_iceberg"));
//    }

}
