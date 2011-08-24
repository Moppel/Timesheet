/* Copyright (c) 2005 by net-linx; All rights reserved */
package com.uwusoft.timesheet;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * todo: add class doc
 *
 * @author Uta Wunderlich
 * @version $Revision: $, $Date: Aug 18, 2011
 * @since Aug 18, 2011
 */
public class SubmissionServiceImpl implements SubmissionService {

    public SubmissionServiceImpl() {
        Properties props = new Properties();
        try {
            props.load(this.getClass().getResourceAsStream("mavenlink.properties"));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        //connect("https://" + props.getProperty("user-id") + ":" + props.getProperty("token") + "@mavenlink.local/api/v0/time_entries?per_page=5");
    }

    public void submit(Date date, String task, Double total) {
        //Todo implement
        System.out.println(new SimpleDateFormat().format(date) + "\t" + task + "\t" + total);
    }

    private void connect(String url) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);

        httpGet.addHeader("Accept", "application/json");

        HttpResponse response;
        try {
            response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                InputStream instream = entity.getContent();
                String result = IOUtils.toString(instream);

                System.out.println(result);
                //JSONObject json = new JSONObject(result);

                instream.close();

                //return json;
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
