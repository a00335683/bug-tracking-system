Feature: Assign Issue

Scenario: Admin assigns issue to developer
  * def projectName = 'Assign Project ' + java.util.UUID.randomUUID()

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
    "description": "Assignment test project"
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
    "title": "Assign Issue",
    "description": "Issue for assignment",
    "priority": "MEDIUM"
  }
  """
  When method post
  Then status 201
  * def issueId = response.id

  Given url baseUrl + '/api/issues/' + issueId + '/assign'
  And header Authorization = 'Bearer ' + adminToken
  And request
  """
  {
    "developerId": 3
  }
  """
  When method put
  Then status 200