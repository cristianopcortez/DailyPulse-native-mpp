package com.petros.efthymiou.dailypulse.network

/**
 * GraphQL payloads used by the in-process UI-test HttpClient (iOS XCUITest)
 * and kept in sync with AndroidGraphqlFixtures.
 */
object UiTestGraphqlFixtures {

    const val ARTICLES_SUCCESS = """{
  "data": {
    "articles": [
      {
        "title": "Breaking: AI Breakthrough in Medical Diagnosis",
        "desc": "New machine learning model achieves 95% accuracy in early cancer detection",
        "date": "2026-08-27T10:00:00Z",
        "imageUrl": ""
      },
      {
        "title": "Tech Giants Invest in Green Energy",
        "desc": "Major technology companies announce billion-dollar renewable energy commitments",
        "date": "2026-08-26T14:30:00Z",
        "imageUrl": ""
      },
      {
        "title": "Quantum Computing Milestone Reached",
        "desc": "Scientists achieve quantum supremacy with new processor design",
        "date": "2026-08-25T09:15:00Z",
        "imageUrl": ""
      }
    ]
  }
}"""

    const val SOURCES_SUCCESS = """{
  "data": {
    "sources": [
      {
        "id": "techcrunch",
        "name": "TechCrunch",
        "desc": "Technology",
        "origin": "US"
      },
      {
        "id": "wired",
        "name": "Wired",
        "desc": "Technology",
        "origin": "US"
      }
    ]
  }
}"""

    const val AGGREGATORS_SUCCESS = """{
  "data": {
    "aggregators": [
      {
        "id": "tech",
        "name": "Technology"
      },
      {
        "id": "business",
        "name": "Business"
      }
    ]
  }
}"""

    const val ERROR_RESPONSE = """{
  "errors": [
    {
      "message": "Internal server error",
      "extensions": {
        "code": "INTERNAL_SERVER_ERROR"
      }
    }
  ]
}"""
}
