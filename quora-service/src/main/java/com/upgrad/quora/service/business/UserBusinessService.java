package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.time.ZonedDateTime;

@Service
public class UserBusinessService {

    @Autowired
    UserDao userDao;

    /** comments by Avia **/
        //The below method checks if the user token is valid.
        //If the token is valid, the method returns the corresponding user entity.
    public UserEntity getUser(final String userUuid,final String authorizationToken) throws Exception,NullPointerException {
        UserAuthTokenEntity userAuthToken = userDao.getUserAuthToken(authorizationToken);
        if (userAuthToken == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        ZonedDateTime logoutTime = userAuthToken.getLogoutAt();
        if(logoutTime!= null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to get user details");
        }
        UserEntity userEntity=userDao.getUserByUuid(userUuid);
        if (userEntity == null) {
            throw new UserNotFoundException("USR-001", "User with entered uuid whose question details are to be seen does not exist");
        }
        return userEntity;

    }


}






