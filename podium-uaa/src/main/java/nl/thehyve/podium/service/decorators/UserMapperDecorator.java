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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.elasticsearch.core.completion.Completion;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.*;

public abstract class UserMapperDecorator implements UserMapper {

    @Autowired
    @Qualifier( "delegate" )
    private UserMapper delegate;

    @Override
    public SearchUser userToSearchUser(User user) {
        SearchUser searchUser = delegate.userToSearchUser(user);
        String fullName = Stream.of(
            searchUser.getFirstName(),
            searchUser.getLastName(),
            "(" + searchUser.getEmail() + ")")
            .filter(part -> part != null && !part.isEmpty())
            .collect(Collectors.joining(" "));
        searchUser.setFullName(fullName);
        String[] inputs = Stream.of(
            fullName,
            searchUser.getFirstName(),
            searchUser.getLastName(),
            searchUser.getLogin(),
            searchUser.getEmail())
            .filter(part -> part != null && !part.isEmpty())
            .toArray(String[]::new);
        Completion fullNameCompletion = new Completion(inputs);
        searchUser.setFullNameSuggest(fullNameCompletion);
        return searchUser;
    }

    @Override
    public List<SearchUser> usersToSearchUsers(List<User> users) {
        if ( users == null ) {
            return null;
        }

        List<SearchUser> list = new ArrayList<>();

        for ( User user : users ) {
            list.add( userToSearchUser( user ) );
        }

        return list;
    }

    @Override
    public SearchUser completionSuggestOptionToSearchUser(CompletionSuggestion.Entry.Option entry) {
        if (entry == null) {
            return null;
        }

        SearchUser searchUser = new SearchUser();
        searchUser.setFullName(entry.getHit().getSourceAsMap().getOrDefault("fullName", "").toString());
        searchUser.setUuid((String) entry.getHit().getSourceAsMap().get("uuid"));

        return searchUser;
    }

    @Override
    public List<SearchUser> completionSuggestOptionsToSearchUsers(List<CompletionSuggestion.Entry.Option> entries) {
        if (entries == null) {
            return null;
        }

        List<SearchUser> searchUsers = new ArrayList<>();

        for (CompletionSuggestion.Entry.Option entry : entries) {
            searchUsers.add( completionSuggestOptionToSearchUser(entry) );
        }

        return searchUsers;
    }
}
