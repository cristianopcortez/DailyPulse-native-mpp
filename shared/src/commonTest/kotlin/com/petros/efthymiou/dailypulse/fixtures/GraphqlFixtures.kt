package com.petros.efthymiou.dailypulse.fixtures

/**
 * GraphQL fixtures for testing.
 * These simulate responses from the BFF GraphQL endpoint.
 */
object GraphqlFixtures {

    /**
     * GraphQL response for articles query.
     * Contains 2 articles: one recent tech article and one business article.
     */
    const val ARTICLES_RESPONSE = """{
  "data": {
    "articles": [
      {
        "title": "Breaking: AI Breakthrough in Medical Diagnosis",
        "desc": "New machine learning model achieves 95% accuracy in early cancer detection",
        "date": "2026-08-27T10:00:00Z",
        "imageUrl": "https://example.com/images/ai-medical.jpg"
      },
      {
        "title": "Tech Giants Invest in Green Energy",
        "desc": "Major technology companies announce billion-dollar renewable energy commitments",
        "date": "2026-08-26T14:30:00Z",
        "imageUrl": "https://example.com/images/green-energy.jpg"
      }
    ]
  }
}"""

    /**
     * GraphQL response for sources query.
     * Contains 3 news sources for a given aggregator.
     */
    const val SOURCES_RESPONSE = """{
  "data": {
    "sources": [
      {
        "id": "techcrunch",
        "name": "TechCrunch",
        "category": "Technology",
        "country": "US"
      },
      {
        "id": "wired",
        "name": "Wired",
        "category": "Technology",
        "country": "US"
      },
      {
        "id": "theverge",
        "name": "The Verge",
        "category": "Technology",
        "country": "US"
      }
    ]
  }
}"""

    /**
     * GraphQL response for aggregators query.
     * Contains 3 news aggregators.
     */
    const val AGGREGATORS_RESPONSE = """{
  "data": {
    "aggregators": [
      {
        "id": "tech",
        "name": "Technology",
        "description": "Latest technology news and trends"
      },
      {
        "id": "business",
        "name": "Business",
        "description": "Business and finance updates"
      },
      {
        "id": "science",
        "name": "Science",
        "description": "Scientific discoveries and research"
      }
    ]
  }
}"""

    /**
     * GraphQL response with empty articles list.
     */
    const val EMPTY_ARTICLES_RESPONSE = """{
  "data": {
    "articles": []
  }
}"""

    /**
     * GraphQL error response (network error simulation).
     */
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

    /**
     * Malformed JSON response for error handling tests.
     */
    const val MALFORMED_RESPONSE = """{ "data": { "articles": [ { "title": "Missing closing brace" """
}
