package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Objects;

/** comments by Archana **/
 //The class UserBusinessService will define all the business logic required to implement user related functionalities
@Service
public class UserBusinessService {

    @Autowired
    UserDao userDao;

    @Autowired
    PasswordCryptographyProvider cryptographyProvider;

    //The signUp method for registering a new user checks for two conditions
    //userName already exists and email already exists. If the attributes found to be existed in the database, then the user is intimated that
    //with the corresponding error message and results in nonRegistration of the user
    //This method also encrypts the password upon the successfull registration of the user

    @Transactional(propagation = Propagation.REQUIRED)
    public UserEntity signUp(final UserEntity userEntity) throws SignUpRestrictedException, NullPointerException {

        String userName = userEntity.getUserName();
        UserEntity userNameExists = userDao.getUserByUserName(userName);
        String email = userEntity.getEmail();
        UserEntity emailExits = userDao.getUserByEmail(email);
        String password = userEntity.getPassword();

       if (userNameExists!=null) {
            throw new SignUpRestrictedException("SGR-001", "Try any other Username, this Username has already been taken");
        } else if (emailExits!=null) {
            throw new SignUpRestrictedException("SGR-002", "This user has already been registered, try with any other emailId");
        }
            String[] encryptedText = cryptographyProvider.encrypt(password);
            userEntity.setSalt(encryptedText[0]);
            userEntity.setPassword(encryptedText[1]);
            return userDao.createUser(userEntity);
        }

    /** comments by Avia **/
        //The below method checks if the user token is valid.
        //If the token is valid, the method returns the corresponding user entity.

    public UserEntity getUser(final String userUuid,final String authorizationToken) throws Exception,NullPointerException {
            Exception e = validateToken(authorizationToken);
        UserEntity userEntity;
        if(e==null){

            return userEntity =  userDao.getUserByUuid(userUuid);

        }
        else{
            return null;
        }
    }

    /** comments by Avia **/
    //The below methods checks for ATHR-001 and ATHR-002 exceptions
    //It checks if the user was assigned a token (i.e. if the user is signed in) and if the user is still logged in.
    //If the token is invalid it returns an exception else it returns null/
    public Exception validateToken (final String authorizationToken) throws AuthorizationFailedException, NullPointerException {
        UserAuthTokenEntity userAuthTokenEntity = userDao.getUserAuthToken(authorizationToken);
        //If the userAuthTokenEntity returns null, it implies the token doesn't exist hence the user is not signed in and wasn't assigned a token
        if(userAuthTokenEntity == null){
            return new AuthorizationFailedException("ATHR-001","User has not signed in");
        }
        else{
            ZonedDateTime logout = userAuthTokenEntity.getLogoutAt();
            if(logout != null){
                return new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to proceed");
            }
        }
        return null;
    }

}






