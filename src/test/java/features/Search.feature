Feature: Search and Place Orders for Football Items

#@SeleniumTest
Scenario: Select the Item and navigate to a Checkout page
Given User is on the online shop landing page
When User search for "Football" items
And "Football" model results are displayed on the page
And User proceeded to the Checkout page to buy the items
Then verified selected "Football" items are displayed on the Checkout page