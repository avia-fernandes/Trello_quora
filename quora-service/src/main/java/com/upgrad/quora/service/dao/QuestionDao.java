package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.QuestionEntity;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class QuestionDao {
    @PersistenceContext
    private EntityManager entityManager;

    /** comments by Avia **/
    //This method updates the question in the database
    public QuestionEntity updateQuestion(final QuestionEntity questionEntity) {
        QuestionEntity updatedQ = entityManager.merge(questionEntity);
            return updatedQ;
    }

    /** comments by Avia **/
    //This method retrieves all the questions posted by a user and returns null if the list is empty.
    public List<QuestionEntity> getAllQuestionsByUser(final String userUuid){
        try{
            return entityManager.createNamedQuery("AllQuestionsByUser", QuestionEntity.class).setParameter("user",userUuid).getResultList();
        }
        catch (NoResultException nre){
            return null;
        }
    }

}

