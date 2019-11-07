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

@Transactional(propagation = Propagation.REQUIRED)
   public String deleteQuestion(final String questionUuid, final String accessToken) throws AuthorizationFailedException, InvalidQuestionException {
       UserAuthTokenEntity userAuthToken = userDao.getUserAuthToken(accessToken);

       /** comments by Archana **/
       //If the accessToken of admin or QuestionOwner doesnt exist in the database throw following Exception
       //It means that if the user hasnt signedin, then the basic Authentication is not done and the accessToken is not generated
       if (userAuthToken == null) {
           throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
       }
       //we have logoutAt attribute in the userAuth table, upon successfull signout of the application
       //the user logoutAt attribute will be updated. So if the logoutAt is not null then it means that user has signed out
       ZonedDateTime logoutTime = userAuthToken.getLogoutAt();
       if(logoutTime!= null) {
           throw new AuthorizationFailedException("ATHR-002", "User is signed out.Sign in first to delete a question");
       }
       //If the questionUuid doesnt exist in the database throw following exception
       QuestionEntity questionEntity = questionDao.getQuestionByUuid(questionUuid);
        if(questionEntity == null) {
            throw new InvalidQuestionException("QUES-001", "Entered question uuid does not exist");
        }
       //The owneroftheQuestion or an admin will have privelege of deleting the Question
       //The user with the role non-admin and non-owner of the Question is trying to delete the Question
       //then following exception is thrown
        String role = userAuthToken.getUser().getRole();
        String questionOwnnerUuid = questionEntity.getUser().getUuid();
        String signedInUserUuid = userAuthToken.getUser().getUuid();

        if(role.equals("admin") || questionOwnnerUuid.equals(signedInUserUuid)) {
            questionDao.deleteQuestion(questionEntity);
        }else {
            throw new AuthorizationFailedException("ATHR-003","Only the question owner or admin can delete the question");
        }

        return questionUuid;
   }

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
        Exception e = userBusinessService.validateToken(accessToken);
        if(e==null) {

            String questionOwnerUuid = questionEntity.getUser().getUuid();
            String signedInUserUuid = userAuthToken.getUser().getUuid();

            if (questionOwnerUuid.equals(signedInUserUuid)) {
                QuestionEntity updatedQuestion = questionDao.updateQuestion(questionEntity);
                return updatedQuestion;
            }

            else{
                throw new AuthorizationFailedException("ATHR-003","Only the question owner or admin can edit the question");
            }
        }

        else{
            throw e;
        }

    }

    public List<QuestionEntity> getAllQuestionsByUser(final String accessToken, String userUuid) throws Exception, NullPointerException {


        Exception e = userBusinessService.validateToken(accessToken);
        if(e==null){
            UserEntity userEntity = userBusinessService.getUser(userUuid,accessToken);
            if (userEntity == null) {
                throw new UserNotFoundException("USR-001", "User with entered uuid whose question details are to be seen does not exist");
            }
            return questionDao.getAllQuestionsByUser(userUuid);}


        else{
            throw e;
        }

    }

}
