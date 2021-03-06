package stepdefinition;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import cucumber.api.java.en.Then;
import cucumber.api.DataTable;
import cucumber.api.junit.Cucumber;
import objectrepository.AccountRepository;
import objectrepository.HomeRepository;
import objectrepository.LoginRepository;
import objectrepository.LogoutRepository;
import objectrepository.RegistrationRepository;
import util.ChromeWebDriverUtility;
import util.JdbcConnection;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import java.lang.String;

import org.apache.logging.log4j.*;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

@RunWith(Cucumber.class)
public class LoginStepDefinition extends ChromeWebDriverUtility {
	private static final Logger logger = LogManager.getLogger(LoginStepDefinition.class.getName());
	HomeRepository homeRepository;
	LoginRepository loginRepository;
	AccountRepository accountRepository;
	RegistrationRepository registrationRepository;
	
    @Given("^User is on the Home Page$")
    public void user_is_on_the_home_page() throws Throwable {
    	webDriver = ChromeWebDriverUtility.getWebDriver();
    	homeRepository = new HomeRepository(webDriver);
    	
    	testPageTitle("FAI Shop | Ireland | Football | FAI", webDriver.getTitle());
    }
    
    // Login with usage of Parameterisation
    @When("^User logs in with a username (.+) and password (.+)$")
    public void user_logs_in_with_username_and_password(String email, String password) throws Throwable {
    	// Create Simple Page Objects   	
    	loginRepository = new LoginRepository(webDriver);
    	
    	logger.info("Username: " + email + ", Password: " + password);
    	// Click the Login link
    	homeRepository.getLoginLinkElement().click();
    	// Type the email
    	loginRepository.getEmailInputElement().sendKeys(email);
    	// Type the password
    	loginRepository.getPasswordInputElement().sendKeys(password);
    	// Click the Login button
    	loginRepository.getLoginButtonElement().click();
    }
    
    // Login with usage of Data Driven from the Excel file
    @When("^User logs in with credentials retrieved from the Excel file$")
    public void user_logs_in_with_credentials_retrieved_from_excel_file() throws Throwable {
    	// Create Simple Page Objects
    	loginRepository = new LoginRepository(webDriver);
    	// All Data retrieved from the Excel file will be stored in this Array
    	ArrayList<String> loginDataArrayList = util.ExcelTestDataDriven.getExcelData("Login");
   	
    	// Click the Login link
    	homeRepository.getLoginLinkElement().click();
    	logger.debug("The Login link has been clicked!");
    	// Type the email
    	loginRepository.getEmailInputElement().sendKeys(loginDataArrayList.get(1));
    	logger.info("User has been navigated to the Login page!");
    	logger.debug("User Email has been entered!");
    	// Type the password
    	loginRepository.getPasswordInputElement().sendKeys(loginDataArrayList.get(2));
    	logger.debug("User Password has been entered!");
    	// Click the Login button
    	loginRepository.getLoginButtonElement().click();
    	logger.info("The Login Button has been clicked!");
    }
    
    // Login with usage of Data Driven from DB
    @When("^User logs in with credentials retrieved from DB$")
    public void user_logs_in_with_credentials_retrieved_from_db() throws IOException, SQLException {
    	String email = "";
    	String password = "";
    	// Create Simple Page Objects
    	loginRepository = new LoginRepository(webDriver);
    	ResultSet logins = JdbcConnection.getLoginUsers();
    	
		while (logins.next()) {
			email = logins.getString("email");
			password = logins.getString("password");
			
			logger.info("Email retrieved from DB: " + email);
			logger.info("Password retrieved from DB: " + password);
		}
    		
    	// Click the Login link
    	homeRepository.getLoginLinkElement().click();
    	logger.debug("The Login link has been clicked!");
    	// Type the email
    	loginRepository.getEmailInputElement().sendKeys(email);
    	logger.info("User has been navigated to the Login page!");
    	logger.debug("User Email has been entered!");
    	// Type the password
    	loginRepository.getPasswordInputElement().sendKeys(password);
    	logger.debug("User Password has been entered!");
    	// Click the Login button
    	loginRepository.getLoginButtonElement().click();
    	logger.info("The Login Button has been clicked!");
    }

