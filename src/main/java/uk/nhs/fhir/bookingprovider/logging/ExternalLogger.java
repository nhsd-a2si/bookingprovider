/*
 * Copyright 2019 dev.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.nhs.fhir.bookingprovider.logging;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class which logs information out to an MS Teams channel. It is defined as a
 * Singleton class, but one instance is created and shared around the whole
 * system.
 *
 * It might be better to do a GetInstance wherever, or make the log() method
 * static and just call it directly?
 *
 * @author dev
 */
public class ExternalLogger {

    /**
     * We log locally for this class using this object
     */
    private static final Logger LOG = Logger.getLogger(ExternalLogger.class.getName());
    private static ExternalLogger instance = null;
    private String environment;

    private void setEnvironment(String env) {
        this.environment = env;
    }

    /**
     * Private constructor to prevent direct creation of this singleton class.
     *
     */
    private ExternalLogger() {
    }

    private ExternalLogger(String env) {
        environment = env;
    }

    /**
     * Here's how to get the one instance.
     *
     * @return The one and only instance of this class.
     */
    public static ExternalLogger GetInstance() {
        if (instance == null) {
            instance = new ExternalLogger();
        }
        return instance;
    }

    /**
     * Here's how to get the instance, and set the environment name.
     *
     * @param env
     * @return
     */
    public static ExternalLogger GetInstance(String env) {
        if (instance == null) {
            instance = new ExternalLogger();
        }
        instance.setEnvironment(env);
        return instance;
    }

    /**
     * Main purpose of this class, we log the supplied message.
     *
     * @param message The message we want to log
     * @return An indication of success (true) or failure (false).
     */
    public boolean log(String message) {

        boolean result = false;

        OkHttpClient client = new OkHttpClient();
        ResponseBody responseBody = null;

        try {
            String envName;
            if (environment != null) {
                envName = environment;
            } else {
                envName = "[None]";
            }

            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, "{\"title\": \"Log event from Demonstrator in " + envName + "\",\"text\": \"" + message + "\"}");
            Request request = new Request.Builder()
                    .url("https://outlook.office.com/webhook/345757da-218e-4a1c-bbb8-93b142739fd4@50f6071f-bbfe-401a-8803-673748e629e2/IncomingWebhook/deb1c5af43484efc9e74a60cf1343245/208797b4-6c4d-4634-a989-37a45204ac2e")
                    .post(body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("cache-control", "no-cache")
                    .build();

            Response response = client.newCall(request).execute();

            if (response.isSuccessful()) {
                result = true;
                LOG.info("Logging got a 200");
                responseBody = response.body();
            } else {
                LOG.warning("Logging failed");
            }

        } catch (IOException ex) {
            LOG.severe(ex.getMessage());
        } finally {
            try {
                responseBody.close();
            }
            catch (IOException ex) {
            }
        }
        return result;
    }
}
