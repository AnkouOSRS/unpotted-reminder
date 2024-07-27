package com.unpottedreminder;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MeleeAlertStyle {
    ATTACK_AND_STRENGTH("Attack & Strength"),
    STR_ONLY("Strength only");

    private final String name;

    @Override
    public String toString()
    {
        return name;
    }
}
