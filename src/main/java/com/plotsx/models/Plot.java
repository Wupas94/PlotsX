package com.plotsx.models;

import org.bukkit.Location;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Plot {
    private final UUID id;
    private final UUID owner;
    private final Set<UUID> coOwners;
    private final Location center;
    private final int radius;
    private final long creationDate;
    private boolean active;
    private boolean pvpEnabled;
    private boolean mobDamageEnabled;
    private boolean basicInteractionsEnabled;

    public Plot(UUID owner, Location center, int radius) {
        this.id = UUID.randomUUID();
        this.owner = owner;
        this.coOwners = new HashSet<>();
        this.center = center;
        this.radius = radius;
        this.creationDate = System.currentTimeMillis();
        this.active = true;
        this.pvpEnabled = false;
        this.mobDamageEnabled = false;
        this.basicInteractionsEnabled = true;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwner() {
        return owner;
    }

    public Set<UUID> getCoOwners() {
        return coOwners;
    }

    public Location getCenter() {
        return center;
    }

    public int getRadius() {
        return radius;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public void setPvpEnabled(boolean pvpEnabled) {
        this.pvpEnabled = pvpEnabled;
    }

    public boolean isMobDamageEnabled() {
        return mobDamageEnabled;
    }

    public void setMobDamageEnabled(boolean mobDamageEnabled) {
        this.mobDamageEnabled = mobDamageEnabled;
    }

    public boolean isBasicInteractionsEnabled() {
        return basicInteractionsEnabled;
    }

    public void setBasicInteractionsEnabled(boolean basicInteractionsEnabled) {
        this.basicInteractionsEnabled = basicInteractionsEnabled;
    }

    public boolean isOwner(UUID playerId) {
        return owner.equals(playerId);
    }

    public boolean isCoOwner(UUID playerId) {
        return coOwners.contains(playerId);
    }

    public boolean hasAccess(UUID playerId) {
        return isOwner(playerId) || isCoOwner(playerId);
    }

    public void addCoOwner(UUID playerId) {
        coOwners.add(playerId);
    }

    public void removeCoOwner(UUID playerId) {
        coOwners.remove(playerId);
    }

    public boolean isInPlot(Location location) {
        if (!location.getWorld().equals(center.getWorld())) {
            return false;
        }
        
        double distanceX = Math.abs(location.getX() - center.getX());
        double distanceZ = Math.abs(location.getZ() - center.getZ());
        
        return distanceX <= radius && distanceZ <= radius;
    }
} 