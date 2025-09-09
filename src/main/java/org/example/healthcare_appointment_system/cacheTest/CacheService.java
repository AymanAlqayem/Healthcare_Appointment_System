package org.example.healthcare_appointment_system.cacheTest;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class CacheService {
    private final CacheManager cacheManager;

    public CacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    /**
     * Evicts a specific doctor from the entity cache
     * Use when a specific doctor's data is updated
     */
    public void evictDoctorCache(Long doctorId) {
        if (cacheManager.getCache("doctors") != null) {
            cacheManager.getCache("doctors").evict(doctorId);
        }
    }

    /**
     * Evict all doctors of a specific specialty from cache
     * Use when doctors are added/removed/updated that affect specialty queries
     */
    public void evictDoctorBySpecialtyCache(String specialty) {
        if (cacheManager.getCache("doctorBySpecialty") != null) {
            cacheManager.getCache("doctorBySpecialty").evict(specialty.toLowerCase());
        }
    }

    /**
     * Clears the entire "all doctors" cache
     * Use when any doctor is added, removed, or has major changes
     * that affect the complete list of doctors
     */
    public void evictAllDoctorsCache() {
        if (cacheManager.getCache("allDoctors") != null) {
            cacheManager.getCache("allDoctors").clear();
        }
    }

    public void clearAllDoctorCaches() {
        evictAllDoctorsCache();
        if (cacheManager.getCache("doctors") != null) {
            cacheManager.getCache("doctors").clear();
        }
        if (cacheManager.getCache("doctorBySpecialty") != null) {
            cacheManager.getCache("doctorBySpecialty").clear();
        }
    }
}