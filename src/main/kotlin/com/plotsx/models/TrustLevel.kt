package com.plotsx.models

enum class TrustLevel {
    NONE,       // No trust, can only visit
    VISIT,      // Can visit and use basic interactions
    BUILD,      // Can build and break blocks
    MANAGE,     // Can manage plot settings
    OWNER       // Full ownership rights
} 