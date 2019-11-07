package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.QuestionDeleteResponse;
import com.upgrad.quora.api.model.QuestionDetailsResponse;
import com.upgrad.quora.api.model.QuestionEditRequest;
import com.upgrad.quora.api.model.QuestionEditResponse;
import com.upgrad.quora.service.business.QuestionBusinessService;
import com.upgrad.quora.service.business.UserBusinessService;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;


@RestController
@RequestMapping("/")
public class QuestionController {

    @Autowired
    QuestionBusinessService questionBusinessService;

    /**Comments by Avia **/
    //This method only allows the owner of the question to edit a question
    //To edit a question, this endpoint takes in the questionUuid, access token and the content to be updated from the editRequest.

    @RequestMapping(method = RequestMethod.PUT, path = "/question/edit/{questionId}", consumes= MediaType.APPLICATION_JSON_UTF8_VALUE, produces=MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionEditResponse> editQuestionContent(@PathVariable("questionId") final String questionUuid, @RequestHeader("authorization") final String authorization, final QuestionEditRequest editRequest) throws Exception {
        QuestionEntity questionEntity;
        QuestionEntity editedQuestion;
        try{
            String[] userToken = authorization.split("Bearer ");
            questionEntity = questionBusinessService.getQuestion(questionUuid, userToken[1]);
            questionEntity.setContent(editRequest.getContent());
            editedQuestion = questionBusinessService.editQuestion(questionEntity,userToken[1]);}
        catch(ArrayIndexOutOfBoundsException e){
            questionEntity  = questionBusinessService.getQuestion(questionUuid, authorization);
            questionEntity.setContent(editRequest.getContent());
            editedQuestion = questionBusinessService.editQuestion(questionEntity,authorization);
        }
        /**Comments by Avia **/
        //In normal cases, updating an entity doesn't change the Uuid, meaning questionUuid==updatedUuid.
        // However, we have implemented this feature in case the system later requires to keep track of the updates, for e.g. by adding a suffix after every update like Uuid-1,-2, etc.

        String updatedUuid = editedQuestion.getUuid();


        QuestionEditResponse questionEditResponse = new QuestionEditResponse().id(updatedUuid).status("QUESTION EDITED");

        return new ResponseEntity<QuestionEditResponse>(questionEditResponse, HttpStatus.OK);

    }

    /**Comments by Avia **/
    //This method returns all the questions posted by user as a list and can be accessed by an user.

    @RequestMapping(method = RequestMethod.GET, path = "question/all/{userId}", produces=MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestionsByUser(@RequestHeader("authorization") final String authorization, @PathVariable ("userId") final String userUuid) throws Exception {
        List<QuestionEntity> listOfUserQuestions = new ArrayList<>();
        try{

            String[] bearerAccessToken = authorization.split("Bearer ");
                listOfUserQuestions = questionBusinessService.getAllQuestionsByUser(bearerAccessToken[1],userUuid);
        }
        catch(Exception e){
            listOfUserQuestions = questionBusinessService.getAllQuestionsByUser(authorization,userUuid);
        }


        ListIterator<QuestionEntity> questions = listOfUserQuestions.listIterator();
        List<QuestionDetailsResponse> displayQuestionIdAndContent = new ArrayList<>();
        while(questions.hasNext()){
            QuestionDetailsResponse questionDetailsResponse = new QuestionDetailsResponse().id(questions.next().getUuid()).content(questions.next().getContent());
            displayQuestionIdAndContent.add(questionDetailsResponse);}
            return new ResponseEntity<List<QuestionDetailsResponse>>(displayQuestionIdAndContent,HttpStatus.CREATED);

    }


}
