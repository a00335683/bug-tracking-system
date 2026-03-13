Feature: Login

Scenario: Successful login
  Given url baseUrl + '/api/auth/login'
  And request
  """
  {
    "username": "admin",
    "password": "password"
  }
  """
  When method post
  Then status 200