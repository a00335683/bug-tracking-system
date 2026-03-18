Feature: Create Issue

Scenario: Tester creates issue in project
  * def projectName = 'Issue Project ' + java.util.UUID.randomUUID()

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
  * def adminToken = response.token

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
  * def testerToken = response.token

  Given url baseUrl + '/api/projects'
  And header Authorization = 'Bearer ' + adminToken
  And request
  """
  {
    "name": "#(projectName)",
    "description": "Project for issue test"
  }
  """
  When method post
  Then status 201
  * def projectId = response.id

  Given url baseUrl + '/api/issues'
  And header Authorization = 'Bearer ' + testerToken
  And request
  """
  {
    "projectId": #(projectId),
    "reporterId": 2,
    "title": "Test Issue",
    "description": "Created by Karate",
    "priority": "HIGH"
  }
  """
  When method post
  Then status 201