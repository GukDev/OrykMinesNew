package com.gukdev.orykminesnew;

import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class MineRegion {
    private final ProtectedRegion region;

    public MineRegion(ProtectedRegion region) {
        this.region = region;
    }

    public ProtectedRegion getRegion() {
        return region;
    }
}