    @Then("^User accesses their profile page \"([^\"]*)\"$")
    public void user_accesses_their_profile_page(String access) throws Throwable {
    	accountRepository = new AccountRepository(webDriver);
    	
    	if (access.equals("true")) {
	    	Assert.assertTrue(accountRepository.getLogoutButtonElement().getText().contains("Logout"));
	    	logger.info("User Accessed their Account!");
    	}
    	
    	if (access.equals("false")) {
    		Assert.assertTrue(loginRepository.getWarningAlertElement().getText().contains("Warning"));
    		logger.warn("User Provided Wrong Credentials!");
    	}
    	
    	if (access.equals("false_registration_empty_fields")) {
    		List<WebElement> warningElements = webDriver.findElements(By.className("text-danger"));
    		checkRegistrationWarningMessages(warningElements);
    		checkRegistrationEmptyInputFields();       	
    		logger.warn("You need to fill all the required input fields!");
    	}
    	
    	if (access.equals("false_registration_password_failure")) {
    		checkPasswordIfEmpty(registrationRepository.getRegistrationPassword().getAttribute("value"), "Password");
    		checkPasswordIfEmpty(registrationRepository.getRegistrationConfirm().getAttribute("value"), "Confirmation Password");
    		logger.warn("Password Failure has occurred!");
    	}
    }
    
    // User Logout
    @And("^User logs out$")
    public void user_logs_out() throws Throwable {
    	testPageTitle("My Account", webDriver.getTitle());
    	
    	accountRepository.getLogoutButtonElement().click();
    	logger.info("User has been successfully Logged Out!");
    }
    
    @Then("^the Login Page should display$")
    public void login_page_should_display() throws Throwable {
    	LogoutRepository logoutRepository = new LogoutRepository(webDriver);
    	Assert.assertTrue(homeRepository.getLoginLinkElement().getText().contains("Login"));
    	Assert.assertTrue(logoutRepository.getLogoutHeaderText().getText().contains("Logout"));
    	logger.info("The Login page is displayed!");
    }
    
    // User Registration
    @When("^User register with following details$")
    public void user_register_with_following_details(DataTable dataTable) throws Throwable {    	
    	registrationRepository = new RegistrationRepository(webDriver);
    	List<List<String>> list = dataTable.raw();
  
    	displayRegistrationListInfo(list);   	
    	navigateToRegistrationPage();    	
    	fillRegistrationFormWithDataTableInfo(list);   	   
    	selectCountryFromDropDownBox();
    	
    	// EXPLICIT WAIT applied - targets only the specific Element (second Drop-Down List)
    	// Wait before the Region dynamic Drop-Down List is loaded and populated with Strings
    	WebDriverWait wait = new WebDriverWait(webDriver, 2);
    	wait.until(ExpectedConditions.visibilityOfElementLocated(registrationRepository.getSelectedRegionOption()));
    	
    	selectRegionFromDropDownBox();
    	handleRegistrationPageRadioButtons();    	
    	comparePasswords(registrationRepository.getRegistrationPassword().getAttribute("value"), registrationRepository.getRegistrationConfirm().getAttribute("value"));    	
    	handleRegistrationPageAgreeTermsCheckBox();
    	
    	// Submit and Register
    	registrationRepository.getRegistrationButton().click();
    	logger.debug("Registration Submit Button has been clicked!");
    }
    
    // **************************************************************************************************
    //										HELPER METHODS
    // **************************************************************************************************
    
    // Get all information from the DataTable for Registration input Fields
    private void displayRegistrationListInfo(List<List<String>> list) {
    	logger.info("User tries to Register with the following details:");					
    	
    	for (int i = 0; i < list.size(); i++) {
			logger.info("\nName: " + list.get(i).get(0) +
						"\nLast Name: " + list.get(i).get(1) +
						"\nEmail: " + list.get(i).get(2) +
						"\nTelephone: " + list.get(i).get(3) +
						"\nAddress: " + list.get(i).get(4) +
						"\nPhone 2: " + list.get(i).get(5) +
						"\nCity: " + list.get(i).get(6) +
						"\nPassword: " + list.get(i).get(7) +
						"\nConfirmed: " + list.get(i).get(8));
    	}
    }
    
    // Navigate to Registration Page
    private void navigateToRegistrationPage() {
    	homeRepository.getRegistrationLinkElement().click();
    	logger.debug("Registration Link has been clicked!");
    	
    	// Confirm the Registration page is loaded
    	Assert.assertTrue(registrationRepository.getRegistrationHeaderText().getText().contains("Register"));
    	logger.info("Registration Page has been loaded!");
    }
    
    // Fill in the Registration Form
    private void fillRegistrationFormWithDataTableInfo(List<List<String>> list) {
    	for (int i = 0; i < registrationRepository.getRegistrationInputFieldSelectors().size(); i++) {
    		WebElement requiredField = webDriver.findElement(By.cssSelector(registrationRepository.getRegistrationInputFieldSelectors().get(i)));
    		requiredField.sendKeys(list.get(0).get(i));
    	}
    }
    
    // Check for Registration Warning Messages
    private void checkRegistrationWarningMessages(List<WebElement> list) {
		for (int i = 0; i < list.size(); i++) {
			logger.warn(list.get(i).getText());
			
			if (list.get(i).isDisplayed()) {
				Assert.assertFalse(list.get(i).getText().length() == 0);
			}
		}
    }
    
