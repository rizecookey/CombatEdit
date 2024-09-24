package net.rizecookey.combatedit.configuration;

import com.google.gson.annotations.SerializedName;

public class MiscConfiguration {
    @SerializedName("enable_1_8_knockback") private Boolean enable1_8Knockback;
    private Boolean disableSweepingWithoutEnchantment;

    public MiscConfiguration(boolean enable1_8Knockback, boolean disableSweepingWithoutEnchantment) {
        this.enable1_8Knockback = enable1_8Knockback;
        this.disableSweepingWithoutEnchantment = disableSweepingWithoutEnchantment;
    }

    public MiscConfiguration() {}

    public boolean is1_8KnockbackEnabled() {
        return enable1_8Knockback;
    }

    public void set1_8KnockbackEnabled(boolean enable1_8Knockback) {
        this.enable1_8Knockback = enable1_8Knockback;
    }

    public boolean isSweepingWithoutEnchantmentDisabled() {
        return disableSweepingWithoutEnchantment;
    }

    public void setSweepingWithoutEnchantmentDisabled(boolean disableSweepingWithoutEnchantment) {
        this.disableSweepingWithoutEnchantment = disableSweepingWithoutEnchantment;
    }

    public void validate(MiscConfiguration defaults) {
        if (enable1_8Knockback == null) {
            enable1_8Knockback = defaults.enable1_8Knockback;
        }
        if (disableSweepingWithoutEnchantment == null) {
            disableSweepingWithoutEnchantment = defaults.disableSweepingWithoutEnchantment;
        }
    }
}
