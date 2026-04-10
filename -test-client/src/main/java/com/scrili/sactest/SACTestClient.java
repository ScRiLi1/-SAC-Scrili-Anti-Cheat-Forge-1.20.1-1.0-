package com.scrili.sactest;

import net.minecraftforge.fml.common.Mod;

/**
 * SAC Test Client — тестовые читы для проверки SAC античита.
 * Открыть меню: INSERT
 * Все читы управляются через GUI меню.
 */
@Mod("sac_test")
public class SACTestClient {
    public SACTestClient() {
        // CheatController и WallHack регистрируются автоматически через @Mod.EventBusSubscriber
    }
}
