package com.upgrad.quora.api.controller;


import ch.qos.logback.core.encoder.EchoEncoder;
import com.upgrad.quora.service.business.UserBusinessService;
import com.upgrad.quora.service.entity.UserEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.api.model.UserDetailsResponse;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")

public class CommonController {
    /** comments by Avia **/
    //This endpoint allows users to retrieve the profile information of any other user in the Quora app.

    @Autowired
    UserBusinessService userBusinessService;

    @RequestMapping(method = RequestMethod.GET, path = "/userprofile/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserDetailsResponse> userProfile(@PathVariable("userId") final String userUuid, @RequestHeader("authorization") final String authorization) throws Exception, NullPointerException {
        UserEntity userEntity;
        try{
        String[] userToken = authorization.split("Bearer ");
            userEntity = userBusinessService.getUser(userUuid, userToken[1]);}
        catch(Exception e){
            userEntity = userBusinessService.getUser(userUuid, authorization);
        }
        UserDetailsResponse userDetailsResponse = new UserDetailsResponse().firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName()).emailAddress(userEntity.getEmail())
                .contactNumber(userEntity.getContactNumber()).userName(userEntity.getUserName())
                .country(userEntity.getCountry()).aboutMe(userEntity.getAboutMe()).dob(userEntity.getDob());
        return new ResponseEntity<UserDetailsResponse>(userDetailsResponse, HttpStatus.OK);



    }
}
