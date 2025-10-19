package com.example.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import java.util.*


object JwtConfig {
    private lateinit var secret: String
    private lateinit var issuer: String
    private lateinit var audience: String
    private lateinit var realm: String
    lateinit var verifier: JWTVerifier
    private var ttl: Long = 3600

    fun initialize(secret: String, issuer: String, audience: String, realm: String, ttl: Long) {
        this.secret = secret
        this.issuer = issuer
        this.audience = audience
        this.realm = realm
        this.ttl =  ttl
        val algorithm = Algorithm.HMAC256(secret)
        verifier = JWT.require(algorithm)
            .withIssuer(issuer)
            .withAudience(audience)
            .build()
    }

    fun makeToken(username: String): String {
        val algorithm = Algorithm.HMAC256(secret)
        return JWT.create()
            .withSubject("Authentication")
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("username", username)
            .withExpiresAt(Date(System.currentTimeMillis() + ttl * 1000))
            .sign(algorithm)
    }
}