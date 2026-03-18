Feature: Login

Scenario: Successful login as admin
  Given url baseUrl + '/api/auth/login'
  And request
  """
  {
    "username": "admin",
    "password": "admin123"
  }
  """
  When method post
  Then status 200
  And match response.token != null

Scenario: Successful login as tester
  Given url baseUrl + '/api/auth/login'
  And request
  """
  {
    "username": "tester1",
    "password": "tester123"
  }
  """
  When method post
  Then status 200
  And match response.token != null

Scenario: Successful login as developer
  Given url baseUrl + '/api/auth/login'
  And request
  """
  {
    "username": "dev1",
    "password": "dev123"
  }
  """
  When method post
  Then status 200
  And match response.token != null