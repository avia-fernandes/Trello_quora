package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;


@Service
public class UserAdminBusinessService {

    @Autowired
    UserDao userDao;

    /** comments by Archana **/
    @Transactional(propagation = Propagation.REQUIRED)
    public String deleteUser(final String userUuid, final String accessToken) throws AuthorizationFailedException, UserNotFoundException, NullPointerException {
        UserAuthTokenEntity userAuthToken = userDao.getUserAuthToken(accessToken);
        //if the requested access token of an admin user is not present the following exception is thrown
        if (userAuthToken == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        //Only admin can delete an user, if other than admin is trying to delete the user following exception is thrwon
        String role = userAuthToken.getUser().getRole();
        if (role.equalsIgnoreCase("nonadmin")) {
            throw new AuthorizationFailedException("ATHR-003", "Unauthorized Access, Entered user is not an admin");
        }
        //The user whose record is to be deleted , if it doesnt exist in database following exception is thrown
        UserEntity userEntity = userDao.getUserByUuid(userUuid);
        if (userEntity == null) {
            throw new UserNotFoundException("USR-001", "User with entered uuid to be deleted does not exist");
        }
        //If the admin is signed in , admin can go and delete the user else signed out but still trying to delete the user,
        // following exception is thrwon
        if (userAuthToken.getLogoutAt() == null) {
            userDao.deleteUser(userEntity);
        } else {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out");
        }

        return userUuid;
    }


}
