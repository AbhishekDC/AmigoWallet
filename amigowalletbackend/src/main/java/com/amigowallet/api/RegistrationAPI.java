package com.amigowallet.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.amigowallet.model.SecurityQuestion;
import com.amigowallet.model.User;
import com.amigowallet.service.RegistrationService;

/**
 * This class has methods to handle registration requests.
 * 
 *  @author ETA_JAVA
 * 
 */
@CrossOrigin
@RestController
@RequestMapping("RegistrationAPI")
public class RegistrationAPI {
	
	/**
	 * This attribute is used for getting property values from
	 * <b>configuration.properties</b> file
	 */
	@Autowired
	private Environment environment;
	
	@Autowired
	RegistrationService registrationService;
	
	static Logger logger = LogManager.getLogger(RegistrationAPI.class.getName());
	
	/**
	 * This method receives the user model in POST request and calls
	 * RegistrationService method to verify the user details. <br>
	 * If verification is success then it sends OTP in an email
	 * to the email id of the user received in POST request. 
	 * Then it sends success message to the client.<br>
	 * If verification fails then it sends failure message to the client.
	 * 
	 * @param user
	 * 
	 * @return ResponseEntity<User> populated with 
	 * 					successMessage,
	 * 								if successfully deleted
	 * 					errorMessage,
	 * 								if any error occurs
	 */
	@RequestMapping(value = "validateForRegistration", method = RequestMethod.POST)
	public ResponseEntity<User> validateForRegistration(@RequestBody User user) {
		ResponseEntity<User> responseEntity=null;
		try{
			logger.info("USER TRYING TO REGISTER, VALIDATING DETAILS. USER NAME : "+
						user.getName()+", EMAIL ID : "+user.getEmailId());
			
			registrationService.validateUser(user);	
			
			/*
			 * The following code populates a user model with a success message
			 */
			user.setSuccessMessage(environment.getProperty("RegistrationAPI.SUCCESSFULLY_VALIDATED"));
			responseEntity=new ResponseEntity<User>(user, HttpStatus.ACCEPTED);
		} catch (Exception e) {
			
			if(e.getMessage().contains("Validator")){
				throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, environment.getProperty(e.getMessage()));
			}
			throw new ResponseStatusException(HttpStatus.CONFLICT, environment.getProperty(e.getMessage()));
		}
		return responseEntity;
	}
	
	@RequestMapping(value="getAllQuestions",method=RequestMethod.GET)
	public ResponseEntity<List<SecurityQuestion>> getAllQuestions(){
		ResponseEntity<List<SecurityQuestion>> responseEntity=null;
		ArrayList<SecurityQuestion> securityQuestions = new ArrayList<>();
			logger.info("Getting all the security questions");

			/*
			 * The following code again validates the user details
			 */
			securityQuestions = registrationService.getAllSecurityQuestions();
			
			responseEntity = new ResponseEntity<List<SecurityQuestion>>(securityQuestions, HttpStatus.OK);
			
		return responseEntity;
	}
	
	
	/**
	 * This method receives the user model in POST request and calls
	 * RegistrationService method to validate the user details. <br>
	 * It also verifies the OTP<br>
	 * If verification is success then it calls the registerUser method which
	 * adds the user details into the database 
	 * Then it sends success message along with the
	 * registrationId to the client.<br>
	 * If verification fails then it sends failure message to the client.
	 * 
	 * @param user
	 * 
	 * @return success or failure message as a ResponseEntity along with HTTP
	 *         Status code
	 */
	@RequestMapping(value = "register", method = RequestMethod.POST)
	public ResponseEntity<String> userRegistration(@RequestBody User user)
	{
		ResponseEntity<String> responseEntity=null;
		String message=null;
		try {

			/*
			 * The following code again validates the user details
			 */
			registrationService.validateUser(user);

			/*
			 * registrationId is generated by saving the user to the database
			 */
			Integer registrationId = registrationService.registerUser(user);

			logger.info("USER REGISTERED SUCCESSFULLY, USER EMAIL : "+user.getEmailId());

			/*
			 * The following code populates a string with a success message
			 */
			message = environment.getProperty("RegistrationAPI.SUCCESSFUL_REGISTRATION")+ registrationId;
			
			responseEntity=new ResponseEntity<String>(message, HttpStatus.CREATED);
		} 
		catch (Exception e){
			if(e.getMessage().contains("Validator")){
				throw new ResponseStatusException(HttpStatus.NOT_ACCEPTABLE, environment.getProperty(e.getMessage()));
			}
			throw new ResponseStatusException(HttpStatus.CONFLICT, environment.getProperty(e.getMessage()));
		}
		
		return responseEntity;
	}
}
