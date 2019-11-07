package com.upgrad.quora.service.business;


import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class QuestionBusinessService {

    @Autowired
    UserDao userDao;

    @Autowired
    QuestionDao questionDao;

    @Autowired
    UserBusinessService userBusinessService;

    /** comments by Avia **/
    //This method retrieves the question in the database
    public QuestionEntity getQuestion(final String questionUuid, final String accessToken) throws InvalidQuestionException{

        QuestionEntity questionEntity = questionDao.getQuestionByUuid(questionUuid);
        if(questionEntity == null) {
            throw new InvalidQuestionException("QUES-001", "Entered question uuid does not exist");
        }
         return questionEntity;

    }
    /** comments by Avia **/
    //This method updates the question in the database
    //THe method first checks if the user token is valid
    //Next it checks if the user trying to edit is the owner of the question.
    //If the current user is not the owner it throws an exception, else the question is updated

    public QuestionEntity editQuestion(final QuestionEntity questionEntity, final String accessToken) throws Exception {
        UserAuthTokenEntity userAuthToken = userDao.getUserAuthToken(accessToken);
        if (userAuthToken == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        ZonedDateTime logoutTime = userAuthToken.getLogoutAt();
        if(logoutTime!= null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to edit the question");
        }

            String questionOwnerUuid = questionEntity.getUser().getUuid();
            String signedInUserUuid = userAuthToken.getUser().getUuid();

            if (questionOwnerUuid.equals(signedInUserUuid)) {
                QuestionEntity updatedQuestion = questionDao.updateQuestion(questionEntity);
                return updatedQuestion;
            }

            else{
                throw new AuthorizationFailedException("ATHR-003","Only the question owner can edit the question");
            }

    }

    /** comments by Avia **/
    //This method updates the question in the database retrieves all the questions post by a user from the database

    public List<QuestionEntity> getAllQuestionsByUser(final String accessToken, String userUuid) throws Exception, NullPointerException {
        UserAuthTokenEntity userAuthToken = userDao.getUserAuthToken(accessToken);
        if (userAuthToken == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        ZonedDateTime logoutTime = userAuthToken.getLogoutAt();
        if(logoutTime!= null) {
            throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to edit the question");
        }

            UserEntity userEntity = userBusinessService.getUser(userUuid,accessToken);
            if (userEntity == null) {
                throw new UserNotFoundException("USR-001", "User with entered uuid whose question details are to be seen does not exist");
            }

            return questionDao.getAllQuestionsByUser(userUuid);


    }

}
