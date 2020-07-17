// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.gson.Gson;
import com.google.sps.data.Task;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException { 
        int num = Integer.parseInt(request.getParameter("num"));
        String post = request.getParameter("post");

        Filter by_post = new FilterPredicate("post", FilterOperator.EQUAL, post);

        Query query = new Query("Task").setFilter(by_post).addSort("timestamp", SortDirection.DESCENDING);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        PreparedQuery results = datastore.prepare(query);

        List<Task> tasks = new ArrayList<>();
        int i = 0;
        for (Entity entity : results.asIterable()) {
            long id = entity.getKey().getId();
            String text = (String) entity.getProperty("text");
            long timestamp = (long) entity.getProperty("timestamp");
            String nickname = (String) entity.getProperty("nickname");

            Task task = new Task(id, text, post, timestamp, nickname);
            tasks.add(task);
            i++;
            if (i == num) {
                break;
            }
        }

        Gson gson = new Gson();

        response.setContentType("application/json;");
        response.getWriter().println(gson.toJson(tasks));
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String text = request.getParameter("text-input");
        String post = request.getParameter("post");

        long timestamp = System.currentTimeMillis();

        UserService userService = UserServiceFactory.getUserService();

        String id = userService.getCurrentUser().getUserId();
        String nickname = getUserNickname(id);

        Entity taskEntity = new Entity("Task");
        taskEntity.setProperty("text", text);
        taskEntity.setProperty("post", post);
        taskEntity.setProperty("timestamp", timestamp);
        taskEntity.setProperty("nickname", nickname);

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        datastore.put(taskEntity);

        // Redirect back to the HTML page.
        response.sendRedirect("/blog.html");
    }

    private String getUserNickname(String id) {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Query query =
            new Query("UserInfo")
                .setFilter(new Query.FilterPredicate("id", Query.FilterOperator.EQUAL, id));
        PreparedQuery results = datastore.prepare(query);
        Entity entity = results.asSingleEntity();
        if (entity == null) {
            return "";
        }
        String nickname = (String) entity.getProperty("nickname");
        return nickname;
    }
}
