Feature: Filter Issues

Scenario: Filter issues by priority

  * def projectName = 'Filter Project ' + java.util.UUID.randomUUID()

  # login admin
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

  # login tester
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

  # create project
  Given url baseUrl + '/api/projects'
  And header Authorization = 'Bearer ' + adminToken
  And request
  """
  {
    "name": "#(projectName)",
    "description": "Filter testing project"
  }
  """
  When method post
  Then status 201
  * def projectId = response.id

  # create HIGH priority issue
  Given url baseUrl + '/api/issues'
  And header Authorization = 'Bearer ' + testerToken
  And request
  """
  {
    "projectId": #(projectId),
    "reporterId": 2,
    "title": "High Priority Issue",
    "description": "Important bug",
    "priority": "HIGH"
  }
  """
  When method post
  Then status 201

  # create LOW priority issue
  Given url baseUrl + '/api/issues'
  And header Authorization = 'Bearer ' + testerToken
  And request
  """
  {
    "projectId": #(projectId),
    "reporterId": 2,
    "title": "Low Priority Issue",
    "description": "Minor bug",
    "priority": "LOW"
  }
  """
  When method post
  Then status 201

  # filter HIGH priority issues
  Given url baseUrl + '/api/issues/filter'
  And header Authorization = 'Bearer ' + adminToken
  And param priority = 'HIGH'
  When method get
  Then status 200
  And match each response[*].priority == 'HIGH'