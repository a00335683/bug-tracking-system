Feature: Update Issue Status

Scenario: Issue workflow
  * def projectName = 'Workflow Project ' + java.util.UUID.randomUUID()

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

  Given url baseUrl + '/api/auth/login'
  And request
  """
  {
    "username": "dev1",
    "password": "pass"
  }
  """
  When method post
  Then status 200
  * def devToken = response.token

  Given url baseUrl + '/api/projects'
  And header Authorization = 'Bearer ' + adminToken
  And request
  """
  {
    "name": "#(projectName)",
    "description": "Workflow test project"
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
    "title": "Workflow Issue",
    "description": "Status workflow",
    "priority": "HIGH"
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

  Given url baseUrl + '/api/issues/' + issueId + '/status'
  And header Authorization = 'Bearer ' + devToken
  And request
  """
  {
    "status": "IN_PROGRESS"
  }
  """
  When method put
  Then status 200

  Given url baseUrl + '/api/issues/' + issueId + '/status'
  And header Authorization = 'Bearer ' + devToken
  And request
  """
  {
    "status": "RESOLVED",
    "resolutionNote": "Fixed"
  }
  """
  When method put
  Then status 200

  Given url baseUrl + '/api/issues/' + issueId + '/status'
  And header Authorization = 'Bearer ' + testerToken
  And request
  """
  {
    "status": "VERIFIED"
  }
  """
  When method put
  Then status 200

  Given url baseUrl + '/api/issues/' + issueId + '/status'
  And header Authorization = 'Bearer ' + adminToken
  And request
  """
  {
    "status": "CLOSED"
  }
  """
  When method put
  Then status 200