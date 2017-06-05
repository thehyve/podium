/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

package nl.thehyve.podium.service.decorators;

import nl.thehyve.podium.domain.User;
import nl.thehyve.podium.search.SearchUser;
import nl.thehyve.podium.service.mapper.UserMapper;
import org.elasticsearch.search.suggest.completion.CompletionSuggestion;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.elasticsearch.core.completion.Completion;

import java.util.ArrayList;
import java.util.List;

public abstract class UserMapperDecorator implements UserMapper {

    @Autowired
    @Qualifier("delegate")
    private UserMapper delegate;

    @Override
    public SearchUser userToSearchUser(User user) {
        SearchUser searchUser = delegate.userToSearchUser(user);
        String[] fullname = {searchUser.getFirstName(), searchUser.getLastName()};
        Completion fullNameCompletion = new Completion(fullname);

        // Set the elasticsearch payload as json
        JSONObject userUUID = new JSONObject();

        try {
            userUUID.put("uuid", searchUser.getUuid());
            fullNameCompletion.setPayload(userUUID.toString());
        } catch (Exception ex) {
            //
        }

        String outputString
            = searchUser.getFirstName() + " " + searchUser.getLastName() + " (" + searchUser.getEmail() + ")";

        fullNameCompletion.setOutput(outputString);

        searchUser.setFullNameSuggest(fullNameCompletion);
        return searchUser;
    }

    @Override
    public List<SearchUser> usersToSearchUsers(List<User> users) {
        if (users == null) {
            return null;
        }

        List<SearchUser> list = new ArrayList<>();

        for (User user : users) {
            list.add(userToSearchUser(user));
        }

        return list;
    }

    @Override
    public SearchUser completionSuggestOptionToSearchUser(CompletionSuggestion.Entry.Option entry) {
        if (entry == null) {
            return null;
        }

        SearchUser searchUser = new SearchUser();
        searchUser.setFullName(entry.getText().toString());

        try {
            JSONObject uuidObject = new JSONObject(entry.getPayloadAsString());
            searchUser.setUuid((String) uuidObject.get("uuid"));
        } catch (Exception ex) {

        }

        return searchUser;
    }

    @Override
    public List<SearchUser> completionSuggestOptionsToSearchUsers(List<CompletionSuggestion.Entry.Option> entries) {
        if (entries == null) {
            return null;
        }

        List<SearchUser> searchUsers = new ArrayList<>();

        for (CompletionSuggestion.Entry.Option entry : entries) {
            searchUsers.add(completionSuggestOptionToSearchUser(entry));
        }

        return searchUsers;
    }
}
