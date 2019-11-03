package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.QuestionDeleteResponse;
import com.upgrad.quora.api.model.QuestionDetailsResponse;
import com.upgrad.quora.api.model.QuestionEditRequest;
import com.upgrad.quora.api.model.QuestionEditResponse;
import com.upgrad.quora.service.business.QuestionBusinessService;
import com.upgrad.quora.service.entity.QuestionEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
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
    /**Commets by Archana **/
    //The admin or the owner of the Question has a privilege of deleting the question
    //This endpoint requests for the questionUuid to be deleted and the questionowner or admin accesstoken in the authorization header


    @RequestMapping(method = RequestMethod.DELETE, path = "/question/delete/{questionId}",produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionDeleteResponse> deleteQuestion(@PathVariable("questionId") final String questionUuid, @RequestHeader("authorization") final String authorization)throws AuthorizationFailedException, InvalidQuestionException {

        String uuid ;
        try {
            String[] accessToken = authorization.split("Bearer ");
            uuid = questionBusinessService.deleteQuestion(questionUuid, accessToken[1]);
        }catch(ArrayIndexOutOfBoundsException are) {
            uuid = questionBusinessService.deleteQuestion(questionUuid, authorization);
        }
        QuestionDeleteResponse authorizedDeletedResponse = new QuestionDeleteResponse().id(uuid).status("QUESTION DELETED");
        //This method returns an object of QuestionDeleteResponse and HttpStatus
        return new ResponseEntity<QuestionDeleteResponse>(authorizedDeletedResponse, HttpStatus.OK);
    }
    /**Commets by Avia **/
    //
    @RequestMapping(method = RequestMethod.PUT, path = "/question/edit/{questionId}", consumes= MediaType.APPLICATION_JSON_UTF8_VALUE, produces=MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionEditResponse> editQuestionContent(@PathVariable("questionId") final String questionUuid, @RequestHeader("authorization") final String authorization, final QuestionEditRequest editRequest) throws AuthorizationFailedException, InvalidQuestionException{
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
        /**Commets by Avia **/
        //In normal cases, updating an entity doesn't change the Uuid, meaning questionUuid==updatedUuid.
        // However, we have implemented this feature in case the system later requires to keep track of the updates, for e.g. by adding a suffix after every update like Uuid-1,-2, etc.

        String updatedUuid = editedQuestion.getUuid();


        QuestionEditResponse questionEditResponse = new QuestionEditResponse().id(updatedUuid).status("QUESTION EDITED");

        return new ResponseEntity<QuestionEditResponse>(questionEditResponse, HttpStatus.OK);

    }

    @RequestMapping(method = RequestMethod.GET, path = "question/all/{userId}", produces=MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestionsByUser(@RequestHeader("authorization") final String authorization, @PathVariable ("userId") final String userUuid) throws Exception {
        List<QuestionEntity> listOfUserQuestions = new ArrayList<>();
        try{
            String[] bearerAccessToken = authorization.split("Bearer ");
            try {
                listOfUserQuestions = questionBusinessService.getAllQuestions(bearerAccessToken[1],userUuid);
            } catch (AuthorizationFailedException e) {
                e.printStackTrace();
            }
        }
        catch(ArrayIndexOutOfBoundsException are){
            listOfUserQuestions = questionBusinessService.getAllQuestions(authorization,userUuid);
        }

        ListIterator<QuestionEntity> questions = listOfUserQuestions.listIterator();
        List<QuestionDetailsResponse> displayQuestionIdAndContent = new ArrayList<>();
        while(questions.hasNext()){
            QuestionDetailsResponse questionDetailsResponse = new QuestionDetailsResponse().id(questions.next().getUuid()).content(questions.next().getContent());
            displayQuestionIdAndContent.add(questionDetailsResponse);

        }
        return new ResponseEntity<List<QuestionDetailsResponse>>(displayQuestionIdAndContent,HttpStatus.CREATED);
    }


}