    // Check for Registration empty Input Fields
    private void checkRegistrationEmptyInputFields() {		
    	for (int i = 0; i < registrationRepository.getRegistrationInputFieldSelectors().size(); i++) {
    		WebElement requiredField = webDriver.findElement(By.cssSelector(registrationRepository.getRegistrationInputFieldSelectors().get(i)));
    		
    		if ((requiredField.getText()).equals("")) {
    			logger.debug(i + " - " + requiredField + " is blank!");
    			Assert.assertTrue(requiredField.getText().isEmpty());
    		}
    	}
    }
    
    // Compare passwords
    private void comparePasswords(String password, String confirm) {
    	if (password.length() >= 4) {
    		logger.info("Length of the Password *** " + password + " *** is correct: " + password.length() + " characters!");
    		
        	if (password.equals(confirm)) {
        		Assert.assertTrue(password.matches(confirm));
        		logger.info("Password and Confimation Password mmatch!");
        	} else {
        		Assert.assertFalse(password.matches(confirm));
        		logger.warn("Password and Confirmation Password do not match!");
        	}	
    	} else {
    		Assert.assertFalse(password.length() >= 4);
    		logger.warn("Password *** " + password + " *** is too short: " + password.length() + " characters!");
    	}
    }
    
    // Check if Password or/and Confirm is/are empty
    private void checkPasswordIfEmpty(String password, String passType) {
    	if (password.length() == 0) {
    		Assert.assertTrue(password.isEmpty());
    		logger.warn(passType + " is not provided!");
    	}
    }
    
    // Select Country from the Selection Options
    private void selectCountryFromDropDownBox() {
    	String country = "Poland";
    	Select selectCountry = new Select(registrationRepository.getCountry());
    	selectCountry.selectByVisibleText(country);
    	Assert.assertEquals(selectCountry.getFirstSelectedOption().getText(), country);
    	logger.debug("Selected country option is: " + country);
    }
    
    // Select Region from the Selection Options
    private void selectRegionFromDropDownBox() {
    	String region = "Pomorskie";
    	Select selectState = new Select(registrationRepository.getZone());   	
    	selectState.selectByVisibleText(region);
    	Assert.assertEquals(selectState.getOptions().get(11).getText(), region);
    	logger.debug("Selected region option is: " + region);
    	//selectState.selectByIndex(11);
    }
    
    // Handle Radio Buttons
    private void handleRegistrationPageRadioButtons() {
    	List<WebElement> radioButtonsList = registrationRepository.getRadioButtons();
    	
    	// check if Radio Buttons exists on the page
    	if (radioButtonsList.size() == 2) {
    		int counter = 1;
    		
    		for (int i = 0; i < radioButtonsList.size(); i++) {
    			Assert.assertTrue(radioButtonsList.get(i).isDisplayed());
    			logger.debug("Radio Button " + i + " is displayed " + radioButtonsList.get(i).isDisplayed());
    			
    			// Verify that both Radio Buttons are not selected
    			if (radioButtonsList.get(i).isSelected()) {
        			Assert.assertFalse(radioButtonsList.get(counter).isSelected());
        			logger.info("Radio Button " + i + " is selected " + radioButtonsList.get(counter).isSelected());
        			radioButtonsList.get(counter).click();
        			logger.debug("Radio Button " + i + " has been clicked!");
        			Assert.assertTrue(radioButtonsList.get(counter).isSelected());
        			logger.info("Radio Button " + i + " is selected " + radioButtonsList.get(counter).isSelected());
    			}
    			counter--;
    		}
    	}
    	logger.info("Number of Radio Buttons: " + radioButtonsList.size());
    }
    
    // Handle the Agree Terms & Conditions Check Box
    private void handleRegistrationPageAgreeTermsCheckBox() {
    	// Handle the Check Box to agree for Terms and Conditions
    	WebElement termsAgreeCheckBox = registrationRepository.getAgreeCheckBox();
    	Assert.assertFalse(termsAgreeCheckBox.isSelected());
    	logger.info("Agree Terms Check Box is selected " + termsAgreeCheckBox.isSelected());
    	termsAgreeCheckBox.click();
    	logger.debug("Agree Terms Check Box has been clicked!");
    	Assert.assertTrue(termsAgreeCheckBox.isSelected());
    	logger.info("Agree Terms Check Box is selected " + termsAgreeCheckBox.isSelected());
    	logger.info("Number of Checkboxes: " + registrationRepository.getCheckBoxes().size());
    }
    
	// Check the Page Title
	private void testPageTitle(String title, String pageTitle) {
		//String pageTitle = webDriver.getTitle();
		Assert.assertEquals(pageTitle, title);
		
		String titlePage = (title.equals(pageTitle)) ? "Page Title is Correct: " : "Page Title is Wrong: ";
		logger.info(titlePage + pageTitle);
	}
}
