package com.petros.efthymiou.dailypulse.android.fixtures

/**
 * Shared GraphQL fixtures for Android instrumented tests.
 * These match the responses from the BFF GraphQL endpoint.
 */
object AndroidGraphqlFixtures {

    /**
     * Complete GraphQL response for articles query.
     * Use this with MockWebServer's enqueue().
     */
    const val ARTICLES_SUCCESS = """{
  "data": {
    "articles": [
      {
        "title": "Breaking: AI Breakthrough in Medical Diagnosis",
        "desc": "New machine learning model achieves 95% accuracy in early cancer detection",
        "date": "2026-08-27T10:00:00Z",
        "imageUrl": "https://image.cnbcfm.com/api/v1/image/107326078-1698758530118-gettyimages-1765623456-wall26362_igj6ehhp.jpeg?v=1698758587&w=1920&h=1080"
      },
      {
        "title": "Tech Giants Invest in Green Energy",
        "desc": "Major technology companies announce billion-dollar renewable energy commitments",
        "date": "2026-08-26T14:30:00Z",
        "imageUrl": "https://image.cnbcfm.com/api/v1/image/107326078-1698758530118-gettyimages-1765623456-wall26362_igj6ehhp.jpeg?v=1698758587&w=1920&h=1080"
      },
      {
        "title": "Quantum Computing Milestone Reached",
        "desc": "Scientists achieve quantum supremacy with new processor design",
        "date": "2026-08-25T09:15:00Z",
        "imageUrl": "https://image.cnbcfm.com/api/v1/image/107326078-1698758530118-gettyimages-1765623456-wall26362_igj6ehhp.jpeg?v=1698758587&w=1920&h=1080"
      }
    ]
  }
}"""

    /**
     * GraphQL response for sources query.
     */
    const val SOURCES_SUCCESS = """{
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
     */
    const val AGGREGATORS_SUCCESS = """{
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
     * Empty articles response (for empty state testing).
     */
    const val ARTICLES_EMPTY = """{
  "data": {
    "articles": []
  }
}"""

    /**
     * GraphQL error response.
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
}
